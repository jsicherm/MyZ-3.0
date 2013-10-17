/**
 * 
 */
package myz.Listeners;

import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.Utilities.Utilities;
import myz.mobs.CustomEntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPreJoin(AsyncPlayerPreLoginEvent e) {
		if (Configuration.usePreLogin()) {
			String name = e.getName();
			PlayerData data = PlayerData.getDataFor(name);
			/*
			 * Check if the player is still banned against the playerdata and sql.
			 */
			long now = System.currentTimeMillis();
			long timeOfKickExpiry = 0L;
			if (data != null) {
				timeOfKickExpiry = data.getTimeOfKickban() + Configuration.getKickBanSeconds() * 1000;
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(name, "timeOfKickban") + Configuration.getKickBanSeconds() * 1000;
			}

			if (timeOfKickExpiry >= now)
				e.disallow(Result.KICK_OTHER, Messenger.getConfigMessage("kick.recur", (timeOfKickExpiry - now) / 1000));
		}
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		MyZ.instance.addPlayer(e.getPlayer());

		// Ensure our NPC doesn't remain on when we log in.
		CustomEntityPlayer ourNPC = null;
		for (CustomEntityPlayer npc : MyZ.instance.getNPCs()) {
			if (npc.getName().equals(e.getPlayer().getName())) {
				ourNPC = npc;
				break;
			}
		}
		if (ourNPC != null) {
			ourNPC.getBukkitEntity().remove();
			MyZ.instance.getNPCs().remove(ourNPC);
		}

		e.setJoinMessage(null);

		// Update name colors for this player and all of their online friends.
		Utilities.colorName(e.getPlayer());

		List<String> friends = new ArrayList<String>();
		PlayerData data = PlayerData.getDataFor(e.getPlayer());
		if (data != null) {
			friends = data.getFriends();
		}
		if (MyZ.instance.getSQLManager().isConnected()) {
			friends = MyZ.instance.getSQLManager().getStringList(e.getPlayer().getName(), "friends");
		}
		for (String friend : friends) {
			Player online_friend = Bukkit.getPlayer(friend);
			if (online_friend != null && online_friend.isOnline()) {
				Utilities.colorName(online_friend);
			}
		}
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

			// Spawn our NPC unless we were flagged.
			if (!MyZ.instance.getFlagged().contains(e.getPlayer().getName())) {
				Utilities.spawnNPC(e.getPlayer());
			}
			MyZ.instance.getFlagged().remove(e.getPlayer().getName());
		}
	}
}
