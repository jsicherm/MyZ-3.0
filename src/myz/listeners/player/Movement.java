/**
 * 
 */
package myz.listeners.player;

import java.util.List;

import myz.scheduling.Sync;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;

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
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (Sync.getSafeLogoutPlayers().containsKey(e.getPlayer().getUniqueId()) && e.getFrom().distance(e.getTo()) >= 0.1) {
			Sync.removeSafeLogoutPlayer(e.getPlayer());
			Messenger.sendConfigMessage(e.getPlayer(), "safe_logout.cancelled");
		}
	}
}
