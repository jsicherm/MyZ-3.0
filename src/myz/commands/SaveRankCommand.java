/**
 * 
 */
package myz.commands;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class SaveRankCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		int rank = 0;
		String prefix = "";
		if (args.length >= 2) {
			try {
				rank = Integer.parseInt(args[0]);
			} catch (Exception exc) {
				Messenger.sendConfigMessage(sender, "command.saverank.requires_number");
				return true;
			}
			for (int position = 1; position < args.length; position++)
				prefix += args[position] + " ";
			prefix = prefix.trim();
		} else if (args.length == 1)
			prefix = args[0];
		else if (args.length == 0) {
			Messenger.sendConfigMessage(sender, "command.saverank.requires_prefix");
			return true;
		}
		Configuration.setRankPrefix(rank, prefix);
		sender.sendMessage(Messenger.getConfigMessage(sender instanceof Player ? Localizer.getLocale((Player) sender) : Localizer.ENGLISH,
				"command.saverank.saved", rank, prefix.replace("%s", "<player name>")));
		return true;
	}
}
