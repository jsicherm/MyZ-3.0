/**
 * 
 */
package myz.listeners.player;

import myz.support.interfacing.Configuration;
import myz.utilities.Utils;
import myz.utilities.Validate;

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
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (e.isSneaking() && (Boolean) Configuration.getConfig(Configuration.AUTOFRIEND))
			Utils.sneakAddFriend(e.getPlayer());
	}
}
