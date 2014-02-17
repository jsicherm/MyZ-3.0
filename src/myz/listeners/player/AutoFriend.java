/**
 * 
 */
package myz.listeners.player;

import java.util.List;

import myz.support.interfacing.Configuration;
import myz.utilities.Utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author Jordan
 * 
 */
public class AutoFriend implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onSneak(PlayerToggleSneakEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.isSneaking() && (Boolean) Configuration.getConfig(Configuration.AUTOFRIEND))
			Utils.sneakAddFriend(e.getPlayer());
	}
}
