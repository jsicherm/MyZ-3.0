/**
 * 
 */
package myz.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import myz.support.MedKit;
import myz.support.interfacing.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class CreateMedKitCommand implements CommandExecutor {

	private Map<UUID, UnfinishedMedKit> kitCreators = new HashMap<UUID, UnfinishedMedKit>();

	public static class UnfinishedMedKit {

		private String configID, name;
		private int ointment = -1, antiseptic = -1;
		private ItemStack input, output;

		public UnfinishedMedKit(String configID) {
			this.configID = configID;
		}

		public int getAntiseptic() {
			return antiseptic;
		}

		public String getConfigID() {
			return configID;
		}

		public ItemStack getInput() {
			return input;
		}

		public String getName() {
			return name;
		}

		public int getOintment() {
			return ointment;
		}

		public ItemStack getOutput() {
			return output;
		}

		public MedKit toMedKit() {
			return new MedKit(configID, name, antiseptic, ointment, input, output);
		}
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (!kitCreators.containsKey(((Player) sender).getUniqueId()))
				if (args.length >= 1) {

					String configID = "", name;
					for (String string : args)
						configID += string + " ";
					configID = configID.trim();
					name = configID;

					for (MedKit kit : MedKit.getKits())
						if (kit.getConfigID().equals(configID)) {
							int i = 0;
							boolean flag = true;
							while (flag) {
								flag = false;
								for (MedKit kit2 : MedKit.getKits())
									if (kit2.getConfigID().equals(configID + i)) {
										i++;
										flag = true;
									}
							}
							configID += i;
							break;
						}
					sender.sendMessage("To create the MedKit '" + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET
							+ "', put the following items in your bar:");
					sender.sendMessage(ChatColor.YELLOW + "Slot 1 - [Input Item]");
					sender.sendMessage(ChatColor.YELLOW + "Slot 2 - [Number of Ointment Items Required]");
					sender.sendMessage(ChatColor.YELLOW + "Slot 3 - [Number of Antiseptic Items Required]");
					sender.sendMessage(ChatColor.YELLOW + "Slot 4 - [Output Item]");
					sender.sendMessage("Once the items are in place, run this command again.");

					UnfinishedMedKit medkit = new UnfinishedMedKit(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							configID)));
					medkit.name = name;

					kitCreators.put(((Player) sender).getUniqueId(), medkit);
				} else
					return false;
			else {
				UnfinishedMedKit kit = kitCreators.get(((Player) sender).getUniqueId());
				ItemStack input = ((Player) sender).getInventory().getItem(0);
				ItemStack ointment = ((Player) sender).getInventory().getItem(1);
				ItemStack antiseptic = ((Player) sender).getInventory().getItem(2);
				ItemStack output = ((Player) sender).getInventory().getItem(3);
				if (input == null || input.getType() == Material.AIR || output == null || output.getType() == Material.AIR) {
					sender.sendMessage(ChatColor.RED + "Cancelled.");
					kitCreators.remove(((Player) sender).getUniqueId());
					return true;
				}
				kit.input = input;
				kit.ointment = ointment == null ? 0 : ointment.getAmount();
				kit.antiseptic = antiseptic == null ? 0 : antiseptic.getAmount();
				kit.output = output;
				sender.sendMessage(kit.toMedKit().toString());
				kitCreators.remove(((Player) sender).getUniqueId());
			}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
