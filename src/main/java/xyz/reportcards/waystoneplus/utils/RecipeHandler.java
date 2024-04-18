package xyz.reportcards.waystoneplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandler {

    /**
     * Register the waystone recipe
     */
    public static void registerRecipe() {
        WaystonePlus plugin = WaystonePlus.getInstance();
        WaystoneConfig waystoneConfig = plugin.getWaystoneConfig();
        if (waystoneConfig.craftingEnabled) {
            if (waystoneConfig.recipeType == WaystoneConfig.RecipeType.SHAPELESS) {
                registerShapeless(plugin, waystoneConfig);
            } else {
                registerShaped(plugin, waystoneConfig);
            }
        }
    }

    private static void registerShapeless(WaystonePlus plugin, WaystoneConfig waystoneConfig) {

        ShapelessRecipe recipe = new ShapelessRecipe(NamespacedKey.fromString("waystoneplus:waystone", plugin), WaystoneHelper.createWaystoneItem());

        // Create a map of how much of each ingredient is needed
        HashMap<Character, Integer> recipeMap = new HashMap<>();
        for (String s : waystoneConfig.craftingRecipeShape) {
            for (char c : s.toCharArray()) {
                if (c == ' ') {
                    continue;
                }
                recipeMap.put(c, recipeMap.getOrDefault(c, 0) + 1);
            }
        }

        // Register each ingredient
        HashMap<Character, ItemStack> ingredients = new HashMap<>();
        for (Map.Entry<String, String> entry : waystoneConfig.craftingRecipeIngredients.entrySet()) {
            try {
                Material material = Material.getMaterial(entry.getValue());
                if (material == null) // If the material is invalid, throw an exception
                    throw new IllegalArgumentException("Invalid material: " + entry.getValue());

                ingredients.put(entry.getKey().charAt(0), new ItemStack(material));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Invalid block data: " + entry.getValue());
                return;
            }

        }

        for (char c : recipeMap.keySet()) {
            // Check if there are any missing ingredients
            if (!ingredients.containsKey(c)) {
                plugin.getLogger().severe("Missing ingredient for character: " + c);
                return;
            }

            // Add the ingredient to the recipe
            recipe.addIngredient(recipeMap.get(c), ingredients.get(c));
        }

        Bukkit.addRecipe(recipe);
    }

    private static void registerShaped(WaystonePlus plugin, WaystoneConfig waystoneConfig) {
        ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.fromString("waystoneplus:waystone", plugin), WaystoneHelper.createWaystoneItem());
        recipe.shape(waystoneConfig.craftingRecipeShape);

        // Register each ingredient
        for (Map.Entry<String, String> entry : waystoneConfig.craftingRecipeIngredients.entrySet()) {
            try {
                // Get the material from the string
                Material material = Material.getMaterial(entry.getValue());
                if (material == null) // If the material is invalid, throw an exception
                    throw new IllegalArgumentException("Invalid material: " + entry.getValue());

                recipe.setIngredient(entry.getKey().charAt(0), material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Invalid block data: " + entry.getValue());
                return;
            }
        }

        Bukkit.addRecipe(recipe);
    }



}
