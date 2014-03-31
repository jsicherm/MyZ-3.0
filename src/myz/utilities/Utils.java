/**
 * 
 */
package myz.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import myz.MyZ;
import myz.api.PlayerFriendEvent;
import myz.mobs.CustomEntityPlayer;
import myz.mobs.CustomEntityZombie;
import myz.scheduling.Sync;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

/**
 * @author Jordan
 * 
 */
public class Utils {

	public static Map<Object, WorldUUID> packets;

	public static class WorldUUID {
		private String world;
		private UUID uuid;

		public WorldUUID(String world, UUID uuid) {
			this.world = world;
			this.uuid = uuid;
		}

		public UUID getUUID() {
			return uuid;
		}

		public String getWorld() {
			return world;
		}
	}

	/**
	 * Send a packet to every player in the world without saving it.
	 * 
	 * @param world
	 *            The world.
	 * @param packet
	 *            The packet.
	 */
	public static void distributePacket(org.bukkit.World world, Object packet) {
		for (Player player : world.getPlayers())
			try {
				NMSUtils.sendPacket(packet, player);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * Get the name of an ItemStack.
	 * 
	 * @param item
	 *            The ItemStack.
	 * @return The name of the item material with data if applicable or, a
	 *         display name, if present.
	 */
	public static String getNameOf(ItemStack item) {
		if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null || item.getItemMeta().getDisplayName().isEmpty())
			return item.getType().toString().charAt(0) + item.getType().toString().substring(1).toLowerCase().replaceAll("_", " ")
					+ (item.getDurability() != (short) 0 ? ":" + item.getDurability() : "");
		return item.getItemMeta().getDisplayName() + "&r";
	}

	/**
	 * Get all entities near a location.
	 * 
	 * @param location
	 *            The location.
	 * @param radius
	 *            The radius to search.
	 * @return The list of entities near the location or an empty list if none
	 *         found.
	 */
	public static List<Entity> getNearbyEntities(Location location, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - radius % 16) / 16;

		List<Entity> entities = new ArrayList<Entity>();

		for (int chunkX = 0 - chunkRadius; chunkX <= chunkRadius; chunkX++)
			for (int chunkZ = 0 - chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) location.getX();
				int y = (int) location.getY();
				int z = (int) location.getZ();

				for (Entity entity : new Location(location.getWorld(), x + chunkX * 16, y, z + chunkZ * 16).getChunk().getEntities())
					entities.add(entity);
			}
		return entities;
	}

	/**
	 * List the players within a given radius of a given location.
	 * 
	 * @param location
	 *            The location.
	 * @param radius
	 *            The radius.
	 * @return The list of players within the radius of the player or an empty
	 *         list if none were found.
	 */
	public static List<Player> getPlayersInRange(Location location, int radius) {
		List<Player> players = new ArrayList<Player>();
		int d2 = radius * radius;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getWorld() == location.getWorld())
				if (p.getLocation().distanceSquared(location) <= d2)
					players.add(p);
		return players;
	}

	public static Object getPrivateField(Object obj, String field) throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		return f.get(obj);
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
	 * Whether or not there is a creature nearby a location.
	 * 
	 * @param player
	 *            The player that wants to spawn.
	 * @param location
	 *            The location.
	 * @param radius
	 *            The radius.
	 * @return True if there is at least one creature/player within the
	 *         specified radius of the location that isn't a friend of @param
	 *         player.
	 */
	public static boolean isCreatureNearby(Player player, Location location, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - radius % 16) / 16;

		List<String> applicableFriends = new ArrayList<String>();
		PlayerData data = PlayerData.getDataFor(player);
		if (MyZ.instance.getSQLManager().isConnected())
			applicableFriends = MyZ.instance.getSQLManager().getStringList(player.getUniqueId(), "friends");

		for (int chunkX = 0 - chunkRadius; chunkX <= chunkRadius; chunkX++)
			for (int chunkZ = 0 - chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				int x = (int) location.getX();
				int y = (int) location.getY();
				int z = (int) location.getZ();

				for (Entity entity : new Location(location.getWorld(), x + chunkX * 16, y, z + chunkZ * 16).getChunk().getEntities())
					if (entity instanceof Player) {
						if (((Player) entity).getUniqueId().equals(player.getUniqueId()))
							continue;
						if (entity.getLocation().distance(location) <= radius && entity.getLocation().getBlock() != location.getBlock()) {
							/*
							 * Ensure the player isn't a friend of our player.
							 */
							if (data != null && !data.isFriend((Player) entity))
								return true;
							if (MyZ.instance.getSQLManager().isConnected()
									&& !applicableFriends.contains(((Player) entity).getUniqueId().toString()))
								return true;
						}
					} else if (entity instanceof Creature)
						return true;
			}
		return false;
	}

	/**
	 * Fired when a player NPC dies. Assumes the NPC entity has already been
	 * removed.
	 * 
	 * @param customEntityPlayer
	 *            The NPC that died.
	 * @param world
	 *            The World the player died in.
	 */
	public static void playerNPCDied(CustomEntityPlayer player, World world) {
		MyZ.instance.getNPCs().remove(player);
		PlayerData data = PlayerData.getDataFor(player.getBukkitEntity().getUniqueId());
		if (data != null)
			data.setWasKilledNPC(true);
		if (MyZ.instance.getSQLManager().isConnected())
			MyZ.instance.getSQLManager().set(player.getBukkitEntity().getUniqueId(), "wasNPCKilled", true, true);
		Messenger.sendMessage(world, "player_npc_killed", player.getName());
	}

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
		itemMeta.setDisplayName(name);
		head.setItemMeta(itemMeta);
		return head;
	}

	/**
	 * Pull an entity to a location.
	 * 
	 * @param e
	 *            The entity.
	 * @param loc
	 *            The location.
	 * @param direct
	 *            False to pull parabolically, true otherwise.
	 */
	public static void pullTo(Entity e, Location loc, boolean direct) {
		Location l = e.getLocation();

		// Snowgears
		if (l.distanceSquared(loc) < 9) {
			if (loc.getY() > l.getY()) {
				e.setVelocity(new Vector(0, 0.25, 0));
				return;
			}
			Vector v = loc.toVector().subtract(l.toVector());
			e.setVelocity(v);
			return;
		}

		l.setY(l.getY() + 0.5);
		e.teleport(l);

		// Snowgears
		double d = loc.distance(l);
		double g = -0.08;
		double x = (1.0 + 0.07 * d) * (loc.getX() - l.getX()) / d;
		double y = (1.0 + 0.03 * d) * (loc.getY() - l.getY()) / d + (direct ? 0 : -0.5 * g * d);
		double z = (1.0 + 0.07 * d) * (loc.getZ() - l.getZ()) / d;

		Vector v = e.getVelocity();
		v.setX(x);
		v.setY(y);
		v.setZ(z);
		v.multiply(direct ? 1.5 : 1.0);
		e.setVelocity(v);
	}

	/**
	 * Save a packet and distribute it to all the players in the world. Players
	 * will be sent this packet when they log in as well.
	 * 
	 * @param packet
	 *            The packet.
	 * @param world
	 *            The World.
	 */
	public static void saveAndDistributePacket(Object packet, Entity entity) {
		if (packets == null)
			packets = new HashMap<Object, WorldUUID>();
		packets.put(packet, new WorldUUID(entity.getWorld().getName(), entity.getUniqueId()));
		for (Player player : entity.getWorld().getPlayers())
			try {
				NMSUtils.sendPacket(packet, player);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * Send a player a packet.
	 * 
	 * @param player
	 *            The player.
	 * @param packet
	 *            The packet.
	 */
	public static void sendPacket(final Player player, final Object packet) {
		if (packets == null)
			packets = new HashMap<Object, WorldUUID>();

		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				Object cp;
				try {
					cp = Class.forName("net.minecraft.server." + NMSUtils.version + ".PacketPlayOutNamedEntitySpawn").newInstance();
				} catch (Exception exc) {
					exc.printStackTrace();
					return;
				}
				LivingEntity npc = null;
				UUID uid = null;
				if (packets.containsKey(packet))
					uid = packets.get(packet).uuid;
				for (Entity entity : player.getWorld().getEntitiesByClass(Skeleton.class))
					if (entity.getUniqueId() == uid) {
						npc = (LivingEntity) entity;
						break;
					}
				if (npc == null)
					return;

				try {
					setPrivateField(cp, "a", npc.getEntityId());
					setPrivateField(cp, "b", getPrivateField(packet, "b"));
					setPrivateField(cp, "c", (int) (npc.getLocation().getX() * 32));
					setPrivateField(cp, "d", (int) (npc.getLocation().getY() * 32));
					setPrivateField(cp, "e", (int) (npc.getLocation().getZ() * 32));
					setPrivateField(cp, "f", (byte) npc.getLocation().getPitch());
					setPrivateField(cp, "g", (byte) npc.getLocation().getYaw());
					setPrivateField(cp, "h", npc.getEquipment().getItemInHand() != null ? npc.getEquipment().getItemInHand().getType()
							.getId() : 0);
				} catch (Exception exc) {
					Messenger.sendConsoleMessage("&4PacketPlayerOutNamedEntitySpawn issue!");
					return;
				}

				try {
					Field f = cp.getClass().getDeclaredField("i");
					f.setAccessible(true);
					f.set(cp, f.get(packet));
					NMSUtils.sendPacket(cp, player);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}, 20L);
	}

	public static void setPrivateField(Object obj, String field, Object value) throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(obj, value);
	}

	/**
	 * Show the researching GUI to a player.
	 * 
	 * @param player
	 *            The player.
	 * @param pg
	 *            The page to show, starting at 1 (not 0).
	 */
	public static void showResearchDialog(Player player, int pg) {
		// Load points.
		int points = 0;
		PlayerData data = PlayerData.getDataFor(player.getUniqueId());
		if (data != null)
			points = data.getResearchPoints();
		if (MyZ.instance.getSQLManager().isConnected())
			points = MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "research");

		// Create inventories and initial arrows.
		ItemStack leftArrow = new ItemStack(Material.PISTON_EXTENSION);
		ItemMeta meta = leftArrow.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + Messenger.getConfigMessage(Localizer.getLocale(player), "gui.previous_page"));
		leftArrow.setItemMeta(meta);
		ItemStack rightArrow = new ItemStack(Material.PISTON_EXTENSION);
		meta = rightArrow.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + Messenger.getConfigMessage(Localizer.getLocale(player), "gui.next_page"));
		rightArrow.setItemMeta(meta);

		List<Inventory> inventories = new ArrayList<Inventory>();
		Inventory gui = Bukkit.createInventory(null, 9, Messenger.getConfigMessage(Localizer.DEFAULT, "science_gui", points + "") + " (1)");
		inventories.add(gui);
		gui.setItem(0, leftArrow);
		gui.setItem(8, rightArrow);

		// Start loading items.
		int position = 1;
		int page = 1;
		if (MyZ.instance.getResearchConfig().getConfigurationSection("item") != null)
			for (String key : MyZ.instance.getResearchConfig().getConfigurationSection("item").getKeys(false))
				if (MyZ.instance.getResearchConfig().contains("item." + key + ".cost")) {
					ItemStack item = MyZ.instance.getResearchConfig().getItemStack("item." + key + ".item").clone();
					meta = item.getItemMeta();
					meta.setLore(Arrays.asList(ChatColor.WHITE
							+ Messenger.getConfigMessage(Localizer.getLocale(player), "gui.cost",
									MyZ.instance.getResearchConfig().get("item." + key + ".cost") + "")));
					item.setItemMeta(meta);
					gui.setItem(position, item);
					position++;
					if (position == 8) {
						// Wrap to new page.
						page++;
						gui = Bukkit.createInventory(null, 9, Messenger.getConfigMessage(Localizer.DEFAULT, "science_gui", points + "")
								+ " (" + page + ")");
						inventories.add(gui);
						gui.setItem(0, leftArrow);
						gui.setItem(8, rightArrow);
						position = 1;
					}
				}

		// Show page.
		if (page > inventories.size())
			page = 1;
		if (page < 1)
			page = inventories.size();
		page--;
		if (inventories.get(page) != null)
			player.openInventory(inventories.get(page));
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

					if (hit != null && !MyZ.instance.isFriend(player, hit.getUniqueId())) {
						PlayerFriendEvent event = new PlayerFriendEvent(player, hit.getName());
						MyZ.instance.getServer().getPluginManager().callEvent(event);
						if (!event.isCancelled())
							MyZ.instance.addFriend(player, hit.getUniqueId());
					}
				}
			}
		}, 20L);
	}

	/**
	 * Spawn a player NPC for the given player.
	 * 
	 * @param playerDuplicate
	 *            The player to duplicate an NPC for.
	 */
	public static void spawnNPC(final Player playerDuplicate) {
		// The NPC won't even be on the screen for a second, may as well not add
		// it.
		if ((Integer) Configuration.getConfig(Configuration.LOGOUT_TIME) <= 0)
			return;

		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				final CustomEntityPlayer player = CustomEntityPlayer.newInstance(playerDuplicate);

				player.world.players.remove(player);
				MyZ.instance.getNPCs().add(player);

				MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
					@Override
					public void run() {
						MyZ.instance.getNPCs().remove(player);
						player.getBukkitEntity().remove();
					}
				}, (Integer) Configuration.getConfig(Configuration.LOGOUT_TIME) * 20L);
			}
		}, 0L);
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

		CustomEntityZombie zombie = CustomEntityZombie.newInstance(player);

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
	 * Begin a safe logout sequence for the specified player.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void startSafeLogout(Player player) {
		if (!Sync.getSafeLogoutPlayers().containsKey(player.getUniqueId())) {
			Messenger.sendConfigMessage(player, "safe_logout.beginning");
			Sync.addSafeLogoutPlayer(player);
		}
	}
}
