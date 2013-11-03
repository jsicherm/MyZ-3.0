/**
 * 
 */
package myz.Commands;

import java.util.HashMap;
import java.util.Map;

import myz.Support.Configuration;
import myz.Support.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class BlockCommand implements CommandExecutor {

	public static Map<String, BlockFunction> blockChangers = new HashMap<String, BlockFunction>();

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0) {
				Messenger.sendConfigMessage(sender, "command.block.arguments");
			} else if (args[0].equalsIgnoreCase("place")) {
				if (args.length == 1) {
					Messenger.sendConfigMessage(sender, "command.block.place.arguments");
				} else {
					if (args[1].equalsIgnoreCase("add")) {
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
					} else {
						Messenger.sendConfigMessage(sender, "command.block.place.arguments");
					}
				}
			} else if (args[0].equalsIgnoreCase("destroy")) {
				if (args.length == 1) {
					Messenger.sendConfigMessage(sender, "command.block.destroy.arguments");
				} else {
					if (args[1].equalsIgnoreCase("add")) {
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
					} else {
						Messenger.sendConfigMessage(sender, "command.block.destroy.arguments");
					}
				}
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
				if (contains) {
					slug = "add.fail";
				} else {
					slug = "add.summary";
					Configuration.addDestroy(hit, hand, respawn);
				}
				break;
			case DESTROY_REMOVE:
				if (!contains) {
					slug = "remove.fail";
				} else {
					slug = "remove.summary";
					Configuration.removeDestroy(hit, hand);
				}
				break;
			default:
				return;
			}
			Messenger.sendMessage(
					player,
					Messenger.getConfigMessage("command.block.destroy." + slug, hit.getType().toString().toLowerCase().replaceAll("_", " ")
							+ (hit.getData() != (byte) 0 ? ":" + hit.getData() : ""),
							hand.getType().toString().toLowerCase().replaceAll("_", " ")));
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
				if (contains) {
					slug = "add.fail";
				} else {
					slug = "add.summary";
					Configuration.addPlace(placed, respawn);
				}
				break;
			case PLACE_REMOVE:
				if (!contains) {
					slug = "remove.fail";
				} else {
					slug = "remove.summary";
					Configuration.removePlace(placed);
				}
				break;
			default:
				return;
			}
			Messenger.sendMessage(
					player,
					Messenger.getConfigMessage("command.block.place." + slug, placed.getType().toString().toLowerCase()
							.replaceAll("_", " ")
							+ (placed.getData() != (byte) 0 ? ":" + placed.getData() : "")));
		}
	}
}
