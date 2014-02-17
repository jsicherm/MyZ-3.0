/**
 * 
 */
package myz.listeners.player;

import java.util.List;

import me.libraryaddict.disguise.events.UndisguiseEvent;
import myz.support.interfacing.Configuration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Jordan
 * 
 */
public class LibsUndisguiseListener implements Listener {

	@EventHandler
	private void onUndisguise(UndisguiseEvent e) {
		if (((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;

		e.setCancelled(!Configuration.isInLobby(e.getEntity().getLocation()));
	}
}
