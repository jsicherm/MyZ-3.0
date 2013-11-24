/**
 * 
 */
package myz.Listeners;

import me.libraryaddict.disguise.events.UndisguiseEvent;
import myz.MyZ;
import myz.Support.Configuration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Jordan
 * 
 */
public class LibsUndisguiseListener implements Listener {

	@EventHandler
	private void onUndisguise(UndisguiseEvent e) {
		if (MyZ.instance.getWorlds().contains(e.getEntity().getWorld().getName()))
			return;

		e.setCancelled(!Configuration.isInLobby(e.getEntity().getLocation()));
	}
}
