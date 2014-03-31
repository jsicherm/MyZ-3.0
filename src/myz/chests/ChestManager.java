/**
 * 
 */
package myz.chests;

import java.util.Map;
import java.util.Random;

import myz.MyZ;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;

/**
 * @author Jordan
 * 
 */
public class ChestManager {

	/**
	 * Convert a config string representing a chest location to a location.
	 * 
	 * @param entry
	 *            The config entry.
	 * @return The location converted to or null if the entry is outdated (does
	 *         not contain a world name). If null is returned, a console message
	 *         is sent requesting the entry to be updated.
	 */
	private static Location configToLocation(String entry) {
		String[] split = entry.split(",");
		try {
			return new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]),
					Integer.parseInt(split[3]));
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4Chest location configured wrong or outdated. Please prefix the entry &e'" + entry
					+ "'&4 with the world name. It should look like so: &eWORLD," + entry);
			return null;
		}
	}

	/**
	 * Make sure a MyZ chest exists. Accounts for slugs with a world but no
	 * direction, without a world or direction, with a world and direction or
	 * without a world but with a direction.
	 * 
	 * @param location
	 *            The location.
	 * @return The BlockFace the chest is supposed to face or null if the chest
	 *         listing doesn't exist in the config.
	 */
	private static BlockFace doesExist(Location location) {
		// Look for set directions.
		BlockFace[] values = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		for (BlockFace slug : values)
			if (MyZ.instance.getChestsConfig().contains(
					"chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + slug)
					|| MyZ.instance.getChestsConfig().contains(
							"chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
									+ location.getBlockZ() + "," + slug))
				return slug;

		// Contains without a direction set.
		if (MyZ.instance.getChestsConfig().contains(
				"chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ())
				|| MyZ.instance.getChestsConfig().contains(
						"chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
								+ location.getBlockZ()))
			return null;

		// Not even a chest.
		return null;
	}

	/**
	 * Fill a MyZ chest. Assumes the chest is already of material chest and is a
	 * MyZ chest.
	 * 
	 * @param inventory
	 *            The inventory of the chest block.
	 * @param lootset
	 *            The lootset to fill with.
	 */
	private static void fillChest(Inventory inventory, String lootset) {
		int fill = 0;
		Random random = new Random();
		Map<ItemStack, Double> spawning = Configuration.getLootsetContents(lootset);
		// Lootset doesn't exist.
		if (spawning.isEmpty())
			return;
		// Make sure we add at least one item. Also ensure that there's a 10%
		// chance we do this again. The more the merrier.
		while (fill < 1 || random.nextDouble() <= 0.1)
			for (ItemStack key : spawning.keySet()) {
				// Make sure we don't overfill.
				if (inventory.firstEmpty() == -1)
					return;
				// Add the item if the price is right.
				if (random.nextDouble() <= spawning.get(key)) {
					inventory.addItem(key.clone());
					fill++;
				}
			}
	}

	/**
	 * Get the facing direction of a MyZ chest at a location.
	 * 
	 * @param location
	 *            The location.
	 * @return The BlockFace it is facing or north if not set.
	 */
	private static BlockFace getFacingDirection(Location location) {
		if (isMyZChest(location)) {
			BlockFace face = doesExist(location);
			return face != null ? face : BlockFace.NORTH;
		}
		return BlockFace.NORTH;
	}

	/**
	 * Make sure a MyZ chest exists and get the lootset of it. Accounts for
	 * slugs with a world but no direction, without a world or direction, with a
	 * world and direction or without a world but with a direction.
	 * 
	 * @param location
	 *            The location.
	 * @return The lootset the chest is supposed to contain or null if the chest
	 *         listing doesn't exist in the config.
	 */
	private static String getLootset(Location location) {
		if (isMyZChest(location)) {
			String locationAt = null;
			// Look for set directions.
			BlockFace[] values = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
			for (BlockFace slug : values)
				if (MyZ.instance.getChestsConfig().contains(
						"chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + slug))
					locationAt = "chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + slug;
				else if (MyZ.instance.getChestsConfig().contains(
						"chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
								+ location.getBlockZ() + "," + slug))
					locationAt = "chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
							+ location.getBlockZ() + "," + slug;

			if (locationAt == null)
				// Contains without a direction set.
				if (MyZ.instance.getChestsConfig().contains(
						"chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()))
					locationAt = "chests." + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
				else if (MyZ.instance.getChestsConfig().contains(
						"chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
								+ location.getBlockZ()))
					locationAt = "chests." + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
							+ location.getBlockZ();

			if (locationAt != null)
				return MyZ.instance.getChestsConfig().getString(locationAt);
		}
		return null;
	}

	/**
	 * Get whether or not an inventory is empty.
	 * 
	 * @param i
	 *            The inventory.
	 * @return True if it was empty, false otherwise.
	 */
	private static boolean isEmpty(Inventory i) {
		for (ItemStack item : i.getContents())
			if (item != null && item.getType() != Material.AIR)
				return false;
		return true;
	}

	/**
	 * Break a MyZ chest.
	 * 
	 * @param block
	 *            The chest that was broken.
	 */
	public static void breakChest(Block block) {
		block.setType(Material.AIR);
	}

	/**
	 * Whether or not a specific location holds a MyZ chest
	 * 
	 * @param location
	 *            The location.
	 * @return True if the location has a MyZ chest, false otherwise.
	 */
	public static boolean isMyZChest(Location location) {
		return doesExist(location) != null;
	}

	/**
	 * Respawn a specific MyZ chest.
	 * 
	 * @param location
	 *            The location.
	 * @param bypass
	 *            Whether or not to bypass nearby player range.
	 */
	public static void respawn(Location location, boolean bypass) {
		if (isMyZChest(location))
			if (Utils.getPlayersInRange(location, 5).isEmpty() || bypass) {
				boolean wasChest = location.getBlock() != null && location.getBlock().getType() == Material.CHEST;
				if (wasChest) {
					org.bukkit.block.Chest test = (org.bukkit.block.Chest) location.getBlock().getState();
					wasChest = !isEmpty(test.getBlockInventory());
				} else {
					location.getBlock().setType(Material.CHEST);
					Chest chest = (Chest) location.getBlock().getState().getData();
					chest.setFacingDirection(getFacingDirection(location));
					location.getBlock().setData(chest.getData(), true);
				}

				String lootset = getLootset(location);
				if (lootset != null) {
					ChestScanner.nameChest(location.getBlock(), lootset);
					if (!wasChest)
						fillChest(((org.bukkit.block.Chest) location.getBlock().getState()).getBlockInventory(), lootset);
				}
			}
	}

	/**
	 * Respawn all MyZ chests.
	 * 
	 * @param bypass
	 *            Whether or not to bypass nearby player range.
	 */
	public static void respawnAll(boolean bypass) {
		if (MyZ.instance.getChestsConfig() == null)
			return;
		if (MyZ.instance.getChestsConfig().isConfigurationSection("chests"))
			for (String entry : MyZ.instance.getChestsConfig().getConfigurationSection("chests").getKeys(false)) {
				Location location = configToLocation(entry);
				if (location != null)
					respawn(location, bypass);
			}
	}
}
