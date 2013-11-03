/**
 * 
 */
package myz.Commands;

import java.util.Map;

import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Utilities.Utilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class AllowedCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Messenger.sendConfigMessage(sender, "command.allowed.breakable");

		Map<ItemStack, ItemStack> broken = Configuration.getAllowedBroken();

		for (ItemStack key : broken.keySet()) {
			Messenger.sendMessage(sender, Utilities.getNameOf(key) + " -> " + Utilities.getNameOf(broken.get(key)));
		}
		sender.sendMessage("");
		Messenger.sendConfigMessage(sender, "command.allowed.placeable");
		for (ItemStack item : Configuration.getAllowedPlaced()) {
			Messenger.sendMessage(sender, Utilities.getNameOf(item));
		}

		return true;
	}
}
