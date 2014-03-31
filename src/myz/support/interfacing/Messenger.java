/**
 * 
 */
package myz.support.interfacing;

import myz.MyZ;
import myz.support.PlayerData;
import myz.utilities.VaultUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class Messenger {

	/**
	 * Get a message out of the config and color it.
	 * 
	 * @param locale
	 *            The locale.
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 * @param variables
	 *            Any applicable variables denoted by a %s.
	 * @return The colored message with replaced variables.
	 */
	public static String getConfigMessage(Localizer locale, String uncolored_config_message, String... variables) {
		String message = MyZ.instance.getLocalizableConfig(locale).getString(uncolored_config_message);
		if (variables != null)
			try {
				message = String.format(message, variables);
			} catch (Exception exc) {
				sendConsoleMessage(ChatColor.RED + message + " must have the correct number of variables (%s). Please reformat: "
						+ exc.getMessage());
				message = message.replaceAll("%s", "");
			}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Process a string and replace all applicable arguments.
	 * 
	 * @param msg
	 *            The input message.
	 * @return The message with all arguments replaced.
	 */
	public static String processForArguments(Player player, String msg) {
		if (player == null)
			return msg;
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null) {
			msg = msg.replaceAll("%CLAN%", data.getClan());
			msg = msg.replaceAll("%RANK%", "" + data.getRank());
			msg = msg.replaceAll("%RESEARCH%", "" + data.getResearchPoints());
		}

		if (MyZ.instance.getSQLManager().isConnected()) {
			msg = msg.replaceAll("%CLAN%", MyZ.instance.getSQLManager().getClan(player.getUniqueId()));
			msg = msg.replaceAll("%RANK%", "" + MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "rank"));
			msg = msg.replaceAll("%RESEARCH%", "" + MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "research"));
		}

		msg = msg.replaceAll("%NAME%", player.getName());
		msg = msg.replaceAll("%THIRST%", "" + player.getLevel());
		msg = msg.replaceAll("%HEALTH%", "" + (int) player.getHealth());
		if (MyZ.vault)
			try {
				msg = msg.replaceAll("%GROUP%", VaultUtils.permission.getPrimaryGroup(player));
			} catch (Exception exc) {
				String c = ChatColor.getLastColors(msg);
				msg = msg.replaceAll("%GROUP%", ChatColor.RED + "N/A" + c);
			}

		return msg;
	}

	/**
	 * Send a colored config message to the console.
	 * 
	 * @param uncolored_config_message
	 *            The uncolored config message.
	 */
	public static void sendConfigConsoleMessage(String uncolored_config_message) {
		sendConsoleMessage(ChatColor.translateAlternateColorCodes('&',
				MyZ.instance.getLocalizableConfig(Localizer.ENGLISH).getString(uncolored_config_message)));
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
		if (player instanceof Player)
			uncolored_config_message = processForArguments((Player) player,
					MyZ.instance.getLocalizableConfig(Localizer.getLocale((Player) player)).getString(uncolored_config_message));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', uncolored_config_message));
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
	 * Send a FancyMessage death formatted to a world of players. Localizes too.
	 * 
	 * @param playerFor
	 *            The murderer.
	 * @param typeFor
	 *            The murdered.
	 */
	public static void sendFancyDeathMessage(Player playerFor, Player typeFor) {
		ItemStack pH = playerFor.getItemInHand();
		ItemStack tH = typeFor.getItemInHand();

		for (Player player : playerFor.getWorld().getPlayers())
			new FancyMessage(Configuration.getPrefixForPlayerRank(playerFor)).itemTooltip(pH)
					.then(" " + Messenger.getConfigMessage(Localizer.getLocale(player), "murdered") + " ")
					.then(Configuration.getPrefixForPlayerRank(typeFor)).itemTooltip(tH);
	}

	/**
	 * Color a message and send it to a player.
	 * 
	 * @param player
	 *            The player.
	 * @param uncolored_message
	 *            The uncolored message.
	 */
	public static void sendMessage(CommandSender player, String uncolored_message) {
		if (player instanceof Player)
			uncolored_message = processForArguments((Player) player, uncolored_message);
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
	 * Color a message and send it to all the players in a world. Provides
	 * ability to specify a string variable and still allow for locale.
	 * 
	 * @param inWorld
	 *            The world.
	 * @param uncolored_message
	 *            The uncolored message.
	 * @param variable
	 *            A string variable.
	 */
	public static void sendMessage(World inWorld, String uncolored_config_message, String variable) {
		for (Player player : inWorld.getPlayers())
			if (player != null)
				sendConfigMessage(player, getConfigMessage(Localizer.getLocale(player), uncolored_config_message, variable));
	}
}
