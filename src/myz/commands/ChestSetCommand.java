/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.List;

import myz.chests.ChestScanner;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class ChestSetCommand implements CommandExecutor, TabCompleter {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			String lootset = "";
			for (String arg : args)
				lootset += arg + " ";
			lootset = lootset.trim();
			ChestScanner.setters.put(((Player) sender).getUniqueId(), lootset.isEmpty() ? null : lootset);
			Messenger.sendConfigMessage(sender, "chest.set.click");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1)
			return new ArrayList<String>(Configuration.getLootsets());
		return new ArrayList<String>();
	}
}
