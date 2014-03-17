/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import myz.MyZ;
import myz.support.interfacing.Localizer;
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
public class FriendCommand implements CommandExecutor, TabCompleter {

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

				UUID uid = MyZ.instance.getUID(name);
				if (uid != null) {
					if (MyZ.instance.isFriend((Player) sender, uid))
						MyZ.instance.removeFriend((Player) sender, uid);
					else
						MyZ.instance.addFriend((Player) sender, uid);
				} else
					sender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "command.friend.non_exist", name));
			} else
				Messenger.sendConfigMessage(sender, "command.friend.requires_name");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> returnList = new ArrayList<String>();
		if (sender instanceof Player && args.length == 1)
			for (Player player : ((Player) sender).getWorld().getPlayers())
				returnList.add(player.getName());
		return returnList;
	}
}
