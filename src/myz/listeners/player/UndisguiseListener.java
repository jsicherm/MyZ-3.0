/**
 * 
 */
package myz.listeners.player;

import myz.MyZ;
import myz.support.interfacing.Configuration;

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
		if (MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;

		e.setCancelled(!Configuration.isInLobby(e.getPlayer()));
	}
}
