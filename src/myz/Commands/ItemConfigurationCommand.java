/**
 * 
 */
package myz.Commands;

import java.util.Map;

import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class ItemConfigurationCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			ItemStack hand = ((Player) sender).getItemInHand();
			if (hand != null) {
				Map<String, Object> serialized = hand.serialize();
				Messenger.sendMessage(sender, "==: org.bukkit.inventory.ItemStack");
				for (String key : serialized.keySet())
					Messenger.sendMessage(sender, key + ": " + serialized.get(key));
			}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
