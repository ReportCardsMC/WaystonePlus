package xyz.reportcards.waystoneplus.utils.nbt;

import org.bukkit.NamespacedKey;
import xyz.reportcards.waystoneplus.WaystonePlus;

public interface NBTCustom {

    NamespacedKey OLD_KEY = new NamespacedKey(WaystonePlus.getInstance(), "custom-nbt");

    void deleteCustomNBT();

}