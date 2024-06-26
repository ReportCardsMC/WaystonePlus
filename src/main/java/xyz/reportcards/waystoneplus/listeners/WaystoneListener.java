package xyz.reportcards.waystoneplus.listeners;

import de.tr7zw.changeme.nbtapi.NBTBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.utils.SimpleLocation;
import xyz.reportcards.waystoneplus.waystones.WaystoneHandler;
import xyz.reportcards.waystoneplus.waystones.WaystoneHelper;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.ArrayList;

/**
 * A listener for the waystone, which is used to handle waystone events
 */
public class WaystoneListener implements Listener {

    private static final Material WAYSTONE_BLOCK = Material.LODESTONE;

    /**
     * Handle the waystone click event to open the waystone book GUI when a player clicks on a waystone
     * @param event The event
     */
    @EventHandler
    public void onWaystoneClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null || !block.getType().equals(WAYSTONE_BLOCK))
            return;

        Player player = event.getPlayer();

        if (player.hasMetadata("clickedTick") && player.getMetadata("clickedTick").get(0).asLong() >= Bukkit.getCurrentTick() - 2)
            return;
        player.setMetadata("clickedTick", new FixedMetadataValue(WaystonePlus.getInstance(), Bukkit.getCurrentTick()));

        boolean isLeftClick = (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR));
        if (isLeftClick && player.isSneaking())
            return;

        if (!WaystoneHandler.isWaystone(block))
            return;

        // Check if they're trying to break the block instead of opening the gui
        if (isLeftClick && !player.isSneaking()) {
            player.sendMessage(Component.text("To break a waystone, sneak and break it!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        WaystoneHelper.openWaystoneBook(player, SimpleLocation.fromBukkitLocation(block.getLocation()));
    }

    /**
     * Handle the waystone place event to add a waystone to the cache when a player places a waystone
     * @param event The event
     */
    @EventHandler
    public void onWaystonePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlockPlaced().getType().equals(WAYSTONE_BLOCK)) return;

        ItemStack itemUsed = event.getItemInHand();
        if (!WaystoneHelper.isWaystoneItem(itemUsed)) return;

        player.sendMessage(Component.text("Placed a waystone!", NamedTextColor.GREEN));
        NBTBlock block = new NBTBlock(event.getBlockPlaced());
        ArrayList<String> exclude = new ArrayList<>();
        for (var waystone : WaystoneHandler.cachedWaystones) {
            exclude.add(waystone.first);
        }
        block.getData().setString("waystone", new WaystoneNBT(exclude).toString());

        WaystoneHandler.addWaystone(event.getBlockPlaced());
    }

    /**
     * Handle the waystone break event to remove a waystone from the cache when a player breaks a waystone
     * @param event The event
     */
    @EventHandler
    public void onWaystoneBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getType().equals(WAYSTONE_BLOCK)) return;

        Block block = event.getBlock();
        NBTCustomBlock nbtBlock = new NBTCustomBlock(block);
        if (!nbtBlock.getData().hasTag("waystone")) return;

        player.sendMessage(Component.text("Broke a waystone!", NamedTextColor.RED));
        WaystoneHandler.removeWaystone(block);

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), WaystoneHelper.createWaystoneItem());
    }

    /**
     * Handle the block explode event to remove waystones from the cache when a waystone explodes
     * @param event The event
     */
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (!block.getType().equals(WAYSTONE_BLOCK)) continue;
            if (!new NBTCustomBlock(block).getData().hasTag("waystone")) continue;

            event.blockList().remove(block);
        }
    }

    /**
     * Handle the entity explode event to remove waystones from the cache when a waystone explodes
     * @param event The event
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (!block.getType().equals(WAYSTONE_BLOCK)) continue;
            if (!new NBTCustomBlock(block).getData().hasTag("waystone")) continue;

            event.blockList().remove(block);
        }
    }

}
