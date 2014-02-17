/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.support.PlayerData;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.Bukkit;
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
public class StatsCommand implements CommandExecutor, TabCompleter {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player || args.length != 0) {
			String name = sender.getName();
			if (args.length != 0) {
				name = "";
				for (String word : args)
					name += word + " ";
				name = name.trim();
			}

			PlayerData data = PlayerData.getDataFor(name);
			int zombie = 0, pigman = 0, giant = 0, player = 0, life = 0;
			long total = 0;

			if (data != null) {
				zombie = data.getZombieKills();
				pigman = data.getPigmanKills();
				giant = data.getGiantKills();
				player = data.getPlayerKills();
				total = data.getMinutesAlive();
				life = data.getMinutesAliveLife();
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				zombie = MyZ.instance.getSQLManager().getInt(sender.getName(), "zombie_kills");
				pigman = MyZ.instance.getSQLManager().getInt(sender.getName(), "pigman_kills");
				giant = MyZ.instance.getSQLManager().getInt(sender.getName(), "giant_kills");
				player = MyZ.instance.getSQLManager().getInt(sender.getName(), "player_kills");
				total = MyZ.instance.getSQLManager().getLong(sender.getName(), "minutes_alive");
				life = MyZ.instance.getSQLManager().getInt(sender.getName(), "minutes_alive_life");
			}
			Messenger.sendMessage(sender, Messenger.getConfigMessage(sender instanceof Player ? Localizer.getLocale((Player) sender)
					: Localizer.ENGLISH, "command.stats.header", name));
			sender.sendMessage("");
			Messenger.sendConfigMessage(sender, "command.stats.kills_header");
			Messenger.sendMessage(sender, Messenger.getConfigMessage(sender instanceof Player ? Localizer.getLocale((Player) sender)
					: Localizer.ENGLISH, "command.stats.kills", zombie + "", pigman + "", giant + "", player + ""));
			sender.sendMessage("");
			Messenger.sendConfigMessage(sender, "command.stats.time_header");
			Messenger.sendMessage(sender, Messenger.getConfigMessage(sender instanceof Player ? Localizer.getLocale((Player) sender)
					: Localizer.ENGLISH, "command.stats.time", total + "", life + ""));
			sender.sendMessage("");
			Messenger.sendMessage(sender, Messenger.getConfigMessage(sender instanceof Player ? Localizer.getLocale((Player) sender)
					: Localizer.ENGLISH, "command.stats.footer", name));
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> returnList = new ArrayList<String>();
		if (args.length == 1)
			if (sender instanceof Player)
				for (Player player : ((Player) sender).getWorld().getPlayers())
					returnList.add(player.getName());
			else
				for (Player player : Bukkit.getOnlinePlayers())
					returnList.add(player.getName());
		return returnList;
	}
}
