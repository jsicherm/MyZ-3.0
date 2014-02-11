package myz.support;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * @author Jordan
 * 
 */
public class Teleport {

	/**
	 * Teleport a player to a location without reserving the vehicle (if
	 * applicable).
	 * 
	 * @param player
	 *            The player.
	 * @param to
	 *            The location to teleport to.
	 */
	public static void teleport(Player player, Location to) {
		teleport(player, to, false);
	}

	/**
	 * Teleport a player to a location.
	 * 
	 * @param player
	 *            The player.
	 * @param to
	 *            The location to teleport to.
	 * @param keepVehicle
	 *            Whether or not to keep the vehicle.
	 */
	public static void teleport(Player player, Location to, boolean keepVehicle) {
		if (to == null || player == null)
			return;

		if (player.isInsideVehicle()) {
			// Eject the vehicle...
			Entity vehicle = player.getVehicle();
			vehicle.eject();
			// Teleport the player...
			player.teleport(to, TeleportCause.PLUGIN);
			// Remove the vehicle if it's not persisting.
			if (!keepVehicle)
				vehicle.remove();
			else {
				// Otherwise teleport the vehicle and remount.
				vehicle.teleport(to, TeleportCause.PLUGIN);
				vehicle.setPassenger(player);
			}
			return;
		}

		player.teleport(to);
	}
}
