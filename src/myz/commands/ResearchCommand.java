/**
 * 
 */
package myz.commands;

import java.util.List;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class ResearchCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(((Player) sender).getWorld().getName()))
				return true;
			Utils.showResearchDialog((Player) sender, 1);
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
