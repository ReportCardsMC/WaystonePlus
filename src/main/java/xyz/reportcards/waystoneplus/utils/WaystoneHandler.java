package xyz.reportcards.waystoneplus.utils;

import com.google.common.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class WaystoneHandler {

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
                Type setType = new TypeToken<HashSet<SimpleLocation>>(){}.getType();
                cachedWaystones = instance.getGson().fromJson(FileUtils.readFileToString(waystoneFile, Charset.defaultCharset()), setType);

                for (SimpleLocation cachedWaystone : cachedWaystones) {
                    Block block = cachedWaystone.getBukkitLocation().getBlock();
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

    static File waystoneFile;
    public static Set<SimpleLocation> cachedWaystones = new HashSet<>();

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

        SimpleLocation location = new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        if (cachedWaystones.add(location)) saveWaystones();
    }

    public static void removeWaystone(Block block) {
        if (!new NBTCustomBlock(block).getData().hasTag("waystone")) return;

        SimpleLocation location = new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        if (cachedWaystones.remove(location)) saveWaystones();
    }

    public static boolean isWaystone(Block block) {
        var nbtBlock = new NBTCustomBlock(block);
        SimpleLocation loc = new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        if (!nbtBlock.getData().hasTag("waystone")) {
            if (cachedWaystones.remove(loc)) saveWaystones();
            return false;
        }

        if (cachedWaystones.add(loc)) saveWaystones();
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

}
