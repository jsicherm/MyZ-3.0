/**
 * 
 */
package myz.commands;

import myz.MyZ;
import myz.support.PlayerData;
import myz.support.interfacing.Localizer;
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
public class JoinClanCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length >= 1) {
				String clan = "";
				StringBuilder b = new StringBuilder(clan);
				for (String arg : args)
					b.append(arg + " ");
				clan = b.toString().trim();
				if (clan.length() >= 20) {
					Messenger.sendConfigMessage(sender, "clan.name.too_long");
					return true;
				}
				PlayerData data = PlayerData.getDataFor((Player) sender);
				if (data != null)
					if (sender.hasPermission("myz.clan.join")) {
						Messenger.sendConfigMessage(sender, "clan.joining");
						data.setClan(clan);
						Messenger
								.sendMessage(sender, Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "clan.joined", clan));
					} else
						Messenger.sendMessage(sender,
								Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "clan.notjoined", clan));
				if (MyZ.instance.getSQLManager().isConnected()) {
					Messenger.sendConfigMessage(sender, "clan.joining");
					MyZ.instance.getSQLManager().setClan(((Player) sender).getUniqueId(), clan);
				}
			} else {
				PlayerData data = PlayerData.getDataFor((Player) sender);
				if (data != null)
					if (data.inClan()) {
						Messenger.sendConfigMessage(sender, "command.clan.leave");
						data.setClan("");
					}
				if (MyZ.instance.getSQLManager().isConnected())
					if (MyZ.instance.getSQLManager().inClan(((Player) sender).getUniqueId())) {
						Messenger.sendConfigMessage(sender, "command.clan.leave");
						MyZ.instance.getSQLManager().setClan(((Player) sender).getUniqueId(), "");
					}
			}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
