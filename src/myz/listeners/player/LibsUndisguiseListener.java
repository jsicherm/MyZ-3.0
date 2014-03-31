/**
 * 
 */
package myz.listeners.player;

import me.libraryaddict.disguise.events.UndisguiseEvent;
import myz.support.interfacing.Configuration;
import myz.utilities.Validate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Jordan
 * 
 */
public class LibsUndisguiseListener implements Listener {

	@EventHandler
	private void onUndisguise(UndisguiseEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;

		e.setCancelled(!Configuration.isInLobby(e.getEntity().getLocation()));
	}
}
