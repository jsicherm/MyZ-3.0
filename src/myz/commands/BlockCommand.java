/**
 * 
 */
package myz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class BlockCommand implements CommandExecutor, TabCompleter {

	public static Map<String, BlockFunction> blockChangers = new HashMap<String, BlockFunction>();

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0)
				Messenger.sendConfigMessage(sender, "command.block.arguments");
			else if (args[0].equalsIgnoreCase("place")) {
				if (args.length == 1)
					Messenger.sendConfigMessage(sender, "command.block.place.arguments");
				else if (args[1].equalsIgnoreCase("add")) {
					if (args.length == 3) {
						int seconds;
						try {
							seconds = Integer.parseInt(args[2]);
						} catch (NumberFormatException exc) {
							Messenger.sendConfigMessage(sender, "command.block.place.arguments");
							return true;
						}
						blockChangers.put(sender.getName(), new BlockFunction(BlockFunction.type.PLACE_ADD, seconds));
						Messenger.sendConfigMessage(sender, "command.block.place.add.help");
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					blockChangers.put(sender.getName(), new BlockFunction(BlockFunction.type.PLACE_REMOVE, -1));
					Messenger.sendConfigMessage(sender, "command.block.place.remove.help");
				} else
					Messenger.sendConfigMessage(sender, "command.block.place.arguments");
			} else if (args[0].equalsIgnoreCase("destroy")) {
				if (args.length == 1)
					Messenger.sendConfigMessage(sender, "command.block.destroy.arguments");
				else if (args[1].equalsIgnoreCase("add")) {
					if (args.length == 3) {
						int seconds;
						try {
							seconds = Integer.parseInt(args[2]);
						} catch (NumberFormatException exc) {
							Messenger.sendConfigMessage(sender, "command.block.destroy.arguments");
							return true;
						}
						blockChangers.put(sender.getName(), new BlockFunction(BlockFunction.type.DESTROY_ADD, seconds));
						Messenger.sendConfigMessage(sender, "command.block.destroy.add.help");
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					blockChangers.put(sender.getName(), new BlockFunction(BlockFunction.type.DESTROY_REMOVE, -1));
					Messenger.sendConfigMessage(sender, "command.block.destroy.remove.help");
				} else
					Messenger.sendConfigMessage(sender, "command.block.destroy.arguments");
			} else {
				Messenger.sendConfigMessage(sender, "command.block.arguments");
			}
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}

	public static class BlockFunction {

		public static enum type {
			PLACE_ADD, PLACE_REMOVE, DESTROY_ADD, DESTROY_REMOVE;
		};

		private type function;
		private int respawn;

		public BlockFunction(BlockFunction.type function, int respawn) {
			this.function = function;
			this.respawn = respawn;
		}

		/**
		 * Add the function to the configuration once a player left-clicks a
		 * block and remove them from the tracking list.
		 * 
		 * @param e
		 *            The event.
		 */
		public void doOnHit(ItemStack hand, Block hit, Player player) {
			boolean contains = Configuration.canBreak(hit, hand);
			blockChangers.remove(player.getName());

			String slug = "";
			switch (function) {
			case DESTROY_ADD:
				if (contains)
					slug = "add.fail";
				else {
					slug = "add.summary";
					Configuration.addDestroy(hit, hand, respawn);
				}
				break;
			case DESTROY_REMOVE:
				if (!contains)
					slug = "remove.fail";
				else {
					slug = "remove.summary";
					Configuration.removeDestroy(hit, hand);
				}
				break;
			default:
				return;

			}
			String item = hand.getType().toString().toLowerCase().replaceAll("_", " ");
			Messenger.sendMessage(
					player,
					Messenger.getConfigMessage(Localizer.getLocale(player), "command.block.destroy." + slug, hit.getType().toString()
							.toLowerCase().replaceAll("_", " ")
							+ (hit.getData() != (byte) 0 ? ":" + hit.getData() : ""), item == "air" ? "anything" : item));
		}

		/**
		 * Add the function to the configuration once a player places a block
		 * and remove them from the tracking list.
		 * 
		 * @param e
		 *            The event.
		 */
		public void doOnPlace(Block placed, Player player) {
			boolean contains = Configuration.canPlace(placed);
			blockChangers.remove(player.getName());

			String slug = "";
			switch (function) {
			case PLACE_ADD:
				if (contains)
					slug = "add.fail";
				else {
					slug = "add.summary";
					Configuration.addPlace(placed, respawn);
				}
				break;
			case PLACE_REMOVE:
				if (!contains)
					slug = "remove.fail";
				else {
					slug = "remove.summary";
					Configuration.removePlace(placed);
				}
				break;
			default:
				return;
			}
			Messenger.sendMessage(
					player,
					Messenger.getConfigMessage(Localizer.getLocale(player), "command.block.place." + slug, placed.getType().toString()
							.toLowerCase().replaceAll("_", " ")
							+ (placed.getData() != (byte) 0 ? ":" + placed.getData() : "")));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			return Arrays.asList("place", "destroy");
		} else if (args.length == 2) { return Arrays.asList("add", "remove"); }
		return new ArrayList<String>();
	}
}
