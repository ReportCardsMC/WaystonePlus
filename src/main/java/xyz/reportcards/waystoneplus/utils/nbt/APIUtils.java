package xyz.reportcards.waystoneplus.utils.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataHolder;

public class APIUtils {

    @SuppressWarnings("ConstantConditions")
    public static final boolean SUPPORTS_BLOCK_NBT = PersistentDataHolder.class.isAssignableFrom(Chunk.class);

    public static void addNBTToBlock(Block block, NBTCompound compound) {
        BlockState blockState = block.getState();
        if (blockState instanceof TileState tileState) {
            NBTCustomTileEntity nbtBlock = new NBTCustomTileEntity(tileState);
            nbtBlock.mergeCompound(compound);
            return;
        }
        if (SUPPORTS_BLOCK_NBT) {
            NBTCustomBlock nbtBlock = new NBTCustomBlock(block);
            nbtBlock.getData().mergeCompound(compound);
        } else {
            Bukkit.getLogger().severe("This server version does not support block NBT!");
        }
    }

}
