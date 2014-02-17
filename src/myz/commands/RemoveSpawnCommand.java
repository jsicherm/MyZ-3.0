/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.List;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class RemoveSpawnCommand implements CommandExecutor, TabCompleter {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 1)
				try {
					if (Configuration.removeSpawnpoint(Integer.parseInt(args[0])))
						Messenger.sendConfigMessage(sender, "command.removespawn.removed");
					else
						Messenger.sendConfigMessage(sender, "command.removespawn.unable_to_remove");
				} catch (NumberFormatException exc) {
					Messenger.sendConfigMessage(sender, "command.removespawn.requires_number");
				}
			else
				Messenger.sendConfigMessage(sender, "command.removespawn.requires_number");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> returnList = new ArrayList<String>();
		if (args.length != 1)
			return returnList;
		int size = ((List<String>) Configuration.getConfig("spawnpoints")).size();
		for (int i = 1; i <= size; i++)
			returnList.add(i + "");
		return returnList;
	}
}
