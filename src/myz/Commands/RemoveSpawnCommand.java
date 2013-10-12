/**
 * 
 */
package myz.Commands;

import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class RemoveSpawnCommand implements CommandExecutor {

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
}
