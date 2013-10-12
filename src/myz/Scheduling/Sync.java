/**
 * 
 */
package myz.Scheduling;

import java.util.HashMap;
import java.util.Map;

import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class Sync implements Runnable {

	private static Map<String, Integer> safeLogoutPlayers = new HashMap<String, Integer>();

	@Override
	public void run() {
		for (String player : getSafeLogoutPlayers().keySet()) {
			Player the_player = Bukkit.getPlayerExact(player);
			if (the_player == null) {
				safeLogoutPlayers.remove(player);
				continue;
			}
			int timeRemaining = safeLogoutPlayers.get(player);
			if (timeRemaining <= 0) {
				the_player.kickPlayer(Messenger.getConfigMessage("kick.safe_logout"));
				safeLogoutPlayers.remove(player);
				continue;
			}
			if (timeRemaining % 5 == 0 || timeRemaining <= 5)
				the_player.sendMessage(ChatColor.YELLOW + "" + timeRemaining);
			safeLogoutPlayers.put(player, timeRemaining - 1);
		}
	}

	/**
	 * Remove a player from the safe logout sequence.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void removeSafeLogoutPlayer(Player player) {
		safeLogoutPlayers.remove(player.getName());
	}

	/**
	 * Add a player to the list of those safely logging out.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void addSafeLogoutPlayer(Player player) {
		safeLogoutPlayers.put(player.getName(), Configuration.getSafeLogoutTime());
	}

	/**
	 * Set the players that are currently logging out.
	 * 
	 * @param players
	 *            The list of players.
	 */
	public static void setSafeLogoutPlayers(Map<String, Integer> players) {
		safeLogoutPlayers = players;
	}

	/**
	 * Get the current safe logout player list.
	 * 
	 * @return The list of players that are safely logging out.
	 */
	public static HashMap<String, Integer> getSafeLogoutPlayers() {
		return new HashMap<String, Integer>(safeLogoutPlayers);
	}
}
