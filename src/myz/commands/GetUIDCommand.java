/**
 * 
 */
package myz.commands;

import java.util.Random;

import myz.MyZ;
import myz.utilities.NMSUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Jordan
 * 
 */
public class GetUIDCommand implements CommandExecutor {

	private static final Random random = new Random();

	private String getUID() {
		String id = "";
		id += MyZ.instance.getDescription().getVersion().replaceAll(".", "&").replaceFirst("3", "MZ");
		id += "@";
		String s = MyZ.instance.getServer().getIp().replaceAll(".", String.valueOf(Character.toChars(random.nextInt(5) + 10 + 96)[0]));
		for (int i = 1; i <= 9; i++)
			s = s.replaceAll(i + "", String.valueOf(Character.toChars(i + 96)[0]));
		id += s;
		id += ">";
		id += MyZ.instance.getServer().getOnlineMode() ? "y" : "n";
		id += "<";
		id += NMSUtils.version;
		id += "MrTeePee-wuz-here";
		// ID will look something like the following:
		// MZ&0&81@abgz0k0a>n<v1_7_R1MrTeePee-wuz-here

		// MyZ version @ string representation of IP with random high-char
		// delimiters > online ? y : n < Bukkit version Easy identifier.
		return id;
	}

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
}
