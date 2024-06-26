package xyz.reportcards.waystoneplus;

import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.listeners.CommandListener;
import xyz.reportcards.waystoneplus.listeners.WaystoneListener;
import xyz.reportcards.waystoneplus.utils.RecipeHandler;
import xyz.reportcards.waystoneplus.waystones.WaystoneHandler;
import xyz.reportcards.waystoneplus.utils.nbt.APIUtils;

public final class WaystonePlus extends JavaPlugin {

    /**
     * The instance of the plugin
     */
    @Getter
    private WaystoneConfig waystoneConfig;
    /**
     * The instance of the waystone handler
     */
    @Getter
    private WaystoneHandler waystoneHandler;
    /**
     * The instance of the Gson object
     */
    @Getter
    private Gson gson;

    /**
     * Get the instance of the plugin
     *
     * @return The instance of the plugin
     */
    public static WaystonePlus getInstance() {
        return getPlugin(WaystonePlus.class);
    }

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        this.gson = new Gson();
        // Plugin startup logic
        if (!APIUtils.SUPPORTS_BLOCK_NBT) {
            getLogger().severe("This server version does not support block NBT!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new WaystoneListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);

        this.saveDefaultConfig();
        this.waystoneConfig = new WaystoneConfig(this.getConfig());
        this.waystoneHandler = new WaystoneHandler(this);

        RecipeHandler.registerRecipe();
    }
}
