package xyz.reportcards.waystoneplus.waystones;

import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.configuration.WaystoneConfig;
import xyz.reportcards.waystoneplus.listeners.CommandListener;
import xyz.reportcards.waystoneplus.utils.Pair;
import xyz.reportcards.waystoneplus.utils.SimpleLocation;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.*;

/**
 * Class to help with waystone items, and the waystone book
 */
public class WaystoneHelper {

    private static final String WAYSTONE_NBT_TAG = "waystone";

    /**
     * Create a new waystone item with the appropriate meta, and NBT data
     * @return The waystone item
     */
    public static ItemStack createWaystoneItem() {
        ItemStack stack = new ItemStack(Material.LODESTONE);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("Waystone", NamedTextColor.LIGHT_PURPLE));
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        NBT.modify(stack, nbt -> {
            nbt.setBoolean(WAYSTONE_NBT_TAG, true);
        });
        return stack;
    }

    /**
     * Check if an item is a waystone item
     * @param itemStack The item to check
     * @return Whether the item is a waystone item
     */
    public static boolean isWaystoneItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return NBT.get(itemStack, nbt -> {
            return nbt.getBoolean(WAYSTONE_NBT_TAG);
        });
    }

    /**
     * Get the waystone NBT data from a block
     * @param location The location of the block
     * @return The waystone NBT data
     */
    public static WaystoneNBT getWaystoneNBT(SimpleLocation location) {
        return WaystoneNBT.fromString(new NBTCustomBlock(location.getBukkitLocation().getBlock()).getData().getString(WAYSTONE_NBT_TAG));
    }

    /**
     * Open the waystone book for a player
     * @param player The player to open the book for
     * @param clickedWaystoneLocation The location of the clicked waystone
     */
    public static void openWaystoneBook(Player player, SimpleLocation clickedWaystoneLocation) {
        Map<SimpleLocation, Double> waystoneDistances = calculateDistancesToWaystones(clickedWaystoneLocation);
        List<SimpleLocation> sortedWaystoneLocations = sortWaystonesByDistance(waystoneDistances);

        Book.Builder bookBuilder = Book.builder().title(Component.text("Waystone"));
        List<Component> pages = buildWaystoneBookPages(sortedWaystoneLocations, waystoneDistances, clickedWaystoneLocation);
        bookBuilder.pages(pages);

        player.openBook(bookBuilder);
    }

    /**
     * Calculate the distances to all waystones from a clicked waystone
     * @param clickedWaystoneLocation The location of the clicked waystone
     * @return A map of waystone locations to their distances
     */
    private static Map<SimpleLocation, Double> calculateDistancesToWaystones(SimpleLocation clickedWaystoneLocation) {
        Map<SimpleLocation, Double> waystoneDistances = new HashMap<>();
        for (Pair<String, SimpleLocation> waystone : WaystoneHandler.cachedWaystones) {
            double distance = clickedWaystoneLocation.distance(waystone.second);
            if (distance >= WaystonePlus.getInstance().getWaystoneConfig().maxTeleportDistance)
                continue;

            waystoneDistances.put(waystone.second, distance);
        }
        return waystoneDistances;
    }

    /**
     * Sort waystones by distance
     * @param waystoneDistances The map of waystone locations to their distances
     * @return A list of waystone locations sorted by distance
     */
    private static List<SimpleLocation> sortWaystonesByDistance(Map<SimpleLocation, Double> waystoneDistances) {
        List<SimpleLocation> sortedWaystoneLocations = new ArrayList<>(waystoneDistances.keySet());
        sortedWaystoneLocations.sort(Comparator.comparingDouble(waystoneDistances::get));
        WaystoneConfig config = WaystonePlus.getInstance().getWaystoneConfig();
        return sortedWaystoneLocations;
    }

    /**
     * Build the pages of the waystone book
     * @param sortedWaystoneLocations The list of waystone locations sorted by distance
     * @param waystoneDistances The map of waystone locations to their distances
     * @param clickedWaystoneLocation The location of the clicked waystone
     * @return A list of pages for the waystone book
     */
    private static List<Component> buildWaystoneBookPages(
            List<SimpleLocation> sortedWaystoneLocations,
            Map<SimpleLocation, Double> waystoneDistances,
            SimpleLocation clickedWaystoneLocation
    ) {
        int pageSize = 14;
        List<Component> pages = new ArrayList<>();

        StringBuilder currentPage = new StringBuilder();
        int currentPageSize = 0;

        for (SimpleLocation waystoneLocation : sortedWaystoneLocations) {
            String waystoneString = getWaystoneDisplayString(
                    waystoneLocation,
                    clickedWaystoneLocation,
                    waystoneDistances.get(waystoneLocation),
                    getWaystoneNBT(waystoneLocation).waystoneName
            );

            if (currentPageSize >= pageSize) {
                pages.add(MiniMessage.miniMessage().deserialize(currentPage.toString()));
                currentPage = new StringBuilder();
                currentPageSize = 0;
            }

            currentPage.append(waystoneString).append("\n");
            currentPageSize++;
        }

        if (currentPageSize > 0) {
            pages.add(MiniMessage.miniMessage().deserialize(currentPage.toString()));
        }

        return pages;
    }

    /**
     * Get the display string for a waystone
     * @param waystoneLocation The location of the waystone
     * @param clickedWaystoneLocation The location of the clicked waystone
     * @param distance The distance to the waystone
     * @param clickedWaystoneName The name of the clicked waystone
     * @return The display string for the waystone
     */
    private static String getWaystoneDisplayString(SimpleLocation waystoneLocation, SimpleLocation clickedWaystoneLocation,
                                                   double distance, String clickedWaystoneName) {
        boolean isClickedWaystone = waystoneLocation.equals(clickedWaystoneLocation);
        String name = isClickedWaystone ? clickedWaystoneName : getWaystoneNBT(waystoneLocation).waystoneName;

        if (isClickedWaystone) { // Ignore distance for clicked waystone, as well as make it unclickable
            return "<yellow>[<gold>0m<yellow>] <black>" + name;
        }

        long distanceRounded = Math.round(distance);
        String serializedData = new CommandListener.WaystoneCommandData(
                name,
                clickedWaystoneLocation.toString(),
                waystoneLocation.toString()
        ).serialize();
        return "<yellow>[<gold>" + distanceRounded + "m<yellow>] <black><click:run_command:'/waystoneplus:waystone " + serializedData + "'>" + name + "</click>";

    }


}
