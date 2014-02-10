/**
 * 
 */
package myz.Commands;

import java.util.Set;

import myz.MyZ;
import myz.Support.Messenger;
import myz.Utilities.Localizer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class AddResearchCommand implements CommandExecutor {

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0) {
				Messenger.sendConfigMessage(sender, "command.research.arguments");
				return true;
			}

			ItemStack hand = ((Player) sender).getItemInHand();
			if (hand == null || hand.getType() == Material.AIR) {
				Messenger.sendConfigMessage(sender, "command.research.item");
				return true;
			}

			String value = "'"
					+ (hand.getItemMeta().hasDisplayName() ? hand.getItemMeta().getDisplayName() : hand.getType().toString().toLowerCase()
							.replaceAll("_", " ")) + (hand.getDurability() != (short) 0 ? ":" + hand.getDurability() : "") + "'";
			value += " x " + hand.getAmount();
			FileConfiguration config = MyZ.instance.getResearchConfig();
			if (!config.isSet("item"))
				config.createSection("item");
			Set<String> keys = config.getConfigurationSection("item").getKeys(false);

			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("remove")) {
					for (String key : keys)
						if (config.getItemStack("item." + key + ".item").equals(hand)) {
							config.set("item." + key, null);
							MyZ.instance.saveResearchConfig();
							Messenger.sendMessage(sender,
									Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "command.research.removed", value));
							return true;
						}

					Messenger.sendConfigMessage(sender, "command.research.item_no_exists");
					return true;
				} else {
					Messenger.sendConfigMessage(sender, "command.research.arguments");
					return true;
				}
			} else if (args.length == 2)
				if (args[0].equalsIgnoreCase("addreward") || args[0].equalsIgnoreCase("add")) {
					boolean reward = args[0].equalsIgnoreCase("addreward");
					String key = "1";
					while (keys.contains(key))
						key = Integer.parseInt(key) + 1 + "";
					for (String entry : keys)
						if (config.getItemStack("item." + entry + ".item").equals(hand))
							if (config.contains("item." + entry + (reward ? ".cost" : ".value"))) {
								Messenger.sendConfigMessage(sender, "command.research.item_exists");
								return true;
							} else {
								key = entry;
								break;
							}

					try {
						int points = Integer.parseInt(args[1]);
						config.set("item." + key + ".item", hand);
						config.set("item." + key + (reward ? ".cost" : ".value"), points);
						Messenger.sendMessage(
								sender,
								Messenger.getConfigMessage(Localizer.getLocale((Player) sender), "command.research."
										+ (reward ? "reward." : "") + "added", value, points));
						MyZ.instance.saveResearchConfig();
					} catch (NumberFormatException exc) {
						Messenger.sendConfigMessage(sender, "command.research.arguments");
						return true;
					}
				} else {
					Messenger.sendConfigMessage(sender, "command.research.arguments");
					return true;
				}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
