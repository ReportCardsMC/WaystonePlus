package xyz.reportcards.waystoneplus.utils.nbt;

import de.tr7zw.changeme.nbtapi.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NBTCustomTileEntity extends NBTTileEntity implements NBTCustom {

    private final BlockState blockState;
    private final String KEY = "skbee-custom";

    /**
     * @param tile BlockState from any TileEntity
     */
    public NBTCustomTileEntity(BlockState tile) {
        super(tile);
        this.blockState = tile;
        convert();
    }

    /**
     * Delete the custom NBT data
     */
    @Override
    public void deleteCustomNBT() {
        getPersistentDataContainer().removeKey(KEY);
    }

    /**
     * Get the NBTCompound of this {@link BlockState}
     * @param name The name of the compound
     * @return NBTCompound of this block
     */
    @Override
    public NBTCompound getOrCreateCompound(String name) {
        if (name.equals("custom")) {
            return getPersistentDataContainer().getOrCreateCompound(KEY);
        }
        try {
            return super.getOrCreateCompound(name);
        } catch (NbtApiException ignore) {
            return null;
        }
    }

    /**
     * Get the NBTCompound of this {@link BlockState}
     * @param name The name of the compound
     * @return NBTCompound of this block
     */
    @Override
    public NBTCompound getCompound(String name) {
        if (name.equals("custom")) {
            return getPersistentDataContainer().getOrCreateCompound(KEY);
        }
        return super.getCompound(name);
    }

    /**
     * Check if the NBTCompound has a tag
     * @param key String key
     * @return true if the tag exists, false otherwise
     */
    @Override
    public boolean hasTag(String key) {
        if (key.equalsIgnoreCase("custom")) {
            return true;
        }
        return super.hasTag(key);
    }

    /**
     * Merge a compound into this compound
     * @param comp The compound to merge
     */
    @Override
    public void mergeCompound(NBTCompound comp) {
        super.mergeCompound(comp);
        if (comp.hasTag("custom")) {
            NBTCompound custom = comp.getOrCreateCompound("custom");
            NBTCompound customNBT = getPersistentDataContainer().getOrCreateCompound(KEY);
            customNBT.mergeCompound(custom);
        }
    }

    /**
     * Get the type of the NBT tag
     * @param name The name of the tag
     * @return The type of the tag
     */
    @Override
    public NBTType getType(String name) {
        if (name.equalsIgnoreCase("custom")) {
            return NBTType.NBTTagCompound;
        }
        return super.getType(name);
    }

    /**
     * Get the NBTCompound of this {@link BlockState}
     * @return NBTCompound of this block
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public String toString() {
        try {
            String bukkit = "PublicBukkitValues";
            NBTCompound compound = new NBTContainer(new NBTTileEntity(blockState).toString());
            NBTCompound custom = null;
            if (compound.hasTag(bukkit)) {
                NBTCompound persist = compound.getCompound(bukkit);
                assert persist != null;
                persist.removeKey("__nbtapi"); // this is just a placeholder one, so we dont need it
                if (persist.hasTag(KEY)) {
                    custom = getPersistentDataContainer().getCompound(KEY);
                    persist.removeKey(KEY);
                }
                if (persist.getKeys().isEmpty()) {
                    compound.removeKey(bukkit);
                }
            }
            NBTCompound customCompound = compound.getOrCreateCompound("custom");
            if (custom != null) {
                customCompound.mergeCompound(custom);
            }
            // For some reason block NBT doesn't show location in NBT-API (it does in vanilla MC)
            compound.setInteger("x", blockState.getX());
            compound.setInteger("y", blockState.getY());
            compound.setInteger("z", blockState.getZ());
            return compound.toString();
        } catch (NbtApiException ignore) {
            return null;
        }
    }

    /**
     * Convert the old NBT data to the new system
     */
    private void convert() {
        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
        if (container.has(OLD_KEY, PersistentDataType.STRING)) {
            String data = container.get(OLD_KEY, PersistentDataType.STRING);
            container.remove(OLD_KEY);
            if (data != null) {
                blockState.update();
                NBTCompound custom = getOrCreateCompound("custom");
                custom.mergeCompound(new NBTContainer(data));
            }
        }
    }

    /**
     * Save the compound to the block
     */
    @Override
    protected void saveCompound() {
        super.saveCompound();
        blockState.getBlock().getState().update();
    }
}