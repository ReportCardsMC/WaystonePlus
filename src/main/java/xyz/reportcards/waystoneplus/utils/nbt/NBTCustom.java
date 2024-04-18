package xyz.reportcards.waystoneplus.utils.nbt;

import org.bukkit.NamespacedKey;
import xyz.reportcards.waystoneplus.WaystonePlus;

/**
 * A custom NBT class for custom NBT data
 */
public interface NBTCustom {

    /**
     * The old key for the custom NBT data
     */
    NamespacedKey OLD_KEY = new NamespacedKey(WaystonePlus.getInstance(), "custom-nbt");

    /**
     * Delete the custom NBT data
     */
    void deleteCustomNBT();

}