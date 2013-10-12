/**
 * 
 */
package myz.Commands;

import myz.MyZ;
import myz.Support.Messenger;
import myz.Support.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class FriendsCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			ChatColor current = ChatColor.DARK_RED;
			String output = "";
			PlayerData data = PlayerData.getDataFor((Player) sender);
			if (data != null)
				for (String name : data.getFriends()) {
					output += current + name + ChatColor.WHITE + ", ";
					if (current == ChatColor.DARK_RED)
						current = ChatColor.RED;
					else
						current = ChatColor.DARK_RED;
				}
			else if (MyZ.instance.getSQLManager().isConnected())
				for (String name : MyZ.instance.getSQLManager().getStringList(sender.getName(), "friends")) {
					output += current + name + ChatColor.WHITE + ", ";
					if (current == ChatColor.DARK_RED)
						current = ChatColor.RED;
					else
						current = ChatColor.DARK_RED;
				}
			if (output.length() >= 2)
				output = output.substring(0, output.length() - 2);
			sender.sendMessage(output);
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
