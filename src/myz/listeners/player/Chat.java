/**
 * 
 */
package myz.listeners.player;

import java.util.HashSet;
import java.util.Set;

import myz.MyZ;
import myz.nmscode.compat.MessageUtils;
import myz.nmscode.compat.PathUtils;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;
import myz.utilities.Validate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class Chat implements Listener {

	/**
	 * Handle all @ chat to search for players with the specified name or name
	 * fragment and send a private message.
	 * 
	 * @param e
	 *            The AsyncPlayerChatEvent that invoked this test.
	 * @return True if normal execution of onChat(AsyncPlayerChatEvent e) has to
	 *         stop (because a private message was attempted).
	 */
	private boolean didHandlePrivateChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();

		if (e.getMessage().startsWith("@")) {
			int results = 0;
			Player recipient = null;
			String fromMessage = ChatColor.translateAlternateColorCodes('&', Configuration.getFromPrefix(player));
			String toMessage = "";
			String finalMessage = "";
			String[] words = e.getMessage().split(" ");
			for (int i = 1; i < words.length; i++)
				finalMessage += " " + words[i];
			String queriedName = words[0].toLowerCase().substring(1, words[0].length());
			Player[] onlinePlayers = player.getServer().getOnlinePlayers();

			for (Player testPlayer : onlinePlayers)
				if (testPlayer != player)
					if (testPlayer.getName().toLowerCase().indexOf(queriedName) >= 0
							|| testPlayer.getDisplayName().toLowerCase().indexOf(queriedName) >= 0) {
						results++;
						recipient = testPlayer;
						toMessage = ChatColor.translateAlternateColorCodes('&', Configuration.getToPrefix(recipient));
					}
			switch (results) {
			case 0:
				Messenger.sendConfigMessage(player, "private.no_player");
				e.setCancelled(true);
				break;
			case 1:
				player.sendMessage(toMessage + finalMessage);
				recipient.sendMessage(fromMessage + finalMessage);
				if ((Boolean) Configuration.getConfig("chat.format"))
					e.setFormat(Configuration.getPrefixForPlayerRank(player) + ChatColor.RESET + " to "
							+ Configuration.getPrefixForPlayerRank(recipient) + " " + ChatColor.RESET + e.getMessage());
				e.setMessage(ChatColor.GRAY + toMessage + finalMessage);
				break;
			default:
				Messenger.sendConfigMessage(player, "private.many_players");
				e.setCancelled(true);
				break;
			}
			return true;
		} else if (e.getMessage().startsWith(".")) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null) {
				if ((Boolean) Configuration.getConfig("chat.format"))
					e.setFormat(MyZ.instance.getConfig().getString("localizable.private.clan_prefix") + " "
							+ Configuration.getPrefixForPlayerRank(player) + ": " + e.getMessage().replaceFirst(".", ""));
				e.getRecipients().clear();
				e.getRecipients().addAll(data.getOnlinePlayersInClan());
				return true;
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				if ((Boolean) Configuration.getConfig("chat.format"))
					e.setFormat(MyZ.instance.getConfig().getString("localizable.private.clan_prefix") + " "
							+ Configuration.getPrefixForPlayerRank(player) + ": " + e.getMessage().replaceFirst(".", ""));
				e.getRecipients().clear();
				e.getRecipients().addAll(MyZ.instance.getSQLManager().getOnlinePlayersInClan(e.getPlayer().getUniqueId()));
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onChat(AsyncPlayerChatEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;

		Player player = e.getPlayer();
		String prefix = Configuration.getPrefixForPlayerRank(player);
		int radio_frequency = -1;

		// Apply the appropriate format to the message depending on radio state
		// and rank prefix.
		if (player.getItemInHand() != null && player.getItemInHand().isSimilar((ItemStack) Configuration.getConfig(Configuration.RADIO))) {
			radio_frequency = player.getInventory().getHeldItemSlot() + 1;
			prefix = ChatColor.translateAlternateColorCodes('&', Configuration.getRadioPrefix(radio_frequency)) + " " + prefix
					+ ChatColor.translateAlternateColorCodes('&', Configuration.getRadioColor());
		}
		if ((Boolean) Configuration.getConfig("chat.format"))
			e.setFormat(prefix + e.getMessage());

		// Cache and clear the recipients.
		Set<Player> original_recipients = new HashSet<Player>(e.getRecipients());
		e.getRecipients().clear();

		// If we're talking in local, not radio, only include those near us.
		if (radio_frequency == -1) {
			if (didHandlePrivateChat(e)) {
				if ((Boolean) Configuration.getConfig("chat.overhead"))
					MessageUtils.holographic(e.getMessage(), e.getPlayer(), e.getPlayer().getLocation(),
							e.getRecipients().toArray(new Player[0]));
				return;
			}
			if (!(Boolean) Configuration.getConfig(Configuration.CHAT_ENABLED))
				e.getRecipients().addAll(original_recipients);
			else
				for (Player player_in_range : Utils.getPlayersInRange(player.getLocation(),
						(Integer) Configuration.getConfig(Configuration.CHAT_DISTANCE)))
					e.getRecipients().add(player_in_range);
		} else
			// Add all players with the same radio equipped.
			for (Player player_on_server : player.getServer().getOnlinePlayers())
				if (player_on_server.getInventory().getItem(radio_frequency - 1) != null
						&& player_on_server.getInventory().getItem(radio_frequency - 1)
								.isSimilar((ItemStack) Configuration.getConfig(Configuration.RADIO)))
					e.getRecipients().add(player_on_server);

		if ((Boolean) Configuration.getConfig("chat.overhead"))
			MessageUtils.holographic(e.getMessage(), e.getPlayer(), e.getPlayer().getLocation(), e.getRecipients().toArray(new Player[0]));

		PathUtils.elevate(e.getPlayer(), 10);
	}
}
