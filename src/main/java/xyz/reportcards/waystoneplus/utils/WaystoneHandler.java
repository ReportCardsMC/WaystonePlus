package xyz.reportcards.waystoneplus.utils;

import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

/**
 * The waystone handler class, handles all direct waystone handling tasks
 */
public class WaystoneHandler {

    public static Set<Pair<String, SimpleLocation>> cachedWaystones = new HashSet<>();
    static File waystoneFile;
    WaystonePlus instance;

    /**
     * Initialize the waystone handler, this will use the "waystones.json" file to store waystones
     * @param instance The instance of the plugin
     */
    @SuppressWarnings("UnstableApiUsage")
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
                Type setType = new TypeToken<HashSet<Pair<String, SimpleLocation>>>() {
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

    /**
     * Save the waystones to the waystone file
     */
    public static void saveWaystones() {
        Bukkit.getScheduler().runTaskAsynchronously(WaystonePlus.getInstance(), () -> {
            try {
                FileUtils.writeStringToFile(waystoneFile, WaystonePlus.getInstance().getGson().toJson(cachedWaystones), Charset.defaultCharset());
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to save waystones!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Add a waystone to the cached waystones
     * @param block The block to add
     */
    public static void addWaystone(Block block) {
        if (!new NBTCustomBlock(block).getData().hasTag("waystone")) return;
        Pair<String, SimpleLocation> waystone = getWaystoneNameAndLocation(block);
        assert waystone != null;
        if (cachedWaystones.add(new Pair<>(waystone.first, waystone.second))) saveWaystones();
    }

    /**
     * Remove a waystone from the cached waystones
     * @param block The block to remove
     */
    public static void removeWaystone(Block block) {
        Pair<String, SimpleLocation> waystone = getWaystoneNameAndLocation(block);
        assert waystone != null;
        if (cachedWaystones.remove(new Pair<>(waystone.first, waystone.second))) saveWaystones();
    }

    /**
     * Check if a block is a waystone
     * @param block The block to check
     * @return If the block is a waystone
     */
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

    /**
     * Check if a location is a waystone
     * @param location The location to check
     * @return If the location is a waystone
     */
    public static boolean isWaystone(SimpleLocation location) {
        Block block = location.getBukkitLocation().getBlock();
        return isWaystone(block);
    }

    /**
     * Check if a location is a waystone
     * @param world The world
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return If the location is a waystone
     */
    public static boolean isWaystone(String world, long x, long y, long z) {
        SimpleLocation location = new SimpleLocation(world, x, y, z);
        Block block = location.getBukkitLocation().getBlock();

        return isWaystone(block);
    }

    /**
     * Get the waystone name and location of a block
     * @param block The block to get the waystone name and location of
     * @return The waystone name and location
     */
    private static Pair<String, SimpleLocation> getWaystoneNameAndLocation(Block block) {
        if (!new NBTCustomBlock(block).getData().hasTag("waystone")) return null;

        WaystoneNBT nbt = WaystoneNBT.fromString(new NBTCustomBlock(block).getData().getString("waystone"));
        return new Pair<>(nbt.waystoneName, new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    /**
     * Get the waystone name of a block
     * @param block The block to get the waystone name of
     * @return The waystone name
     */
    public static String getWaystoneName(Block block) {
        return Objects.requireNonNull(getWaystoneNameAndLocation(block)).first;
    }

    /**
     * Get the waystone name of a location
     * @param player The player to get the waystone name of
     * @param waystoneName The name of the waystone
     * @param waystoneLocation The location of the waystone
     */
    public static void teleportToWaystone(Player player, String waystoneName, SimpleLocation waystoneLocation) {
        WaystoneConfig waystoneConfig = WaystonePlus.getInstance().getWaystoneConfig();
        if (!checkCooldown(player))
            return;
        setCooldown(player);

        switch (waystoneConfig.teleportCostType) {
            case NONE -> {
                player.sendActionBar(Component.text("Teleporting to " + waystoneName, NamedTextColor.GREEN));
            }
            case XP -> {
                if (player.getLevel() < waystoneConfig.teleportCostLevels) {
                    player.sendMessage(Component.text("You need at least " + waystoneConfig.teleportCostLevels + " levels to teleport!", NamedTextColor.RED));
                    return;
                }

                player.setLevel(player.getLevel() - waystoneConfig.teleportCostLevels);
                player.sendActionBar(
                        Component
                                .text("Teleporting to " + waystoneName, NamedTextColor.GREEN)
                                .append(Component.text(" (Cost: " + waystoneConfig.teleportCostLevels + " levels)", NamedTextColor.GRAY))
                );
            }
            case ITEM -> {
                ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(waystoneConfig.teleportCostItemType)), waystoneConfig.teleportCostItemAmount);

                if (!player.getInventory().containsAtLeast(item, waystoneConfig.teleportCostItemAmount)) {
                    player.sendMessage(Component.text("You need at least " + waystoneConfig.teleportCostItemAmount + " " + item.getType().name() + " to teleport!", NamedTextColor.RED));
                    return;
                }

                player.getInventory().removeItem(item);
                player.sendActionBar(
                        Component
                                .text("Teleporting to " + waystoneName, NamedTextColor.GREEN)
                                .append(Component.text(" (Cost: " + waystoneConfig.teleportCostItemAmount + " " + item.getType().name() + ")", NamedTextColor.GRAY))
                );
            }
        }

        player.teleport(waystoneLocation.getBukkitLocation().add(0.5, 1.0, 0.5));
    }

    /**
     * Check if the player is on cooldown
     * @param player The player to check
     * @return If the player is on cooldown
     */
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
