/**
 * 
 */
package myz.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Jordan
 * 
 */
public class GetUIDCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/*
		 * The override for MrTeePee (developer) is required for verification on my-z.org so that users do not try to list servers they don't own.
		 * Of course, server owners could simply give users their server ID but I'm hoping this will reduce the amount of spam to my email.
		 */
		if(sender.isOp() || sender.getName().equals("MrTeePee")) {
			sender.sendMessage(Bukkit.getServerId());
		}
		return true;
	}
}
