package xyz.reportcards.waystoneplus.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.reportcards.waystoneplus.listeners.CommandListener;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.*;

public class WaystoneHelper {

    private static final String WAYSTONE_NBT_TAG = "waystone";

    public static ItemStack createWaystoneItem() {
        ItemStack stack = new ItemStack(Material.LODESTONE);
        NBT.modify(stack, nbt -> {
            nbt.setBoolean(WAYSTONE_NBT_TAG, true);
        });
        return stack;
    }

    public static boolean isWaystoneItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return NBT.get(itemStack, nbt -> {
            return nbt.getBoolean(WAYSTONE_NBT_TAG);
        });
    }

    public static WaystoneNBT getWaystoneNBT(SimpleLocation location) {
        return WaystoneNBT.fromString(new NBTCustomBlock(location.getBukkitLocation().getBlock()).getData().getString(WAYSTONE_NBT_TAG));
    }

    public static void openWaystoneBook(Player player, SimpleLocation clickedWaystoneLocation) {
        Map<SimpleLocation, Double> waystoneDistances = calculateDistancesToWaystones(clickedWaystoneLocation);
        List<SimpleLocation> sortedWaystoneLocations = sortWaystonesByDistance(waystoneDistances);

        Book.Builder bookBuilder = Book.builder().title(Component.text("Waystone"));
        List<Component> pages = buildWaystoneBookPages(sortedWaystoneLocations, waystoneDistances, clickedWaystoneLocation);
        bookBuilder.pages(pages);

        player.openBook(bookBuilder);
    }

    private static Map<SimpleLocation, Double> calculateDistancesToWaystones(SimpleLocation clickedWaystoneLocation) {
        Map<SimpleLocation, Double> waystoneDistances = new HashMap<>();
        for (Pair<String, SimpleLocation> waystone : WaystoneHandler.cachedWaystones) {
            waystoneDistances.put(waystone.second, clickedWaystoneLocation.distance(waystone.second));
        }
        return waystoneDistances;
    }

    private static List<SimpleLocation> sortWaystonesByDistance(Map<SimpleLocation, Double> waystoneDistances) {
        List<SimpleLocation> sortedWaystoneLocations = new ArrayList<>(waystoneDistances.keySet());
        sortedWaystoneLocations.sort(Comparator.comparingDouble(waystoneDistances::get));
        return sortedWaystoneLocations;
    }

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
