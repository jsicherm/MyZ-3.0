/**
 * 
 */
package myz.commands;

import java.util.Map;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Configuration.TimePair;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;

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

		Map<ItemStack, TimePair> broken = Configuration.getAllowedBroken();

		for (ItemStack key : broken.keySet())
			Messenger.sendMessage(sender, Utils.getNameOf(key) + " -> " + Utils.getNameOf(broken.get(key).getItem()));
		sender.sendMessage("");
		Messenger.sendConfigMessage(sender, "command.allowed.placeable");
		for (TimePair item : Configuration.getAllowedPlaced())
			Messenger.sendMessage(sender, Utils.getNameOf(item.getItem()));

		return true;
	}
}
