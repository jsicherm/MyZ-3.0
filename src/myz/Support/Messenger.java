/**
 * 
 */
package myz.Support;

import java.util.MissingFormatArgumentException;

import myz.MyZ;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class Messenger {

	/**
	 * Color a message and send it to a player.
	 * 
	 * @param player
	 *            The player.
	 * @param uncolored_message
	 *            The uncolored message.
	 */
	public static void sendMessage(CommandSender player, String uncolored_message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', uncolored_message));
	}

	/**
	 * Color a message and send it to all the players in a world.
	 * 
	 * @param inWorld
	 *            The world.
	 * @param uncolored_message
	 *            The uncolored message.
	 */
	public static void sendMessage(World inWorld, String uncolored_message) {
		for (Player player : inWorld.getPlayers())
			sendMessage(player, uncolored_message);
	}

	/**
	 * Color a config message and send it to a player.
	 * 
	 * @param player
	 *            The player.
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 */
	public static void sendConfigMessage(CommandSender player, String uncolored_config_message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				MyZ.instance.getConfig().getString("localizable." + uncolored_config_message)));
	}

	/**
	 * Color a config message and send it to all the players in a world.
	 * 
	 * @param inWorld
	 *            The world.
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 */
	public static void sendConfigMessage(World inWorld, String uncolored_config_message) {
		for (Player player : inWorld.getPlayers())
			sendConfigMessage(player, uncolored_config_message);
	}

	/**
	 * Send a colored message to the console.
	 * 
	 * @param uncolored_message
	 *            The uncolored message.
	 */
	public static void sendConsoleMessage(String uncolored_message) {
		Bukkit.getConsoleSender().sendMessage("[MyZ-3] " + ChatColor.translateAlternateColorCodes('&', uncolored_message));
	}

	/**
	 * Send a colored config message to the console.
	 * 
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 */
	public static void sendConfigConsoleMessage(String uncolored_config_message) {
		sendConsoleMessage(ChatColor.translateAlternateColorCodes('&',
				MyZ.instance.getConfig().getString("localizable." + uncolored_config_message)));
	}

	/**
	 * Get a message out of the config and color it.
	 * 
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 * @param variables
	 *            Any applicable variables denoted by a %s.
	 * @return The colored message with replaced variables.
	 */
	public static String getConfigMessage(String uncolored_config_message, Object... variables) {
		String message = MyZ.instance.getConfig().getString("localizable." + uncolored_config_message);
		if (variables != null)
			try {
				message = String.format(message, variables);
			} catch (MissingFormatArgumentException exc) {
				sendConsoleMessage(ChatColor.RED + message + " must have the correct number of variables (%s). Please reformat.");
				message = message.replaceAll("%s", "");
			}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
