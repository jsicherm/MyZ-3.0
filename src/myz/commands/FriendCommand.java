/**
 * 
 */
package myz.commands;

import myz.MyZ;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class FriendCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length != 0) {
				String name = "";
				for (String partial : args)
					name += partial + " ";
				name = name.trim();
				if (name.equals(((Player) sender).getName()))
					return true;

				OfflinePlayer player = Bukkit.getOfflinePlayer(name);
				if (player != null && player.hasPlayedBefore()) {
					if (MyZ.instance.isFriend((Player) sender, name))
						MyZ.instance.removeFriend((Player) sender, name);
					else
						MyZ.instance.addFriend((Player) sender, name);
				} else
					sender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "command.friend.non_exist", name));
			} else
				Messenger.sendConfigMessage(sender, "command.friend.requires_name");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
