/**
 * 
 */
package myz.Commands;

import java.util.Map;

import myz.MyZ;
import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Jordan
 * 
 */
public class BaseCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// For reference, the map is set-up as Map<command name, Map<property,
		// value>>

		Map<String, Map<String, Object>> map = MyZ.instance.getDescription().getCommands();

		Messenger.sendConfigMessage(sender, "command.base.help");
		
		for (String cmd : map.keySet()) {
			if (!map.get(cmd).containsKey("permission") || sender.hasPermission((String) map.get(cmd).get("permission")))
				sender.sendMessage(ChatColor.YELLOW + cmd + ": " + ChatColor.RESET + map.get(cmd).get("description"));
		}
		return true;
	}
}
