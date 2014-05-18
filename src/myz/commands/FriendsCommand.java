/**
 * 
 */
package myz.commands;

import java.util.UUID;

import myz.MyZ;
import myz.support.PlayerData;
import myz.support.SQLManager;
import myz.support.interfacing.Messenger;

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
			StringBuilder b = new StringBuilder(output);
			PlayerData data = PlayerData.getDataFor((Player) sender);
			if (data != null)
				for (UUID name : data.getFriends()) {
					b.append(current + MyZ.instance.getName(name) + ChatColor.WHITE + ", ");
					if (current == ChatColor.DARK_RED)
						current = ChatColor.RED;
					else
						current = ChatColor.DARK_RED;
				}
			else if (MyZ.instance.getSQLManager().isConnected())
				for (String name : MyZ.instance.getSQLManager().getStringList(((Player) sender).getUniqueId(), "friends")) {
					UUID uid = SQLManager.fromString(name, false);
					if (uid == null)
						continue;
					b.append(current + MyZ.instance.getName(uid) + ChatColor.WHITE + ", ");
					if (current == ChatColor.DARK_RED)
						current = ChatColor.RED;
					else
						current = ChatColor.DARK_RED;
				}
			if (b.length() >= 2)
				output = b.toString().substring(0, output.length() - 2);
			if (!b.toString().trim().isEmpty() && b.toString() != "" && b.toString().trim() != "")
				sender.sendMessage(b.toString());
			else
				Messenger.sendConfigMessage(sender, "command.friend.empty");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
