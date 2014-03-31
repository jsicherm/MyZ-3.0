/**
 * 
 */
package myz.listeners.player;

import myz.support.interfacing.Configuration;
import myz.utilities.Validate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;

/**
 * @author Jordan
 * 
 */
public class UndisguiseListener implements Listener {

	@EventHandler
	private void onUndisguise(PlayerUndisguiseEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;

		e.setCancelled(!Configuration.isInLobby(e.getPlayer()));
	}
}
