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
public class JoinClanCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length >= 1) {
				String clan = "";
				for (String arg : args) {
					clan += arg + " ";
				}
				clan = clan.trim();
				if (clan.length() >= 20) {
					Messenger.sendConfigMessage(sender, "clan.name.too_long");
					return true;
				}
				PlayerData data = PlayerData.getDataFor((Player) sender);
				if (data != null) {
					data.setClan(clan);
					Messenger.sendMessage(sender, Messenger.getConfigMessage("clan.joined", clan));
				}
				if (MyZ.instance.getSQLManager().isConnected()) {
					Messenger.sendConfigMessage(sender, "clan.joining");
					MyZ.instance.getSQLManager().setClan(sender.getName(), clan);
				}
			} else {
				PlayerData data = PlayerData.getDataFor((Player) sender);
				if (data != null) {
					if (data.inClan()) {
						Messenger.sendConfigMessage(sender, "command.clan.leave");
						data.setClan("");
					}
				}
				if (MyZ.instance.getSQLManager().isConnected()) {
					if (MyZ.instance.getSQLManager().inClan(sender.getName())) {
						Messenger.sendConfigMessage(sender, "command.clan.leave");
						MyZ.instance.getSQLManager().setClan(sender.getName(), "");
					}
				}
			}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
