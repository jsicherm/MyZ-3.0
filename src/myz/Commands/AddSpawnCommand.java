/**
 * 
 */
package myz.Commands;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class AddSpawnCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (!MyZ.instance.getWorlds().contains(((Player) sender).getWorld().getName()))
				return true;

			Location new_location = ((Player) sender).getLocation();
			if (Configuration.addSpawnpoint(new_location))
				Messenger.sendConfigMessage(sender, "command.addspawn.added");
			else
				Messenger.sendConfigMessage(sender, "command.addspawn.already_exists");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
