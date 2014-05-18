/**
 * 
 */
package myz.commands;

import myz.chests.ChestScanner;
import myz.support.interfacing.Messenger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class LootSetCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0) {
				Messenger.sendConfigMessage(sender, "loot.set.arguments");
				return true;
			}
			String lootset = "";
			StringBuilder b = new StringBuilder(lootset);
			for (String arg : args)
				b.append(arg + " ");
			lootset = b.toString().trim();
			ChestScanner.addLooter((Player) sender, lootset);
		}

		return true;
	}
}
