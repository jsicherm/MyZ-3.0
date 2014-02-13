/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class SaveKitCommand implements CommandExecutor, TabCompleter {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			int rank = 0;
			if (args.length == 1)
				try {
					rank = Integer.parseInt(args[0]);
				} catch (NumberFormatException exc) {
					Messenger.sendConfigMessage(sender, "command.savekit.requires_number");
				}
			List<ItemStack> armor = Arrays.asList(((Player) sender).getInventory().getArmorContents());
			List<ItemStack> inventory = Arrays.asList(((Player) sender).getInventory().getContents());
			Configuration.setArmorContents(armor, rank);
			Configuration.setInventoryContents(inventory, rank);
			sender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "command.savekit.saved", rank));
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> returnList = new ArrayList<String>();
		if (args.length != 1) { return returnList; }
		returnList.add(Configuration.getRanks().size() + "");
		return returnList;
	}
}
