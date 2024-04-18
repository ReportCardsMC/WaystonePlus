package xyz.reportcards.waystoneplus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import xyz.reportcards.waystoneplus.utils.SimpleLocation;
import xyz.reportcards.waystoneplus.utils.WaystoneHandler;
import xyz.reportcards.waystoneplus.utils.nbt.NBTCustomBlock;
import xyz.reportcards.waystoneplus.utils.nbt.models.WaystoneNBT;

import java.util.Base64;
import java.util.Objects;

/**
 * A listener for the waystone command, which is used to teleport to a waystone
 */
public class CommandListener implements Listener {

    /**
     * Handle the waystone command
     * @param event The event
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] command = event.getMessage().split(" ");

        if (command.length <= 1)
            return;

        if (!command[0].equalsIgnoreCase("/waystoneplus:waystone"))
            return;

        String serialized = command[1];
        WaystoneCommandData data = WaystoneCommandData.deserialize(serialized);
        SimpleLocation clickedLocation = SimpleLocation.fromString(data.clickedWaystoneLocation);
        SimpleLocation waystoneLocation = SimpleLocation.fromString(data.waystoneLocation);
        String waystoneName = data.waystoneName;

        if (player.getLocation().distance(clickedLocation.getBukkitLocation()) > 5)
            return;

        WaystoneNBT nbt = WaystoneNBT.fromString(new NBTCustomBlock(waystoneLocation.getBukkitLocation().getBlock()).getData().getString("waystone"));
        if (nbt == null)
            return;
        if (!Objects.equals(nbt.waystoneName, waystoneName))
            return;

        WaystoneHandler.teleportToWaystone(player, waystoneName, waystoneLocation);
        event.setCancelled(true);
    }

    /**
     * Data class for the waystone command
     *
     * @param waystoneName The name of the waystone
     * @param clickedWaystoneLocation The location of the clicked waystone
     * @param waystoneLocation The location of the waystone
     */
    public record WaystoneCommandData(
            String waystoneName,
            String clickedWaystoneLocation,
            String waystoneLocation
    ) {
        /**
         * Deserialize the data from a serialized string (Base64)
         * @param serialized The serialized string
         * @return The deserialized data
         */
        public static WaystoneCommandData deserialize(String serialized) {
            String decoded = new String(Base64.getDecoder().decode(serialized));
            String[] split = decoded.split(":");
            return new WaystoneCommandData(split[0], split[1], split[2]);
        }

        /**
         * @return The Base64 serialized data
         */
        public String serialize() {
            String serialized = waystoneName + ":" + clickedWaystoneLocation + ":" + waystoneLocation;
            return Base64.getEncoder().encodeToString(serialized.getBytes());
        }
    }

}
