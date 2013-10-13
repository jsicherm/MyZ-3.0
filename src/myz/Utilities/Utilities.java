/**
 * 
 */
package myz.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import myz.MyZ;
import myz.API.PlayerFriendEvent;
import myz.Scheduling.Sync;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.mobs.CustomEntityPlayer;
import myz.mobs.CustomEntityZombie;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;
import net.minecraft.server.v1_6_R3.World;
import net.minecraft.server.v1_6_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
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

	/**
	 * Begin a safe logout sequence for the specified player.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void startSafeLogout(Player player) {
		if (!Sync.getSafeLogoutPlayers().containsKey(player.getName())) {
			Messenger.sendConfigMessage(player, "safe_logout.beginning");
			Sync.addSafeLogoutPlayer(player);
		}
	}

	/**
	 * Spawn in a zombified version of a player.
	 * 
	 * @param player
	 *            The player to zombify.
	 * @param inventory
	 *            The inventory to spawn the zombie with. If null, will instead
	 *            use the player's inventory.
	 */
	public static void spawnPlayerZombie(Player player, List<ItemStack> inventory) {
		ItemStack head = playerSkull(player.getName());

		World world = ((CraftWorld) player.getWorld()).getHandle();
		CustomEntityZombie zombie = new CustomEntityZombie(world);
		zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
		world.addEntity(zombie);

		zombie.setBaby(false);
		zombie.setVillager(false);
		((Zombie) zombie.getBukkitEntity()).setRemoveWhenFarAway(true);
		zombie.setCustomName(player.getName());
		((Zombie) zombie.getBukkitEntity()).setCanPickupItems(false);

		((Zombie) zombie.getBukkitEntity()).getEquipment().setHelmet(head);
		((Zombie) zombie.getBukkitEntity()).getEquipment().setHelmetDropChance(0f);
		((Zombie) zombie.getBukkitEntity()).getEquipment().setChestplate(player.getEquipment().getChestplate());
		((Zombie) zombie.getBukkitEntity()).getEquipment().setChestplateDropChance(1f);
		((Zombie) zombie.getBukkitEntity()).getEquipment().setLeggings(player.getEquipment().getLeggings());
		((Zombie) zombie.getBukkitEntity()).getEquipment().setLeggingsDropChance(1f);
		((Zombie) zombie.getBukkitEntity()).getEquipment().setBoots(player.getEquipment().getBoots());
		((Zombie) zombie.getBukkitEntity()).getEquipment().setBootsDropChance(1f);
		((Zombie) zombie.getBukkitEntity()).getEquipment().setItemInHand(player.getEquipment().getItemInHand());
		((Zombie) zombie.getBukkitEntity()).getEquipment().setItemInHandDropChance(0f);

		if (inventory == null)
			inventory = new ArrayList<ItemStack>(Arrays.asList(player.getInventory().getContents()));
		inventory.add(player.getEquipment().getHelmet());
		zombie.setInventory(inventory);
	}

	/**
	 * Spawn a player NPC for the given player.
	 * 
	 * @param playerDuplicate
	 *            The player to duplicate an NPC for.
	 */
	public static void spawnNPC(Player playerDuplicate) {
		// The NPC won't even be on the screen for a second, may as well not add
		// it.
		if (Configuration.getSafeLogoutTime() <= 0) { return; }

		WorldServer worldServer = ((CraftWorld) playerDuplicate.getWorld()).getHandle();
		final CustomEntityPlayer player = new CustomEntityPlayer(worldServer.getMinecraftServer(), worldServer, playerDuplicate.getName(),
				new PlayerInteractManager(worldServer));
		Location loc = playerDuplicate.getLocation();
		player.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		((Player) player.getBukkitEntity()).setItemInHand(playerDuplicate.getItemInHand());
		((Player) player.getBukkitEntity()).setCustomName(playerDuplicate.getName());
		((Player) player.getBukkitEntity()).getEquipment().setArmorContents(playerDuplicate.getInventory().getArmorContents());
		player.setInventory(new ArrayList<ItemStack>(Arrays.asList(playerDuplicate.getInventory().getContents())));

		((Player) player.getBukkitEntity()).setHealthScale(playerDuplicate.getHealthScale());
		((Player) player.getBukkitEntity()).setHealth(playerDuplicate.getHealth());
		((Player) player.getBukkitEntity()).setRemoveWhenFarAway(false);

		worldServer.addEntity(player, SpawnReason.CUSTOM);
		player.world.players.remove(player);
		MyZ.instance.getNPCs().add(player);

		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			public void run() {
				MyZ.instance.getNPCs().remove(player);
				player.getBukkitEntity().remove();
			}
		}, Configuration.getSafeLogoutTime() * 20L);
	}

	/**
	 * Fired when a player NPC dies. Assumes the NPC entity has already been
	 * removed.
	 * 
	 * @param customEntityPlayer
	 *            The NPC that died.
	 */
	public static void playerNPCDied(CustomEntityPlayer player) {
		MyZ.instance.getNPCs().remove(player);
		PlayerData data = PlayerData.getDataFor(player.getName());
		if (data != null) {
			data.setWasKilledNPC(true);
		}
		if (MyZ.instance.getSQLManager().isConnected()) {
			MyZ.instance.getSQLManager().set(player.getName(), "wasNPCKilled", true, true);
		}
		Messenger.sendMessage(player.getBukkitEntity().getWorld(), Messenger.getConfigMessage("player_npc_killed", player.getName()));
	}
}
