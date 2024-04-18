package xyz.reportcards.waystoneplus;

import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.listeners.CommandListener;
import xyz.reportcards.waystoneplus.listeners.PlayerListener;
import xyz.reportcards.waystoneplus.utils.WaystoneHandler;
import xyz.reportcards.waystoneplus.utils.WaystoneHelper;
import xyz.reportcards.waystoneplus.utils.nbt.APIUtils;

import java.util.HashMap;
import java.util.Map;

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

        // Handle recipe
        if (waystoneConfig.craftingEnabled) {
            if (waystoneConfig.recipeType == WaystoneConfig.RecipeType.SHAPELESS) {
                ShapelessRecipe recipe = new ShapelessRecipe(NamespacedKey.fromString("waystoneplus:waystone", this), WaystoneHelper.createWaystoneItem());
                HashMap<Character, Integer> recipeMap = new HashMap<>();
                for (String s : waystoneConfig.craftingRecipeShape) {
                    for (char c : s.toCharArray()) {
                        if (c == ' ') {
                            continue;
                        }
                        recipeMap.put(c, recipeMap.getOrDefault(c, 0) + 1);
                    }
                }

                HashMap<Character, ItemStack> ingredients = new HashMap<>();
                for (Map.Entry<String, String> entry : waystoneConfig.craftingRecipeIngredients.entrySet()) {
                    try {
                        Material material = Material.getMaterial(entry.getValue());
                        if (material == null)
                            throw new IllegalArgumentException("Invalid material: " + entry.getValue());

                        ingredients.put(entry.getKey().charAt(0), new ItemStack(material));
                    } catch (IllegalArgumentException e) {
                        getLogger().severe("Invalid block data: " + entry.getValue());
                        return;
                    }

                }

                for (char c : recipeMap.keySet()) {
                    if (!ingredients.containsKey(c)) {
                        getLogger().severe("Missing ingredient for character: " + c);
                        return;
                    }

                    recipe.addIngredient(recipeMap.get(c), ingredients.get(c));
                }

                Bukkit.addRecipe(recipe);
            } else {
                // This is shaped
                ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.fromString("waystoneplus:waystone", this), WaystoneHelper.createWaystoneItem());
                recipe.shape(waystoneConfig.craftingRecipeShape);

                for (Map.Entry<String, String> entry : waystoneConfig.craftingRecipeIngredients.entrySet()) {
                    try {
                        Material material = Material.getMaterial(entry.getValue());
                        if (material == null)
                            throw new IllegalArgumentException("Invalid material: " + entry.getValue());

                        recipe.setIngredient(entry.getKey().charAt(0), material);
                    } catch (IllegalArgumentException e) {
                        getLogger().severe("Invalid block data: " + entry.getValue());
                        return;
                    }
                }

                Bukkit.addRecipe(recipe);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
