package xyz.reportcards.waystoneplus.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.*;

public class WaystoneHelper {

    public static ItemStack getWaystoneItem() {
        ItemStack stack = new ItemStack(Material.LODESTONE);
        NBT.modify(stack, nbt -> {
            nbt.setBoolean("waystone", true);
        });
        return stack;
    }

    public static boolean isWaystoneItem(ItemStack itemUsed) {
        if (itemUsed == null) return false;
        return NBT.get(itemUsed, nbt -> {
            return nbt.hasTag("waystone");
        });
    }

    public static WaystoneNBT getWaystoneNBT(SimpleLocation at) {
        return WaystoneNBT.fromString(new NBTCustomBlock(at.getBukkitLocation().getBlock()).getData().getString("waystone"));
    }

    public static void openWaystoneBook(Player player, SimpleLocation clickedWaystone) {
        String clickedWaystoneName = getWaystoneNBT(clickedWaystone).waystoneName;

        Map<SimpleLocation, Long> distances = new HashMap<>();
        for (SimpleLocation cachedWaystone : WaystoneHandler.cachedWaystones) {
            if (!Objects.equals(cachedWaystone.world, clickedWaystone.world)) continue;
            distances.put(cachedWaystone, (long) clickedWaystone.distance(cachedWaystone));
        }

        List<SimpleLocation> sortedWaystones = new ArrayList<>(distances.keySet());
        sortedWaystones.sort((o1, o2) -> (int) (distances.get(o1) - distances.get(o2)));

        Book.Builder bookBuilder = Book.builder();
        bookBuilder.title(Component.text("Waystone"));

        int pageSize = 14;
        List<Component> pages = new ArrayList<>();

        StringBuilder currentPage = new StringBuilder();
        int currentPageSize = 0;
        for (SimpleLocation cachedWaystone : sortedWaystones) {
            if (currentPageSize >= pageSize) {
                pages.add(MiniMessage.miniMessage().deserialize(currentPage.toString()));
                currentPage = new StringBuilder();
                currentPageSize = 0;
            }

            currentPage.append(getWaystoneString(player, cachedWaystone, clickedWaystone, distances.get(cachedWaystone), clickedWaystoneName)).append("\n");
            currentPageSize++;
        }

        if (currentPageSize > 0) {
            pages.add(MiniMessage.miniMessage().deserialize(currentPage.toString()));
        }

        bookBuilder.pages(pages);
        player.openBook(bookBuilder);
    }

    private static String getWaystoneString(Player player, SimpleLocation waystone, SimpleLocation clickedWaystone, Long distance, String clickedWaystonName) {
        if (clickedWaystone == waystone) {
            return "<yellow>[<gold>%sm<yellow>] <black>%s".formatted(distance, clickedWaystonName);
        }

        String name = getWaystoneNBT(waystone).waystoneName;
        return "<yellow>[<gold>%sm<yellow>] <black>%s".formatted(distance, name);
    }
}
