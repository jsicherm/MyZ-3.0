/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Support.PlayerData;

import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Jordan
 * 
 */
public class JoinQuit implements Listener {

	@EventHandler
	private void onPreJoin(AsyncPlayerPreLoginEvent e) {
		if (Configuration.usePreLogin()) {
			String name = e.getName();
			PlayerData data = PlayerData.getDataFor(name);
			/*
			 * Check if the player is still banned against the playerdata and sql.
			 */
			long now = System.currentTimeMillis();
			long timeOfKickExpiry;
			if (data != null
					&& (timeOfKickExpiry = data.getTimeOfKickban() + Configuration.getKickBanSeconds() * 1000) >= now
					|| MyZ.instance.getSQLManager().isConnected()
					&& (timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(name, "timeOfKickban") + Configuration.getKickBanSeconds()
							* 1000) >= now)
				e.disallow(Result.KICK_OTHER, Messenger.getConfigMessage("kick.recur", (timeOfKickExpiry - now) / 1000));
		}
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		MyZ.instance.addPlayer(e.getPlayer());
		e.setJoinMessage(null);
	}

	@EventHandler
	private void onLeave(PlayerQuitEvent e) {
		if (MyZ.instance.removePlayer(e.getPlayer())) {
			e.setQuitMessage(null);

			if (e.getPlayer().getVehicle() != null) {
				e.getPlayer().getVehicle().eject();
			}
			
			// Get rid of our horse.
			for (Horse horse : e.getPlayer().getWorld().getEntitiesByClass(Horse.class))
				if (horse.getOwner() != null && horse.getOwner().getName() != null
						&& horse.getOwner().getName().equals(e.getPlayer().getName())) {
					horse.setOwner(null);
					horse.setTamed(false);
					horse.setDomestication(0);
				}
		}
	}
}
