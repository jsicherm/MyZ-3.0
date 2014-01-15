/**
 * 
 */
package myz.Commands;

import myz.Support.Messenger;
import myz.chests.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class ChestSetCommand implements CommandExecutor {

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
			Scanner.setters.put(sender.getName(), lootset.isEmpty() ? null : lootset);
			Messenger.sendConfigMessage(sender, "chest.set.click");
		}

		return true;
	}
}
