package xyz.reportcards.waystoneplus;

import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.listeners.CommandListener;
import xyz.reportcards.waystoneplus.listeners.PlayerListener;
import xyz.reportcards.waystoneplus.utils.WaystoneHandler;
import xyz.reportcards.waystoneplus.utils.nbt.APIUtils;

public final class WaystonePlus extends JavaPlugin {

    @Getter
    WaystoneConfig waystoneConfig;
    @Getter
    WaystoneHandler waystoneHandler;
    @Getter
    Gson gson;

    public static WaystonePlus getInstance() {
        return getPlugin(WaystonePlus.class);
    }

    @Override
    public void onEnable() {
        this.gson = new Gson();
        // Plugin startup logic
        if (!APIUtils.SUPPORTS_BLOCK_NBT) {
            getLogger().severe("This server version does not support block NBT!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);

        this.saveDefaultConfig();
        this.waystoneConfig = new WaystoneConfig(this.getConfig());

        this.waystoneHandler = new WaystoneHandler(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
