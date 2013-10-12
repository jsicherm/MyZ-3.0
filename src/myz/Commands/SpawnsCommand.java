/**
 * 
 */
package myz.Commands;

import java.util.List;

import myz.Support.Configuration;
import myz.Utilities.WorldlessLocation;

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
		List<WorldlessLocation> spawnpoints = Configuration.getSpawnpoints();
		int spawnnumber = 1;
		ChatColor current = ChatColor.YELLOW;
		boolean player = sender instanceof Player;

		for (WorldlessLocation location : spawnpoints) {
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
