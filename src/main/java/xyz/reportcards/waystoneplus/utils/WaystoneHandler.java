package xyz.reportcards.waystoneplus.utils;

import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WaystoneHandler {

    public static Set<Pair<String, SimpleLocation>> cachedWaystones = new HashSet<>();
    static File waystoneFile;
    WaystonePlus instance;
    public WaystoneHandler(WaystonePlus instance) {
        this.instance = instance;
        waystoneFile = new File(instance.getDataFolder(), "waystones.json");
        if (!waystoneFile.exists()) {
            try {
                waystoneFile.createNewFile();
                FileUtils.writeStringToFile(waystoneFile, instance.getGson().toJson(new HashSet<>()), Charset.defaultCharset());
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to initialize waystones!");
                e.printStackTrace();
                instance.getServer().getPluginManager().disablePlugin(instance);
            }
        } else {
            try {
                Type setType = new TypeToken<HashSet<SimpleLocation>>() {
                }.getType();
                cachedWaystones = instance.getGson().fromJson(FileUtils.readFileToString(waystoneFile, Charset.defaultCharset()), setType);

                for (Pair<String, SimpleLocation> cachedWaystone : cachedWaystones) {
                    Block block = cachedWaystone.second.getBukkitLocation().getBlock();
                    if (!new NBTCustomBlock(block).getData().hasTag("waystone")) {
                        cachedWaystones.remove(cachedWaystone);
                    }
                }
                saveWaystones();
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to read waystones!");
                e.printStackTrace();
                instance.getServer().getPluginManager().disablePlugin(instance);
            }
        }
    }

    public static void saveWaystones() {
        Bukkit.getScheduler().runTaskAsynchronously(WaystonePlus.getInstance(), () -> {
            try {
//                Bukkit.getLogger().info("""
//                        Saving waystones...
//                        Waystones: %s
//                        """.formatted(WaystonePlus.getInstance().getGson().toJson(cachedWaystones)));
                FileUtils.writeStringToFile(waystoneFile, WaystonePlus.getInstance().getGson().toJson(cachedWaystones), Charset.defaultCharset());
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to save waystones!");
                e.printStackTrace();
            }
        });
    }

    public static void addWaystone(Block block) {
        if (!new NBTCustomBlock(block).getData().hasTag("waystone")) return;
        Pair<String, SimpleLocation> waystone = getWaystoneNameAndLocation(block);
        assert waystone != null;
        if (cachedWaystones.add(new Pair<>(waystone.first, waystone.second))) saveWaystones();
    }

    public static void removeWaystone(Block block) {
        Pair<String, SimpleLocation> waystone = getWaystoneNameAndLocation(block);
        assert waystone != null;
        if (cachedWaystones.remove(new Pair<>(waystone.first, waystone.second))) saveWaystones();
    }

    public static boolean isWaystone(Block block) {
        var nbtBlock = new NBTCustomBlock(block);
        SimpleLocation loc = new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

        if (!nbtBlock.getData().hasTag("waystone")) {
            boolean anyChange = false;
            for (Pair<String, SimpleLocation> cachedWaystone : cachedWaystones) {
                if (cachedWaystone.second.equals(loc)) {
                    cachedWaystones.remove(cachedWaystone);
                    anyChange = true;
                }
            }

            if (anyChange)
                saveWaystones();
            return false;
        }

        WaystoneNBT nbt = WaystoneNBT.fromString(nbtBlock.getData().getString("waystone"));
        String name = nbt.waystoneName;
        if (cachedWaystones.add(new Pair<>(name, loc)))
            saveWaystones();
        return true;
    }

    public static boolean isWaystone(SimpleLocation location) {
        Block block = location.getBukkitLocation().getBlock();
        return isWaystone(block);
    }

    public static boolean isWaystone(String world, long x, long y, long z) {
        SimpleLocation location = new SimpleLocation(world, x, y, z);
        Block block = location.getBukkitLocation().getBlock();

        return isWaystone(block);
    }

    private static Pair<String, SimpleLocation> getWaystoneNameAndLocation(Block block) {
        if (!new NBTCustomBlock(block).getData().hasTag("waystone")) return null;

        WaystoneNBT nbt = WaystoneNBT.fromString(new NBTCustomBlock(block).getData().getString("waystone"));
        return new Pair<>(nbt.waystoneName, new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    public static String getWaystoneName(Block block) {
        return Objects.requireNonNull(getWaystoneNameAndLocation(block)).first;
    }

    public static void teleportToWaystone(Player player, String waystoneName, SimpleLocation waystoneLocation) {
        WaystoneConfig waystoneConfig = WaystonePlus.getInstance().getWaystoneConfig();
        switch (waystoneConfig.teleportCostType) {
            case NONE -> {
                if (!checkCooldown(player))
                    return;
                setCooldown(player);

                player.teleport(waystoneLocation.getBukkitLocation());
            }
            case XP -> {
                if (!checkCooldown(player))
                    return;
                setCooldown(player);

                if (player.getLevel() < waystoneConfig.teleportCostLevels) {
                    player.sendMessage(Component.text("You need at least " + waystoneConfig.teleportCostLevels + " levels to teleport!", NamedTextColor.RED));
                    return;
                }

                player.setLevel(player.getLevel() - waystoneConfig.teleportCostLevels);
                player.teleport(waystoneLocation.getBukkitLocation());
            }
            case ITEM -> {
                if (!checkCooldown(player))
                    return;
                setCooldown(player);

                WaystoneConfig config = WaystonePlus.getInstance().getWaystoneConfig();
                ItemStack item = new ItemStack(Bukkit.createBlockData(config.teleportCostItemType).getMaterial(), config.teleportCostItemAmount);

                if (!player.getInventory().containsAtLeast(item, config.teleportCostItemAmount)) {
                    player.sendMessage(Component.text("You need at least " + config.teleportCostItemAmount + " " + item.getType().name() + " to teleport!", NamedTextColor.RED));
                    return;
                }

                player.getInventory().removeItem(item);
                player.teleport(waystoneLocation.getBukkitLocation());
            }
        }
    }

    private static boolean checkCooldown(Player player) {
        if (player.hasMetadata("waystone-teleport-cooldown")) {
            long cooldown = player.getMetadata("waystone-teleport-cooldown").get(0).asLong();
            if (System.currentTimeMillis() - cooldown >= WaystonePlus.getInstance().getWaystoneConfig().teleportCooldown * 1000L) {
                player.removeMetadata("waystone-teleport-cooldown", WaystonePlus.getInstance());
                return true;
            } else {
                player.sendMessage("§cYou are on cooldown!");
                return false;
            }
        }
        return true;
    }

    public static void setCooldown(Player player) {
        player.setMetadata("waystone-teleport-cooldown", new FixedMetadataValue(WaystonePlus.getInstance(), System.currentTimeMillis()));
    }
}
