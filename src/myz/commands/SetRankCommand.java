/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import myz.MyZ;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class SetRankCommand implements CommandExecutor, TabCompleter {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length >= 2) {
			int rank = -1;
			String player = "";
			try {
				rank = Integer.parseInt(args[args.length - 1]);
			} catch (NumberFormatException exc) {
				if (sender instanceof Player)
					Messenger.sendConfigMessage(sender, "command.setrank.failure");
				else
					Messenger.sendConfigConsoleMessage("command.setrank.failure");
				return true;
			}

			if (rank < 0) {
				if (sender instanceof Player)
					Messenger.sendConfigMessage(sender, "command.setrank.failure");
				else
					Messenger.sendConfigConsoleMessage("command.setrank.failure");
				return true;
			}

			for (int i = 0; i < args.length - 1; i++)
				player += args[i] + " ";
			player = player.trim();

			UUID uid = MyZ.instance.getUID(player);
			if (uid == null) {
				if (sender instanceof Player)
					Messenger.sendConfigMessage(sender, "command.setrank.failure");
				else
					Messenger.sendConfigConsoleMessage("command.setrank.failure");
				return true;
			}

			PlayerData data = PlayerData.getDataFor(uid);
			if (data != null)
				data.setRank(rank);
			if (MyZ.instance.getSQLManager().isConnected())
				MyZ.instance.getSQLManager().set(uid, "rank", rank, true);
			if (sender instanceof Player)
				Messenger.sendConfigMessage(sender, "command.setrank.success");
			else
				Messenger.sendConfigConsoleMessage("command.setrank.success");
		} else if (sender instanceof Player)
			Messenger.sendConfigMessage(sender, "command.setrank.failure");
		else
			Messenger.sendConfigConsoleMessage("command.setrank.failure");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> returnList = new ArrayList<String>();
		if (args.length == 1) {
			if (sender instanceof Player)
				for (Player player : ((Player) sender).getWorld().getPlayers())
					returnList.add(player.getName());
			else
				for (Player player : Bukkit.getOnlinePlayers())
					returnList.add(player.getName());
		} else if (args.length == 2)
			for (int i : Configuration.getRankPrefixes().keySet())
				returnList.add(i + "");
		return returnList;
	}
}
