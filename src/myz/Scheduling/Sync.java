/**
 * 
 */
package myz.Scheduling;

import java.util.HashMap;
import java.util.Map;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class Sync implements Runnable {

	public static Map<String, Integer> safeLogoutPlayers = new HashMap<String, Integer>();
	private static long ticks = 0;

	@Override
	public void run() {
		for (String player : getSafeLogoutPlayers().keySet()) {
			Player the_player = Bukkit.getPlayerExact(player);
			if (the_player == null) {
				safeLogoutPlayers.remove(player);
				continue;
			}
			int timeRemaining = safeLogoutPlayers.get(player);
			if (timeRemaining <= 0) {
				the_player.kickPlayer(Messenger.getConfigMessage("kick.safe_logout"));
				safeLogoutPlayers.remove(player);
				continue;
			}
			if (timeRemaining % 5 == 0 || timeRemaining <= 5)
				the_player.sendMessage(ChatColor.YELLOW + "" + timeRemaining);
			safeLogoutPlayers.put(player, timeRemaining - 1);
		}

		for (String location : MyZ.instance.getBlocksConfig().getKeys(false))
			if (MyZ.instance.getBlocksConfig().contains(location + ".time"))
				if (ticks >= MyZ.instance.getBlocksConfig().getLong(location + ".time"))
					actOnBlock(location, true);

		if (ticks == Long.MAX_VALUE || ticks == 0) {
			ticks = 0;

			// Restore all blocks because we're wrapping around after 2^63 -1
			// seconds and we would lose track OR we're at 0 because we might
			// have restarted the server.
			wipeBlocks();
		}
		ticks++;
	}

	/**
	 * Restore all the blocks to the world from the blocks YAML file.
	 */
	public static void wipeBlocks() {
		for (String location : MyZ.instance.getBlocksConfig().getKeys(false))
			actOnBlock(location, false);
		MyZ.instance.saveBlocksConfig();
	}

	/**
	 * Respawn or despawn a block with its given key from the blocks YAML
	 * config.
	 * 
	 * @param slug
	 *            The YAML config slug (world_x_y_z).
	 * @param autoSave
	 *            Whether or not to automatically save the YAML file.
	 */
	private static void actOnBlock(String slug, boolean autoSave) {
		if (MyZ.instance.getBlocksConfig().contains(slug + ".respawn") && MyZ.instance.getBlocksConfig().contains(slug + ".type")
				&& MyZ.instance.getBlocksConfig().contains(slug + ".data") && MyZ.instance.getBlocksConfig().contains(slug + ".time"))
			try {
				World world = Bukkit.getWorld(slug.split("_")[0]);
				if (world != null) {
					Location loc = new Location(world, Integer.parseInt(slug.split("_")[1]), Integer.parseInt(slug.split("_")[2]),
							Integer.parseInt(slug.split("_")[3]));
					if (MyZ.instance.getBlocksConfig().getBoolean(slug + ".respawn")) {
						Material mat = Material.getMaterial(MyZ.instance.getBlocksConfig().getString(slug + ".type"));
						if (mat != null) {
							loc.getBlock().setType(mat);
							loc.getBlock().setData((byte) MyZ.instance.getBlocksConfig().getInt(slug + ".data"), true);
						}
					} else
						loc.getBlock().setType(Material.AIR);
				}
			} catch (Exception exc) {
				// Bury silently.
			}
		MyZ.instance.getBlocksConfig().set(slug, null);
		if (autoSave)
			MyZ.instance.saveBlocksConfig();
	}

	/**
	 * Remove a player from the safe logout sequence.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void removeSafeLogoutPlayer(Player player) {
		safeLogoutPlayers.remove(player.getName());
	}

	/**
	 * Add a player to the list of those safely logging out.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void addSafeLogoutPlayer(Player player) {
		safeLogoutPlayers.put(player.getName(), Configuration.getSafeLogoutTime());
	}

	/**
	 * Set the players that are currently logging out.
	 * 
	 * @param players
	 *            The list of players.
	 */
	public static void setSafeLogoutPlayers(Map<String, Integer> players) {
		safeLogoutPlayers = players;
	}

	/**
	 * Get the current safe logout player list.
	 * 
	 * @return The list of players that are safely logging out.
	 */
	public static HashMap<String, Integer> getSafeLogoutPlayers() {
		return new HashMap<String, Integer>(safeLogoutPlayers);
	}

	/**
	 * Add a block to the despawn sequence.
	 * 
	 * @param block
	 *            The block.
	 * @param seconds
	 *            The time in seconds (from now) at which time it will despawn
	 *            (turn to air).
	 */
	public static void addDespawningBlock(Block block, int seconds) {
		FileConfiguration config = MyZ.instance.getBlocksConfig();
		ConfigurationSection section = config.createSection(block.getWorld().getName() + "_" + block.getX() + "_" + block.getY() + "_"
				+ block.getZ());
		section.set("respawn", false);
		section.set("type", block.getType().toString());
		section.set("data", block.getData());
		section.set("time", ticks + seconds);
		MyZ.instance.saveBlocksConfig();
	}

	/**
	 * Add a block to the respawn sequence.
	 * 
	 * @param block
	 *            The block.
	 * @param seconds
	 *            The time in seconds (from now) at which time it will respawn
	 *            (turn to previous block).
	 */
	public static void addRespawningBlock(Block block, int seconds) {
		FileConfiguration config = MyZ.instance.getBlocksConfig();
		ConfigurationSection section = config.createSection(block.getWorld().getName() + "_" + block.getX() + "_" + block.getY() + "_"
				+ block.getZ());
		section.set("respawn", true);
		section.set("type", block.getType().toString());
		section.set("data", block.getData());
		section.set("time", ticks + seconds);
		MyZ.instance.saveBlocksConfig();
	}
}
