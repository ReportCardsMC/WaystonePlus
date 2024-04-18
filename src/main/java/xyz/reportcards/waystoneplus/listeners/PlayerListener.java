package xyz.reportcards.waystoneplus.listeners;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import xyz.reportcards.waystoneplus.WaystonePlus;
import xyz.reportcards.waystoneplus.utils.SimpleLocation;
import xyz.reportcards.waystoneplus.utils.WaystoneHandler;
import xyz.reportcards.waystoneplus.utils.WaystoneHelper;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.ArrayList;

public class PlayerListener implements Listener {

    private static final Material WAYSTONE_BLOCK = Material.LODESTONE;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Give player a waystone
        Player player = event.getPlayer();
        player.getInventory().addItem(WaystoneHelper.createWaystoneItem());
    }

    @EventHandler
    public void onWaystoneClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        assert block != null;

        if (!block.getType().equals(WAYSTONE_BLOCK))
            return;

        Player player = event.getPlayer();

        if (player.hasMetadata("clickedTick") && player.getMetadata("clickedTick").get(0).asLong() >= Bukkit.getCurrentTick()-2)
            return;
        player.setMetadata("clickedTick", new FixedMetadataValue(WaystonePlus.getInstance(), Bukkit.getCurrentTick()));

        boolean isLeftClick = (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR));
        if (isLeftClick && player.isSneaking())
            return;

        if (!WaystoneHandler.isWaystone(block))
            return;

        if (isLeftClick && !player.isSneaking()) {
            player.sendMessage(Component.text("To break a waystone, sneak and break it!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        WaystoneHelper.openWaystoneBook(player, SimpleLocation.fromBukkitLocation(block.getLocation()));
    }

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

    @EventHandler
    public void onWaystoneBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getType().equals(WAYSTONE_BLOCK)) return;

        Block block = event.getBlock();
        NBTCustomBlock nbtBlock = new NBTCustomBlock(block);
        if (!nbtBlock.getData().hasTag("waystone")) return;

        player.sendMessage(Component.text("Broke a waystone!", NamedTextColor.RED));
        WaystoneHandler.removeWaystone(block);
    }
    
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (!block.getType().equals(WAYSTONE_BLOCK)) continue;
            if (!new NBTCustomBlock(block).getData().hasTag("waystone")) continue;

            event.blockList().remove(block);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (!block.getType().equals(WAYSTONE_BLOCK)) continue;
            if (!new NBTCustomBlock(block).getData().hasTag("waystone")) continue;

            event.blockList().remove(block);
        }
    }

}
