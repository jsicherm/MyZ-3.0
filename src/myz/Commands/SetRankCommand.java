/**
 * 
 */
package myz.Commands;

import myz.MyZ;
import myz.Support.Messenger;
import myz.Support.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class SetRankCommand implements CommandExecutor {

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

			OfflinePlayer offline_player = Bukkit.getOfflinePlayer(player);
			if (offline_player == null || !offline_player.hasPlayedBefore()) {
				if (sender instanceof Player)
					Messenger.sendConfigMessage(sender, "command.setrank.failure");
				else
					Messenger.sendConfigConsoleMessage("command.setrank.failure");
				return true;
			}

			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setRank(rank);
			if (MyZ.instance.getSQLManager().isConnected())
				MyZ.instance.getSQLManager().set(player, "rank", rank, true);
			if (sender instanceof Player)
				Messenger.sendConfigMessage(sender, "command.setrank.success");
			else
				Messenger.sendConfigConsoleMessage("command.setrank.success");
		}
		return true;
	}
}
