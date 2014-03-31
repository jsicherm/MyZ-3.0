/**
 * 
 */
package myz.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import myz.MyZ;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Jordan
 * 
 */
public class TranslateCommand implements CommandExecutor {

	/**
	 * Transpose a translation from an input file to a MyZ-ready code view.
	 */
	public static void beginTranslating(CommandSender sender, FileConfiguration i, File out) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(out));
			output.write("Map<String, Object> set = <SET>;\n\n");
			for (String key : i.getKeys(true))
				if (!i.isConfigurationSection(key) && i.get(key) != null)
					output.write("set.put(\"" + key + "\", \"" + i.get(key) + "\");\n");

			output.flush();
			output.close();
			sender.sendMessage(ChatColor.GREEN + "Transposed to " + out.getPath());
		} catch (IOException exc) {
			sender.sendMessage(ChatColor.RED + "Unable to write or create BufferedWriter: " + exc.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED
					+ "Please specify the name of an existing translation file followed by the name of an output file. Both files will exist in /MyZ-3/locales/");
			return true;
		}

		String first = args[0];
		String out = args[1];

		File folder = MyZ.instance.getLocales();

		File f = new File(folder.getAbsolutePath() + File.separator + first + ".yml");
		if (!f.exists()) {
			sender.sendMessage(ChatColor.RED + f.getPath() + " does not exist!");
			return true;
		}
		File o = new File(folder.getAbsolutePath() + File.separator + out + ".txt");
		if (!o.exists())
			try {
				o.createNewFile();
			} catch (IOException exc) {
				sender.sendMessage(ChatColor.RED + "Unable to create output file: " + exc.getMessage());
			}
		else {
			sender.sendMessage(ChatColor.RED + out + " already exists!");
			return true;
		}

		FileConfiguration x = YamlConfiguration.loadConfiguration(f);
		beginTranslating(sender, x, o);

		return true;
	}
}
