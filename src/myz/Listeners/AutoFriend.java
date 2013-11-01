/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Utilities.Utilities;

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
		if(!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName())) { return; }
		if (e.isSneaking() && Configuration.isAutofriend())
			Utilities.sneakAddFriend(e.getPlayer());
	}
}
