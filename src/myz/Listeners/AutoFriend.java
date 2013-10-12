/**
 * 
 */
package myz.Listeners;

import myz.Support.Configuration;
import myz.Utilities.Utilities;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author Jordan
 * 
 */
public class AutoFriend implements Listener {

	@EventHandler
	private void onSneak(PlayerToggleSneakEvent e) {
		if (e.isSneaking() && Configuration.isAutofriend())
			Utilities.sneakAddFriend(e.getPlayer());
	}
}
