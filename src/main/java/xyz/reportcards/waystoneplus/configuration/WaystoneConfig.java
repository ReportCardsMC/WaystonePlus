package xyz.reportcards.waystoneplus.configuration;

import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WaystoneConfig {

    @ConfigValue(key = "waystones.max-teleport-distance")
    public int maxTeleportDistance;
    @ConfigValue(key = "waystones.teleport-cooldown")
    public int teleportCooldown;
    @ConfigValue(key = "waystones.teleport-cost.type")
    public CostType teleportCostType;
    @ConfigValue(key = "waystones.teleport-cost.levels")
    public int teleportCostLevels;
    @ConfigValue(key = "waystones.teleport-cost.item.type")
    public String teleportCostItemType;
    @ConfigValue(key = "waystones.teleport-cost.item.amount")
    public int teleportCostItemAmount;
    @ConfigValue(key = "waystones.teleport-sound")
    public String teleportSound;
    @ConfigValue(key = "waystones.names")
    public NameGeneration nameGeneration;
    @ConfigValue(key = "crafting.enabled")
    public boolean craftingEnabled;
    @ConfigValue(key = "crafting.recipe.type")
    public RecipeType recipeType; // Assuming this is still needed
    public String[] craftingRecipeShape; // New field to handle shape
    public HashMap<String, String> craftingRecipeIngredients = new HashMap<>();

    public WaystoneConfig(FileConfiguration from) {
        // Load values from the provided FileConfiguration
        // Use the ConfigValue annotation to map fields to keys
        HashMap<String, Field> fields = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (annotation != null) {
                fields.put(annotation.key(), field);
            }
        }

        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            String key = entry.getKey();
            Field field = entry.getValue();
            if (from.contains(key)) {
                try {
//                    field.set(this, from.get(key));
                    // Check if enum
                    if (field.getType().isEnum()) {
                        try {
                            field.set(this, Enum.valueOf((Class<Enum>) field.getType(), Objects.requireNonNull(from.getString(key, "")).toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid value for " + key + ": " + from.getString(key) + " (expected: " + field.getType().getSimpleName() + ")");
                        }
                    } else {
                        field.set(this, from.get(key));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // Handle shape & ingredients
        if (from.contains("crafting.recipe.shape")) {
            craftingRecipeShape = from.getStringList("crafting.recipe.shape").toArray(new String[0]);
        }

        if (from.contains("crafting.recipe.ingredients")) {
            Map<String, Object> ingredients = from.getConfigurationSection("crafting.recipe.ingredients").getValues(false);
            for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
                craftingRecipeIngredients.put(entry.getKey(), entry.getValue().toString());
            }
        }

        if (this.craftingEnabled && (this.craftingRecipeShape == null || this.craftingRecipeIngredients.isEmpty())) {
            craftingEnabled = false;
            throw new IllegalStateException("Crafting is enabled but the recipe is not properly configured!");
        }
    }

    // Enums (unchanged)
    public enum NameGeneration {WORDS, NUMBERS, LETTERS}

    public enum CostType {NONE, XP, ITEM}

    public enum RecipeType {SHAPED, SHAPELESS}
}