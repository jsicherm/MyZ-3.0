/**
 * 
 */
package myz.Utilities;

import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.API.PlayerFriendEvent;
import myz.Support.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author Jordan
 * 
 */
public class Utilities {

	/**
	 * Get a skull for a given player.
	 * 
	 * @param name
	 *            The player's name.
	 * @return The skinned skull item.
	 */
	public static ItemStack playerSkull(String name) {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		ItemMeta itemMeta = head.getItemMeta();
		((SkullMeta) itemMeta).setOwner(name);
		itemMeta.setDisplayName(name + "'s Head");
		head.setItemMeta(itemMeta);
		return head;
	}

	/**
	 * Whether or not there is a player nearby a location.
	 * 
	 * @param player
	 *            The player that wants to spawn.
	 * @param location
	 *            The location.
	 * @param radius
	 *            The radius.
	 * @return True if there is at least one player within the specified radius
	 *         of the location that isn't a friend of @param player.
	 */
	public static boolean isPlayerNearby(Player player, Location location, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - radius % 16) / 16;

		List<String> applicableFriends = new ArrayList<String>();
		PlayerData data = PlayerData.getDataFor(player);
		if (MyZ.instance.getSQLManager().isConnected())
			applicableFriends = MyZ.instance.getSQLManager().getStringList(player.getName(), "friends");

		for (int chunkX = 0 - chunkRadius; chunkX <= chunkRadius; chunkX++)
			for (int chunkZ = 0 - chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) location.getX();
				int y = (int) location.getY();
				int z = (int) location.getZ();

				for (Entity entity : new Location(location.getWorld(), x + chunkX * 16, y, z + chunkZ * 16).getChunk().getEntities())
					if (entity instanceof Player) {
						if (((Player) entity).getName().equals(player.getName()))
							continue;
						if (entity.getLocation().distance(location) <= radius && entity.getLocation().getBlock() != location.getBlock()) {
							/*
							 * Ensure the player isn't a friend of our player.
							 */
							if (data != null && !data.isFriend((Player) entity))
								return true;
							if (MyZ.instance.getSQLManager().isConnected() && !applicableFriends.contains(((Player) entity).getName()))
								return true;
						}
					}
			}
		return false;
	}

	// Source:
	// [url]http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/[/url]
	public static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
		final double epsilon = 0.0001f;

		Vector3D d = p2.subtract(p1).multiply(0.5);
		Vector3D e = max.subtract(min).multiply(0.5);
		Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
		Vector3D ad = d.abs();

		if (Math.abs(c.x) > e.x + ad.x)
			return false;
		if (Math.abs(c.y) > e.y + ad.y)
			return false;
		if (Math.abs(c.z) > e.z + ad.z)
			return false;

		if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
			return false;
		if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
			return false;
		if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
			return false;

		return true;
	}

	/**
	 * Toggle a friend from non-friend to friend by sneak-blocking.
	 * 
	 * @param player
	 *            The player that sneak-blocked.
	 */
	public static void sneakAddFriend(final Player player) {
		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				// Make sure we are sneaking and blocking.
				if (player != null && !player.isDead() && player.isOnline() && player.isSneaking() && player.isBlocking()) {
					// Ensure we have players nearby.
					List<Player> nearby = new ArrayList<Player>();
					for (Entity entity : player.getNearbyEntities(20, 10, 20))
						if (entity instanceof Player)
							nearby.add((Player) entity);
					if (nearby.isEmpty())
						return;

					Location eyePosition = player.getEyeLocation();
					Vector3D POV = new Vector3D(eyePosition.getDirection());

					Vector3D eyeLocation = new Vector3D(eyePosition);
					Vector3D POV_end = eyeLocation.add(POV.multiply(20));

					Player hit = null;

					for (Player target : nearby) {
						// Bounding box of the given player.
						Vector3D targetPosition = new Vector3D(target.getLocation());
						Vector3D minimum = targetPosition.add(-0.5, 0, -0.5);
						Vector3D maximum = targetPosition.add(0.5, 1.67, 0.5);

						if (target != player && hasIntersection(eyeLocation, POV_end, minimum, maximum))
							if (hit == null
									|| hit.getLocation().distanceSquared(eyePosition) > target.getLocation().distanceSquared(eyePosition))
								hit = target;
					}

					if (hit != null && !MyZ.instance.isFriend(player, hit.getName())) {
						PlayerFriendEvent event = new PlayerFriendEvent(player, hit.getName());
						MyZ.instance.getServer().getPluginManager().callEvent(event);
						if (!event.isCancelled())
							MyZ.instance.addFriend(player, hit.getName());
					}
				}
			}
		}, 20L);
	}

	/**
	 * List the players within a given radius of a given player. Includes the
	 * player denoted by @param player.
	 * 
	 * @param player
	 *            The player.
	 * @param radius
	 *            The radius.
	 * @return The list of players within the radius of the player or an empty
	 *         list if none were found.
	 */
	public static List<Player> getPlayersInRange(Player player, int radius) {
		List<Player> players = new ArrayList<Player>();
		int d2 = radius * radius;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getWorld() == player.getWorld())
				if (p.getLocation().distanceSquared(player.getLocation()) <= d2)
					players.add(p);
		return players;
	}
}
