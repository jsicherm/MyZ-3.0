/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.Scheduling.Sync;
import myz.Support.Messenger;

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
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getFrom().distance(e.getTo()) >= 0.2 && Sync.getSafeLogoutPlayers().containsKey(e.getPlayer().getName())) {
			Sync.removeSafeLogoutPlayer(e.getPlayer());
			Messenger.sendConfigMessage(e.getPlayer(), "safe_logout.cancelled");
		}
	}
}
