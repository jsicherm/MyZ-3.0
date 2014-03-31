/**
 * 
 */
package myz.listeners.player;

import myz.MyZ;
import myz.utilities.Validate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

/**
 * @author Jordan
 * 
 */
public class KittehTag implements Listener {

	/**
	 * Color the name of the given player based on bandit/healer definitions.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void colorName(Player player) {
		if (MyZ.instance.isBandit(player) || MyZ.instance.isHealer(player))
			TagAPI.refreshPlayer(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onNameTagsReceived(AsyncPlayerReceiveNameTagEvent e) {
		if (!Validate.inWorld(e.getNamedPlayer().getLocation()))
			return;

		Player player = e.getNamedPlayer();

		if (MyZ.instance.isBandit(player))
			e.setTag(ChatColor.RED + e.getTag());
		else if (MyZ.instance.isHealer(player))
			e.setTag(ChatColor.GREEN + e.getTag());
		if (MyZ.instance.isFriend(e.getPlayer().getUniqueId(), player.getUniqueId()))
			e.setTag(ChatColor.BLUE + e.getTag());
	}
}
