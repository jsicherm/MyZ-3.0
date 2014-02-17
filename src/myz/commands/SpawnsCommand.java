/**
 * 
 */
package myz.commands;

import java.util.List;

import myz.support.interfacing.Configuration;
import myz.utilities.WorldlessLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class SpawnsCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> spawnpoints = (List<String>) Configuration.getConfig("spawnpoints");
		int spawnnumber = 1;
		ChatColor current = ChatColor.YELLOW;
		boolean player = sender instanceof Player;

		for (String loc : spawnpoints) {
			WorldlessLocation location = WorldlessLocation.fromString(loc);
			if (player)
				sender.sendMessage(current + "" + spawnnumber + ". " + location.toString());
			else
				Bukkit.getConsoleSender().sendMessage(current + "" + spawnnumber + ". " + location.toString());
			if (current == ChatColor.YELLOW)
				current = ChatColor.GOLD;
			else
				current = ChatColor.YELLOW;
			spawnnumber++;
		}
		return true;
	}
}
