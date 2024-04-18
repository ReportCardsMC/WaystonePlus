package xyz.reportcards.waystoneplus.utils.nbt;

import org.bukkit.Chunk;
import org.bukkit.persistence.PersistentDataHolder;

/**
 * A utility class for the NBT API
 */
public class APIUtils {

    /**
     * Whether the server supports block NBT data or not
     */
    @SuppressWarnings("ConstantConditions")
    public static final boolean SUPPORTS_BLOCK_NBT = PersistentDataHolder.class.isAssignableFrom(Chunk.class);

}
