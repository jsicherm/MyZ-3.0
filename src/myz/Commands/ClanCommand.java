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
public class ClanCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			PlayerData data = PlayerData.getDataFor((Player) sender);
			String clan = "";
			int online = 1, total = 1;
			if (data != null) {
				clan = data.getClan();
				online = data.getOnlinePlayersInClan().size();
				total = data.getNumberInClan();
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				clan = MyZ.instance.getSQLManager().getClan(sender.getName());
				online = MyZ.instance.getSQLManager().getOnlinePlayersInClan(sender.getName()).size();
				total = MyZ.instance.getSQLManager().getNumberInClan(sender.getName());
			}
			if (clan == null || clan.isEmpty())
				Messenger.sendConfigMessage(sender, "command.clan.not_in");
			else
				Messenger.sendMessage(sender, Messenger.getConfigMessage("command.clan.in", clan, online, total));
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
