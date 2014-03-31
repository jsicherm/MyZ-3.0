/**
 * 
 */
package myz.listeners.player;

import myz.scheduling.Sync;
import myz.support.interfacing.Messenger;
import myz.utilities.Validate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Jordan
 * 
 */
public class Movement implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onMove(PlayerMoveEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (Sync.getSafeLogoutPlayers().containsKey(e.getPlayer().getUniqueId()) && e.getFrom().distance(e.getTo()) >= 0.1) {
			Sync.removeSafeLogoutPlayer(e.getPlayer());
			Messenger.sendConfigMessage(e.getPlayer(), "safe_logout.cancelled");
		}
	}
}
