/**
 * 
 */
package myz.commands;

import myz.MyZ;

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
		if (sender.isOp() || sender.getName().equals("MrTeePee"))
			sender.sendMessage(getUID());
		return true;
	}

	private String getUID() {
		String id = MyZ.instance.getServer().getServerId().isEmpty() ? "d38" : MyZ.instance.getServer().getServerId()
				.substring(0, MyZ.instance.getServer().getServerId().length() / 3).replaceAll("a", "5").replaceAll("r", "!");
		id += MyZ.instance.getServer().getVersion().isEmpty() ? "g84T" : MyZ.instance.getServer().getVersion()
				.substring(0, MyZ.instance.getServer().getVersion().length() / 4).toLowerCase().replaceAll("bukkit", "b77")
				.replaceAll("-", "G");
		id += MyZ.instance.getServer().getIp().replaceAll("1", "a").replaceAll("2", "b").replaceAll("9", "c").replaceAll(".", "9");
		id += "__fj38";
		return id;
	}
}
