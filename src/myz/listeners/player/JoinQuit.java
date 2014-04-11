/**
 * 
 */
package myz.listeners.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import myz.MyZ;
import myz.nmscode.compat.MobUtils;
import myz.support.PlayerData;
import myz.support.SQLManager;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;
import myz.utilities.Validate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Jordan
 * 
 */
public class JoinQuit implements Listener {

	/**
	 * Run all join actions for a player join event.
	 * 
	 * @param e
	 *            The event.
	 */
	private void doJoin(PlayerJoinEvent e) {
		playerJoin(e.getPlayer());
		e.setJoinMessage(null);
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		MyZ.instance.map(e.getPlayer());
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (MyZ.alertOps && e.getPlayer().isOp())
			Messenger.sendMessage(e.getPlayer(), ChatColor.YELLOW + "Visit http://my-z.org/request.php to get a free MyZ MySQL database.");
		doJoin(e);
	}

	@EventHandler
	private void onLeave(PlayerQuitEvent e) {
		if (MyZ.instance.removePlayer(e.getPlayer(), MyZ.instance.getFlagged().contains(e.getPlayer().getUniqueId()), false)) {
			e.setQuitMessage(null);

			if (e.getPlayer().getVehicle() != null)
				e.getPlayer().getVehicle().eject();

			// Get rid of our horse.
			for (Horse horse : e.getPlayer().getWorld().getEntitiesByClass(Horse.class))
				if (horse.getOwner() != null && horse.getOwner().getName() != null
						&& horse.getOwner().getName().equals(e.getPlayer().getName())) {
					horse.setOwner(null);
					horse.setTamed(false);
					horse.setDomestication(0);
				}

			// Spawn our NPC unless we were flagged.
			if (!MyZ.instance.getFlagged().contains(e.getPlayer().getUniqueId()) && !Configuration.isInLobby(e.getPlayer()))
				Utils.spawnNPC(e.getPlayer());
			MyZ.instance.getFlagged().remove(e.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPreJoin(AsyncPlayerPreLoginEvent e) {
		if ((Boolean) Configuration.getConfig(Configuration.PRELOGIN)) {
			UUID uid = MyZ.instance.getUID(e.getName());
			if (uid == null) { return; }
			PlayerData data = PlayerData.getDataFor(uid);
			/*
			 * Check if the player is still banned against the playerdata and sql.
			 */
			long now = System.currentTimeMillis();
			long timeOfKickExpiry = 0L;
			if (data != null)
				timeOfKickExpiry = data.getTimeOfKickban() + (Integer) Configuration.getConfig(Configuration.KICKBAN_TIME) * 1000;
			if (MyZ.instance.getSQLManager().isConnected())
				timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(uid, "timeOfKickban")
						+ (Integer) Configuration.getConfig(Configuration.KICKBAN_TIME) * 1000;

			if (timeOfKickExpiry >= now)
				e.disallow(Result.KICK_OTHER,
						Messenger.getConfigMessage(Localizer.DEFAULT, "kick.recur", (timeOfKickExpiry - now) / 1000 + ""));
		}
	}

	@EventHandler
	private void onWorldChange(PlayerChangedWorldEvent e) {
		if (Validate.inWorld(e.getPlayer().getLocation())) {
			if (!MyZ.instance.isPlayer(e.getPlayer()))
				playerJoin(e.getPlayer());
		} else
			MyZ.instance.removePlayer(e.getPlayer(), false, false);
	}

	/**
	 * Standardized join sequence for a player.
	 * 
	 * @param player
	 *            The player.
	 */
	private void playerJoin(Player player) {
		MyZ.instance.addPlayer(player, false);

		MobUtils.clearOurNPC(player);

		PlayerData data = PlayerData.getDataFor(player);

		// Update name colors for this player and all of their online friends.
		if (MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI").isEnabled()) {
			KittehTag.colorName(player);

			List<UUID> friends = new ArrayList<UUID>();
			List<String> stringFriends = new ArrayList<String>();

			if (data != null)
				friends = data.getFriends();
			if (MyZ.instance.getSQLManager().isConnected())
				stringFriends = MyZ.instance.getSQLManager().getStringList(player.getUniqueId(), "friends");
			for (String s : stringFriends) {
				UUID t = SQLManager.fromString(s, false);
				if (t != null)
					friends.add(t);
			}
			for (UUID friend : friends) {
				Player online_friend = MyZ.instance.getPlayer(friend);
				if (online_friend != null)
					KittehTag.colorName(online_friend);
			}
		}

		MyZ.instance.getFlagged().remove(player.getUniqueId());

		if (Utils.packets != null)
			for (Object packet : Utils.packets.keySet())
				if (player.getWorld().getName().equals(Utils.packets.get(packet).getWorld()))
					Utils.sendPacket(player, packet);
	}
}
