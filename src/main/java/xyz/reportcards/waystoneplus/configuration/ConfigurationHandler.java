package xyz.reportcards.waystoneplus.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.reportcards.waystoneplus.WaystonePlus;

public class ConfigurationHandler {

    WaystonePlus instance;
    FileConfiguration config;
    public ConfigurationHandler(WaystonePlus instance) {
        this.instance = instance;
        instance.saveDefaultConfig();
        config = instance.getConfig();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, T expectedType) throws ClassCastException {
        Object value = config.get(path);
        if (value == null) {
            return null;
        }

        if (value.getClass() != expectedType.getClass()) {
            throw new ClassCastException("Expected type " + expectedType.getClass().getName() + " but got " + value.getClass().getName() + " for path " + path);
        }

        return (T) value;
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

}
