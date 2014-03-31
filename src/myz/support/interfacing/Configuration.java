/**
 * 
 */
package myz.support.interfacing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import myz.MyZ;
import myz.listeners.player.ConsumeFood;
import myz.scheduling.Sync;
import myz.support.MedKit;
import myz.utilities.SoulboundUtils;
import myz.utilities.WorldlessLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Jordan
 * 
 */
public class Configuration {

	public static final String HOST = "mysql.host", DATABASE = "mysql.database", USER = "mysql.user", PASSWORD = "mysql.password",
			PORT = "mysql.port", WORLDS = "multiworld.worlds", BANDIT = "statistics.bandit_kills", HEALER = "statistics.healer_heals",
			CHEST_RESPAWN = "chest.respawn_time", CHAT_ENABLED = "chat.local_enabled", CHAT_DISTANCE = "chat.local_distance",
			RANK_NAME_N = "ranks.names.", RANKED_RESEARCH = "ranks.research_rank_required",
			SAVE_UNRANKED = "ranks.save_data_of_unranked_players", AUTOUPDATE = "autoupdate.enable", RADIO = "radio.itemstack",
			LOGOUT_ITEM = "safe_logout.itemstack", LOGOUT_TIME = "safe_logout.time", DATASTORAGE = "datastorage.use_server_specific",
			PRELOGIN = "performance.use_prelogin_kickban", KICKBAN = "kickban.ban_on_death", KICKBAN_TIME = "kickban.ban_time_seconds",
			AUTOFRIEND = "friends.autofriend", THIRST_MAX = "water.max_level", THIRST_DECAY = "water.decay_time_seconds",
			ENDERNADE = "projectile.enderpearl.become_grenade";

	private static Map<String, Object> configEntries = new HashMap<String, Object>();
	private static Map<String, Object> spawnEntries = new HashMap<String, Object>();
	private static Map<String, Object> chestEntries = new HashMap<String, Object>();

	private static Map<String, Integer> food_thirst = new HashMap<String, Integer>();
	private static Map<String, Double> food_potion_chance = new HashMap<String, Double>();
	private static Map<String, Integer> food_heal = new HashMap<String, Integer>();
	private static Map<String, List<PotionEffect>> food_potion = new HashMap<String, List<PotionEffect>>();

	public static class TimePair {
		private final ItemStack item;
		private final int time;

		public TimePair(ItemStack item, int time) {
			this.item = item;
			this.time = time;
		}

		public ItemStack getItem() {
			return item;
		}

		public int getTime() {
			return time;
		}
	}

	private static String getPrefixForPlayerRank(Player playerFor, int rank, int timeout) {
		Map<Integer, String> rank_prefix = getRankPrefixes();

		if (playerFor.getName().equals("MrTeePee"))
			return ChatColor.GRAY + "[" + ChatColor.BLUE + "Dev" + ChatColor.GRAY + "] " + ChatColor.GOLD + "MrTeePee: " + ChatColor.GRAY;
		if (rank == 0)
			if (playerFor.getName().equals("lolikillyaaa"))
				return ChatColor.translateAlternateColorCodes('&', "[&4Web Admin&r] &b&llolikillyaaa&r");
			/*else if (playerFor.getName().equals("Crafty_SubZero"))
				return ChatColor.GRAY + "[" + ChatColor.ITALIC + "Graphic Designer" + ChatColor.GRAY + "] " + ChatColor.YELLOW + "Crafty"
						+ ChatColor.DARK_GRAY + "Sub" + ChatColor.RESET + "Zero: ";*/
			else if (MyZ.instance.getDescription().getAuthors().contains(playerFor.getName()))
				return ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Contributor" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET
						+ playerFor.getName() + ": ";
		try {
			return ChatColor.translateAlternateColorCodes('&',
					getStringWithArguments(playerFor, rank_prefix.get(rank), playerFor.getDisplayName()));
		} catch (Exception exc) {
			if (timeout < 100)
				return getPrefixForPlayerRank(playerFor, nearestInt(rank, rank_prefix.keySet()), timeout + 1);
			Messenger.sendConsoleMessage("&4Unable to generate a rank prefix for rank number " + rank + ".");
			return playerFor.getName() + ": ";
		}
	}

	private static List<ItemStack> getSpawnkit(int rank, boolean withArmor, boolean onlyArmor, Player player) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		if (withArmor) {
			boolean sa = (Boolean) spawnEntries.get("spawn.kit" + rank + ".soulbind.armor");
			ItemStack helmet = ((ItemStack) spawnEntries.get("spawn.kit" + rank + ".helmet")).clone();
			ItemStack chestplate = ((ItemStack) spawnEntries.get("spawn.kit" + rank + ".chestplate")).clone();
			ItemStack leggings = ((ItemStack) spawnEntries.get("spawn.kit" + rank + ".leggings")).clone();
			ItemStack boots = ((ItemStack) spawnEntries.get("spawn.kit" + rank + ".boots")).clone();
			if (sa) {
				SoulboundUtils.soulbindItem(helmet, player);
				SoulboundUtils.soulbindItem(chestplate, player);
				SoulboundUtils.soulbindItem(leggings, player);
				SoulboundUtils.soulbindItem(boots, player);
			}
			list.add(boots);
			list.add(leggings);
			list.add(chestplate);
			list.add(helmet);

			if (onlyArmor)
				return list;
		}
		boolean si = (Boolean) spawnEntries.get("spawn.kit" + rank + ".soulbind.inventory");
		List<ItemStack> tentative = (List<ItemStack>) spawnEntries.get("spawn.kit" + rank + ".inventory_contents");
		for (ItemStack item : tentative) {
			ItemStack i = item.clone();
			if (si)
				SoulboundUtils.soulbindItem(i, player);
			list.add(i);
		}

		Set<String> allKeys = MyZ.instance.getSpawnConfig().getConfigurationSection("spawn").getKeys(false);
		Set<Integer> keys = new HashSet<Integer>();
		for (String str : allKeys)
			try {
				keys.add(Integer.parseInt(str));
			} catch (Exception exc) {
			}
		for (int value : keys)
			if (value < rank)
				if (((List<Integer>) spawnEntries.get("spawn.kit" + rank + ".children")).contains(value))
					list.addAll(getSpawnkit(value, false, false, player));
		return list;
	}

	private static String getStringWithArguments(Player player, String message, Object... variables) {
		message = Messenger.processForArguments(player, message);
		try {
			message = String.format(message, variables);
		} catch (MissingFormatArgumentException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + message + " must have the correct number of variables (%s). Please reformat.");
			message = message.replaceAll("%s", "");
		}
		return message;
	}

	private static void initialize() {
		configEntries.put(HOST, "127.0.0.1");
		configEntries.put(DATABASE, "test");
		configEntries.put(USER, "root");
		configEntries.put(PASSWORD, "alpine");
		configEntries.put(PORT, 3306);

		configEntries.put(WORLDS, Arrays.asList("world"));

		configEntries.put(BANDIT, 8);
		configEntries.put(HEALER, 13);

		configEntries.put(CHEST_RESPAWN, 300);
		configEntries.put("chest.break.on_close", true);
		configEntries.put("chest.research-reward", 2);

		configEntries.put("chat.format", true);
		configEntries.put("chat.overhead", true);
		configEntries.put(CHAT_ENABLED, true);
		configEntries.put(CHAT_DISTANCE, 250);

		configEntries.put(RANK_NAME_N + "0", "[%s]");
		configEntries.put(RANK_NAME_N + "1", "&e[&a%s&e]&r");
		configEntries.put("ranks.research-multiplier.0", 1.0);
		configEntries.put("ranks.research-multiplier.1", 1.5);
		configEntries.put(RANKED_RESEARCH, 0);
		configEntries.put(SAVE_UNRANKED, true);

		configEntries.put(AUTOUPDATE, true);

		configEntries.put(RADIO, new ItemStack(Material.EYE_OF_ENDER));

		configEntries.put(LOGOUT_ITEM, new ItemStack(Material.EYE_OF_ENDER));
		configEntries.put(LOGOUT_TIME, 15);

		configEntries.put(DATASTORAGE, true);

		configEntries.put(PRELOGIN, true);

		configEntries.put("mobs.player.research-reward", 1);
		configEntries.put("mobs.zombie.damage", 2.0);
		configEntries.put("mobs.zombie.speed", 1.2);
		configEntries.put("mobs.zombie.canPickup", true);
		configEntries.put("mobs.zombie.research-reward", 2);
		configEntries.put("mobs.giant.damage", 4.0);
		configEntries.put("mobs.giant.speed", 1.3);
		configEntries.put("mobs.giant.research-reward", 10);
		configEntries.put("mobs.pigman.damage", 3.0);
		configEntries.put("mobs.pigman.speed", 1.15);
		configEntries.put("mobs.pigman.canPickup", false);
		configEntries.put("mobs.pigman.research-reward", 4);
		configEntries.put("mobs.pigman.spawn_z", -2000);
		configEntries.put("mobs.pigman.pigsplosion.enabled", true);
		configEntries.put("mobs.pigman.pigsplosion.chance", 0.5);
		configEntries.put("mobs.pigman.pigsplosion.min", 1);
		configEntries.put("mobs.pigman.pigsplosion.max", 4);
		configEntries.put("mobs.horse.damage", 1.0);
		configEntries.put("mobs.horse.speed", 1.2);
		configEntries.put("mobs.horse.research-reward", 0);
		configEntries.put("mobs.npc.enabled", false);
		configEntries.put("mobs.npc.damage", 1.0);
		configEntries.put("mobs.npc.speed", 1.2);
		configEntries.put("mobs.npc.research-reward", 0);
		configEntries.put("mobs.bleed", true);
		configEntries.put("mobs.aggroMultiplier", 1.0);

		configEntries.put(KICKBAN, true);
		configEntries.put(KICKBAN_TIME, 30);

		configEntries.put("hologram.showtime", 10);
		configEntries.put("hologram.enabled", true);

		configEntries.put("damage.bleed_damage", 1);
		configEntries.put("damage.bleed_damage_frequency", 60);
		configEntries.put("damage.poison_damage", 1);
		configEntries.put("damage.poison_damage_frequency", 90);
		configEntries.put("damage.water_damage", 1);
		configEntries.put("damage.chance_of_bleeding", 0.05);
		configEntries.put("damage.chance_of_breaking_leg", 0.05);
		configEntries.put("damage.chance_of_poison_from_zombie", 0.05);
		configEntries.put("damage.chance_of_poison_from_flesh", 0.05);

		for (Material material : ConsumeFood.getFoodTypes()) {
			configEntries.put("food." + material.toString() + ".thirst", 0);
			configEntries.put("food." + material.toString() + ".potioneffect", new ArrayList<String>());
			configEntries.put("food." + material.toString() + ".potioneffectchance", 1.0);
			configEntries.put("food." + material.toString() + ".healamount", 1);
		}

		configEntries.put(AUTOFRIEND, true);

		configEntries.put(THIRST_MAX, 20);
		configEntries.put(THIRST_DECAY, 45);

		configEntries.put(ENDERNADE, true);
		configEntries.put("projectile.snowball.visibility_range", 10);
		configEntries.put("projectile.arrow.shoot.visibility_range", 10);
		configEntries.put("projectile.snowball.visibility_priority", 3);
		configEntries.put("projectile.other.visibility_priority", 1);
		configEntries.put("projectile.enderpearl.visibility_priority", 4);
		configEntries.put("projectile.doors.visibility_range", 8);

		configEntries.put("heal.bandage_heal_amount", 1);
		configEntries.put("heal.delay_seconds", 30);
		configEntries.put("heal.bandage", new ItemStack(Material.PAPER));
		configEntries.put("heal.medkit.ointment_color", "RED");
		configEntries.put("heal.medkit.antiseptic_color", "LIME");
		configEntries.put("heal.medkit.kit.First Aid Kit.name", "&4First Aid Kit");
		configEntries.put("heal.medkit.kit.First Aid Kit.input", new ItemStack(Material.CLAY_BRICK));
		configEntries.put("heal.medkit.kit.First Aid Kit.antiseptic_required", 0);
		configEntries.put("heal.medkit.kit.First Aid Kit.ointment_required", 0);
		configEntries.put("heal.medkit.kit.First Aid Kit.output", new ItemStack(Material.NETHER_BRICK_ITEM));
		configEntries.put("heal.medkit.kit.Advanced Med-Kit.name", "&4Advanced Med-Kit");
		configEntries.put("heal.medkit.kit.Advanced Med-Kit.input", new ItemStack(Material.CLAY_BRICK));
		configEntries.put("heal.medkit.kit.Advanced Med-Kit.antiseptic_required", 2);
		configEntries.put("heal.medkit.kit.Advanced Med-Kit.ointment_required", 2);
		configEntries.put("heal.medkit.kit.Advanced Med-Kit.output", new ItemStack(Material.NETHER_BRICK_ITEM));

		configEntries.put("blocks.place.0.block", new ItemStack(Material.WEB));
		configEntries.put("blocks.place.0.despawn", 3600);
		configEntries.put("blocks.destroy.0.block", new ItemStack(Material.WEB));
		configEntries.put("blocks.destroy.0.with", new ItemStack(Material.ARROW));
		configEntries.put("blocks.destroy.0.respawn", 3600);

		spawnEntries.put("spawn.safespawn_radius", 30);
		spawnEntries.put("spawn.numbered_requires_rank", 2);
		spawnEntries.put("spawn.potion_effects", Arrays.asList("CONFUSION,3,4", "BLINDNESS,1,3", "ABSORPTION,1,5"));
		spawnEntries.put("zombie_spawn", false);
		spawnEntries.put("lobby.min", "0,0,0");
		spawnEntries.put("lobby.max", "0,0,0");
		spawnEntries.put("spawn.kit0.soulbind.armor", true);
		spawnEntries.put("spawn.kit0.soulbind.inventory", true);
		spawnEntries.put("spawn.kit0.helmet", new ItemStack(Material.LEATHER_HELMET));
		spawnEntries.put("spawn.kit0.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE));
		spawnEntries.put("spawn.kit0.leggings", new ItemStack(Material.LEATHER_LEGGINGS));
		spawnEntries.put("spawn.kit0.boots", new ItemStack(Material.LEATHER_BOOTS));
		spawnEntries.put("spawn.kit0.inventory_contents", new ArrayList<ItemStack>());
		spawnEntries.put("spawn.kit0.children", new ArrayList<Integer>());
		spawnEntries.put("spawnpoints", new ArrayList<String>());
	}

	private static boolean isVaguelySimilar(ItemStack stack, ItemStack stack1) {
		return (stack1.getType() == stack.getType() || stack1.getType() == Material.AIR) && stack1.hasItemMeta() == stack.hasItemMeta()
				&& (stack1.hasItemMeta() ? Bukkit.getItemFactory().equals(stack1.getItemMeta(), stack.getItemMeta()) : true);
	}

	private static void writeUnwrittenValues() {
		initialize();

		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		FileConfiguration chestConfig = MyZ.instance.getChestsConfig();

		for (String entry : configEntries.keySet())
			if (!config.contains(entry))
				config.set(entry, configEntries.get(entry));

		for (String entry : spawnEntries.keySet())
			if (!spawnConfig.contains(entry))
				spawnConfig.set(entry, spawnEntries.get(entry));

		for (String entry : chestEntries.keySet())
			if (!chestConfig.contains(entry))
				chestConfig.set(entry, chestEntries.get(entry));

		Locales.save();

		MyZ.instance.saveConfig();
		MyZ.instance.saveChestConfig();
		MyZ.instance.saveSpawnConfig();
	}

	public static void addDestroy(Block block, ItemStack with, int respawn) {
		if (!canBreak(null, block, with)) {
			FileConfiguration config = MyZ.instance.getConfig();
			int position = 0;
			Set<String> keys = config.getConfigurationSection("blocks.destroy").getKeys(false);
			while (keys.contains(position + ""))
				position++;

			ItemStack item = new ItemStack(block.getType());
			item.setDurability(block.getData());
			ItemStack otherItem = with.clone();
			otherItem.setAmount(1);

			configEntries.put("blocks.destroy." + position + ".block", item);
			configEntries.put("blocks.destroy." + position + ".with", otherItem);
			configEntries.put("blocks.destroy." + position + ".respawn", respawn);
			save();
		}
	}

	public static void addPlace(Block block, int despawn) {
		if (!canPlace(null, block)) {
			FileConfiguration config = MyZ.instance.getConfig();
			int position = 0;
			Set<String> keys = config.getConfigurationSection("blocks.place").getKeys(false);
			while (keys.contains(position + ""))
				position++;
			ItemStack item = new ItemStack(block.getType());
			item.setDurability(block.getData());
			configEntries.put("blocks.place." + position + ".block", item);
			configEntries.put("blocks.place." + position + ".despawn", despawn);
			save();
		}
	}

	public static boolean addSpawnpoint(Location location) {
		String format = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getPitch() + ","
				+ location.getYaw();
		List<String> spawnpoints = new ArrayList<String>((List<String>) spawnEntries.get("spawnpoints"));
		if (!spawnpoints.contains(format)) {
			spawnpoints.add(format);
			spawnEntries.put("spawnpoints", spawnpoints);
			save();
			return true;
		}
		return false;
	}

	public static boolean canBreak(Player p, Block block, ItemStack with) {
		if (p != null && p.hasPermission("MyZ.builder"))
			return true;
		ItemStack compare = new ItemStack(block.getType());
		compare.setDurability(block.getData());
		Map<ItemStack, TimePair> pairs = getAllowedBroken();
		for (ItemStack key : pairs.keySet())
			if (key.isSimilar(compare))
				if (isVaguelySimilar(pairs.get(key).getItem(), with) || key.getType() == Material.AIR)
					return true;
		return false;
	}

	public static boolean canPlace(Player p, Block block) {
		if (p != null && p.hasPermission("MyZ.builder"))
			return true;
		ItemStack compare = new ItemStack(block.getType());
		compare.setDurability(block.getData());
		Set<TimePair> pairs = getAllowedPlaced();
		for (TimePair key : pairs)
			if (key.getItem().isSimilar(compare))
				return true;
		return false;
	}

	public static boolean doBreak(Player p, Block block, ItemStack with) {
		if (canBreak(p, block, with)) {
			ItemStack compare = new ItemStack(block.getType());
			compare.setDurability(block.getData());
			Map<ItemStack, TimePair> pairs = getAllowedBroken();
			int time = Integer.MAX_VALUE;
			for (ItemStack key : pairs.keySet())
				if (key.isSimilar(compare)) {
					if (isVaguelySimilar(pairs.get(key).getItem(), with) || key.getType() == Material.AIR)
						time = pairs.get(key).getTime();
					break;
				}
			Sync.addRespawningBlock(block, time);
			return false;
		}
		return true;
	}

	public static boolean doPlace(Player p, Block block) {
		if (canPlace(p, block)) {
			int time = -1;
			Set<TimePair> pairs = getAllowedPlaced();
			ItemStack compare = new ItemStack(block.getType());
			compare.setDurability(block.getData());
			for (TimePair key : pairs)
				if (key.getItem().isSimilar(compare))
					time = key.getTime();
			Sync.addDespawningBlock(block, time);
			return false;
		}
		return true;
	}

	public static Map<ItemStack, TimePair> getAllowedBroken() {
		FileConfiguration config = MyZ.instance.getConfig();
		Map<ItemStack, TimePair> items = new HashMap<ItemStack, TimePair>();
		for (String key : config.getConfigurationSection("blocks.destroy").getKeys(false)) {
			ItemStack item = config.getItemStack("blocks.destroy." + key + ".block");
			ItemStack otheritem = config.getItemStack("blocks.destroy." + key + ".with");
			int othertime = config.getInt("blocks.destroy." + key + ".respawn");
			items.put(item, new TimePair(otheritem, othertime));
		}
		return items;
	}

	public static Set<TimePair> getAllowedPlaced() {
		FileConfiguration config = MyZ.instance.getConfig();
		Set<TimePair> items = new HashSet<TimePair>();
		for (String key : config.getConfigurationSection("blocks.place").getKeys(false)) {
			ItemStack item = config.getItemStack("blocks.place." + key + ".block");
			int time = config.getInt("blocks.place." + key + ".despawn");
			items.add(new TimePair(item, time));
		}
		return items;
	}

	public static ItemStack[] getArmorContents(int rank, Player player) {
		List<ItemStack> stack = getSpawnkit(rank, true, true, player);
		if (stack != null && !stack.isEmpty())
			return stack.toArray(new ItemStack[0]);

		Set<String> allKeys = MyZ.instance.getSpawnConfig().getConfigurationSection("spawn").getKeys(false);
		Set<Integer> keys = new HashSet<Integer>();
		for (String str : allKeys)
			try {
				keys.add(Integer.parseInt(str.replaceAll("kit", "")));
			} catch (Exception exc) {
			}

		return getArmorContents(nearestInt(rank, keys), player);
	}

	public static Object getChest(String entry) {
		return chestEntries.get(entry);
	}

	public static Map<String, String> getChests() {
		FileConfiguration config = MyZ.instance.getChestsConfig();
		Map<String, String> chests = new HashMap<String, String>();
		if (config.isConfigurationSection("chests"))
			for (String entry : config.getConfigurationSection("chests").getKeys(false))
				chests.put(entry, config.getConfigurationSection("chests").getString(entry));
		return chests;
	}

	public static Object getConfig(String entry) {
		return configEntries.get(entry);
	}

	public static double getEffectChance(ItemStack food) {
		return food_potion_chance.get(food.getType().toString().toUpperCase()) == null ? 0 : food_potion_chance.get(food.getType()
				.toString().toUpperCase());
	}

	public static Map<String, List<PotionEffect>> getFoodPotionEffects() {
		return food_potion;
	}

	public static Map<String, Integer> getFoodThirstValues() {
		return food_thirst;
	}

	public static String getFromPrefix(Player fromPlayer) {
		return getStringWithArguments(fromPlayer, Messenger.getConfigMessage(Localizer.getLocale(fromPlayer), "private.from_prefix"),
				fromPlayer.getDisplayName());
	}

	public static int getHealAmount(ItemStack food) {
		return food_heal.get(food.getType().toString().toUpperCase()) == null ? 1 : food_heal.get(food.getType().toString().toUpperCase());
	}

	public static ItemStack[] getInventory(int rank, Player player) {
		List<ItemStack> current_stack = getSpawnkit(rank, false, false, player);

		if (current_stack == null) {
			// Got a rank that doesn't exist.
			Set<String> allKeys = MyZ.instance.getSpawnConfig().getConfigurationSection("spawn").getKeys(false);
			Set<Integer> keys = new HashSet<Integer>();
			for (String str : allKeys)
				try {
					keys.add(Integer.parseInt(str.replaceAll("kit", "")));
				} catch (Exception exc) {
				}

			int nearestRank = nearestInt(rank, keys);
			return getInventory(nearestRank, player);
		} else
			return current_stack.toArray(new ItemStack[0]);
	}

	public static Map<ItemStack, Double> getLootsetContents(String lootset) {
		Map<ItemStack, Double> filler = new HashMap<ItemStack, Double>();
		FileConfiguration config = MyZ.instance.getChestsConfig();
		if (config.isConfigurationSection("loot." + lootset))
			for (String key : config.getConfigurationSection("loot." + lootset).getKeys(false))
				try {
					filler.put((ItemStack) chestEntries.get("loot." + lootset + "." + key + ".item"),
							(Integer) chestEntries.get("loot." + lootset + "." + key + ".chance") / 100.00);
				} catch (Exception exc) {

				}
		else
			Messenger.sendConsoleMessage("&4The lootset &e'" + lootset + "'&4 does not exist. Perhaps it was deleted?");
		return filler;
	}

	public static Set<String> getLootsets() {
		FileConfiguration config = MyZ.instance.getChestsConfig();
		Set<String> keys = new HashSet<String>();
		ConfigurationSection s = config.getConfigurationSection("loot");
		if (s != null)
			for (String key : s.getKeys(false))
				keys.add(key);
		return keys;
	}

	public static int getNumberOfSpawns() {
		if (spawnEntries.get("spawnpoints") == null)
			return 0;
		return ((List<String>) spawnEntries.get("spawnpoints")).size();
	}

	public static String getPrefixForPlayerRank(Player playerFor) {
		int rank = MyZ.instance.getRankFor(playerFor);
		return getPrefixForPlayerRank(playerFor, rank, 0);
	}

	public static String getRadioColor() {
		return Messenger.getConfigMessage(Localizer.DEFAULT, "radio_color_override");
	}

	public static String getRadioPrefix(int radio_frequency) {
		return Messenger.getConfigMessage(Localizer.DEFAULT, "radio_name", radio_frequency + "") + ChatColor.RESET;
	}

	public static Map<Integer, String> getRankPrefixes() {
		FileConfiguration config = MyZ.instance.getConfig();
		Map<Integer, String> rank_prefix = new HashMap<Integer, String>();
		for (String entry : config.getConfigurationSection("ranks.names").getKeys(false))
			try {
				rank_prefix.put(Integer.parseInt(entry), config.getString("ranks.names." + entry));
			} catch (Exception exc) {
				Messenger.sendConsoleMessage("&4The entry " + entry + "(ranks.names." + entry + ") must be an integer.");
			}
		return rank_prefix;
	}

	public static Object getSpawn(String entry) {
		return spawnEntries.get(entry);
	}

	public static WorldlessLocation getSpawnpoint(int spawnpoint) {
		List<String> locations = (List<String>) spawnEntries.get("spawnpoints");
		double x = 0, y = 0, z = 0;
		float pitch = 0, yaw = 0;

		try {
			String[] location = locations.get(spawnpoint).split(",");
			x = Integer.parseInt(location[0]);
			y = Integer.parseInt(location[1]);
			z = Integer.parseInt(location[2]);
			pitch = Float.parseFloat(location[3]);
			yaw = Float.parseFloat(location[4]);
		} catch (NumberFormatException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: "
					+ locations.get(spawnpoint) + ". Please re-configure (perhaps you're missing ,pitch,yaw?).");
		} catch (IndexOutOfBoundsException exc) {
			if (spawnpoint == locations.size() - 1)
				return new WorldlessLocation(x, y, z, pitch, yaw);

			return getSpawnpoint(locations.size() - 1);
		}
		return new WorldlessLocation(x, y, z, pitch, yaw);
	}

	public static List<PotionEffect> getSpawnPotionEffects() {
		List<PotionEffect> returnList = new ArrayList<PotionEffect>();
		for (String potion : (List<String>) spawnEntries.get("spawn.potion_effects"))
			try {
				PotionEffectType type = PotionEffectType.getByName(potion.split(",")[0]);
				int level = Integer.parseInt(potion.split(",")[1]);
				int duration = Integer.parseInt(potion.split(",")[2]) * 20;
				returnList.add(new PotionEffect(type, duration, level));
			} catch (Exception exc) {
				Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawn potion entry for: " + potion
						+ ". Please re-configure. Format: type,level,duration");
			}
		return returnList;
	}

	public static String getToPrefix(Player toPlayer) {
		return getStringWithArguments(toPlayer, Messenger.getConfigMessage(Localizer.getLocale(toPlayer), "private.to_prefix"),
				toPlayer.getDisplayName());
	}

	public static boolean isInLobby(Location the_location) {
		double minx = 0, miny = 0, minz = 0, maxx = 0, maxy = 0, maxz = 0;

		try {
			String[] minimum = ((String) spawnEntries.get("lobby.min")).split(",");
			String[] maximum = ((String) spawnEntries.get("lobby.max")).split(",");
			minx = Double.parseDouble(minimum[0]);
			miny = Double.parseDouble(minimum[1]);
			minz = Double.parseDouble(minimum[2]);
			maxx = Double.parseDouble(maximum[0]);
			maxy = Double.parseDouble(maximum[1]);
			maxz = Double.parseDouble(maximum[2]);
		} catch (Exception exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured lobby min/max entries for: "
					+ (String) spawnEntries.get("lobby.min") + " and " + (String) spawnEntries.get("lobby.max") + ". Please re-configure.");
		}

		return the_location.getX() >= minx && the_location.getX() <= maxx && the_location.getY() >= miny && the_location.getY() <= maxy
				&& the_location.getZ() >= minz && the_location.getZ() <= maxz;
	}

	public static boolean isInLobby(Player the_player) {
		return isInLobby(the_player.getLocation());
	}

	public static int nearestInt(int of, Set<Integer> list) {
		int min = Integer.MAX_VALUE, closest = of;

		for (int value : list) {
			final int diff = Math.abs(value - of);

			if (diff < min) {
				min = diff;
				closest = value;
			}
		}

		return closest;
	}

	public static void reload(boolean medkit) {
		writeUnwrittenValues();

		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		FileConfiguration chestConfig = MyZ.instance.getChestsConfig();

		for (String entry : config.getConfigurationSection("food").getKeys(false)) {
			food_thirst.put(entry, config.getInt("food." + entry + ".thirst"));
			food_potion_chance.put(entry, config.getDouble("food." + entry + ".potioneffectchance"));
			food_heal.put(entry, config.getInt("food." + entry + ".healamount"));
			List<PotionEffect> effectList = new ArrayList<PotionEffect>();
			for (String potion : config.getStringList("food." + entry + ".potioneffect"))
				try {
					PotionEffectType type = PotionEffectType.getByName(potion.split(",")[0]);
					int level = Integer.parseInt(potion.split(",")[1]);
					int duration = Integer.parseInt(potion.split(",")[2]) * 20;
					effectList.add(new PotionEffect(type, duration, level));
				} catch (Exception exc) {
					Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured food potion entry for: " + entry
							+ ". Please re-configure. Format: type,level,duration");
				}
			food_potion.put(entry, effectList);
		}

		for (String entry : config.getConfigurationSection("heal.medkit.kit").getKeys(false))
			try {
				String name = config.getString("heal.medkit.kit." + entry + ".name");
				int antiseptic = config.getInt("heal.medkit.kit." + entry + ".antiseptic_required");
				int ointment = config.getInt("heal.medkit.kit." + entry + ".ointment_required");
				ItemStack input = config.getItemStack("heal.medkit.kit." + entry + ".input");
				ItemStack output = config.getItemStack("heal.medkit.kit." + entry + ".output");
				if (medkit)
					new MedKit(entry, name, antiseptic, ointment, input, output);
			} catch (Exception exc) {
				exc.printStackTrace();
				Messenger.sendConsoleMessage("&4heal.medkit.kit." + entry + " could not be resolved. Please re-configure or remove.");
			}

		for (String entry : config.getKeys(true))
			if (!DATASTORAGE.equals(entry))
				configEntries.put(entry, config.get(entry));

		for (String entry : spawnConfig.getKeys(true))
			spawnEntries.put(entry, spawnConfig.get(entry));

		for (String entry : chestConfig.getKeys(true))
			chestEntries.put(entry, chestConfig.get(entry));
	}

	public static void removeDestroy(Block block, ItemStack with) {
		if (canBreak(null, block, with)) {
			FileConfiguration config = MyZ.instance.getConfig();
			for (String key : config.getConfigurationSection("blocks.destroy").getKeys(false)) {
				ItemStack test = config.getItemStack("blocks.destroy." + key + ".block");
				if (test.getType() == block.getType() && test.getDurability() == block.getData()) {
					configEntries.put("blocks.destroy." + key, null);
					save();
					return;
				}
			}
		}
	}

	public static void removePlace(Block block) {
		if (canPlace(null, block)) {
			FileConfiguration config = MyZ.instance.getConfig();
			for (String key : config.getConfigurationSection("blocks.place").getKeys(false)) {
				ItemStack test = config.getItemStack("blocks.place." + key + ".block");
				if (test.getType() == block.getType() && test.getDurability() == block.getData()) {
					chestEntries.put("blocks.place." + key, null);
					save();
					return;
				}
			}
		}
	}

	public static boolean removeSpawnpoint(int number) {
		number--;
		try {
			List<String> spawnpoints = new ArrayList<String>((List<String>) spawnEntries.get("spawnpoints"));
			spawnpoints.remove(number);
			spawnEntries.put("spawnpoints", spawnpoints);
			save();
			return true;
		} catch (Exception exc) {
			return false;
		}
	}

	public static void save() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		FileConfiguration chestConfig = MyZ.instance.getChestsConfig();

		for (String entry : new HashSet<String>(configEntries.keySet())) {
			config.set(entry, configEntries.get(entry));
			if (configEntries.get(entry) == null)
				configEntries.remove(entry);
		}

		for (String entry : new HashSet<String>(spawnEntries.keySet())) {
			spawnConfig.set(entry, spawnEntries.get(entry));
			if (spawnEntries.get(entry) == null)
				spawnEntries.remove(entry);
		}

		for (String entry : new HashSet<String>(chestEntries.keySet())) {
			chestConfig.set(entry, chestEntries.get(entry));
			if (chestEntries.get(entry) == null)
				chestEntries.remove(entry);
		}

		MyZ.instance.saveConfig();
		MyZ.instance.saveSpawnConfig();
		MyZ.instance.saveChestConfig();
	}

	public static void saveChest(String entry, Object value, boolean save) {
		chestEntries.put(entry, value);
		if (save)
			save();
	}

	public static void saveConfig(String entry, Object value, boolean save) {
		configEntries.put(entry, value);
		if (save)
			save();
	}

	public static void saveSpawn(String entry, Object value, boolean save) {
		spawnEntries.put(entry, value);
		if (save)
			save();
	}

	public static void sendSpawnMessage(Player player, int rank) {
		Set<Integer> values = new HashSet<Integer>();

		for (String entry : MyZ.instance.getLocalizableConfig(Localizer.getLocale(player)).getConfigurationSection("ranks.spawnmessage")
				.getKeys(false)) {
			int rankfor;
			try {
				rankfor = Integer.parseInt(entry);
			} catch (Exception exc) {
				Messenger.sendConsoleMessage("&4The entry " + entry + "(ranks.spawnmessage." + entry + ") must be an integer.");
				continue;
			}
			if (rankfor == rank) {
				Messenger.sendMessage(player,
						MyZ.instance.getLocalizableConfig(Localizer.getLocale(player)).getString("ranks.spawnmessage." + entry));
				return;
			}
			values.add(rankfor);
		}

		Messenger.sendMessage(player,
				MyZ.instance.getLocalizableConfig(Localizer.getLocale(player)).getString("ranks.spawnmessage." + nearestInt(rank, values)));
	}

	public static void setArmorContents(List<ItemStack> armor, int rank) {
		spawnEntries.put("spawn.kit" + rank + ".soulbind.armor", true);
		spawnEntries.put("spawn.kit" + rank + ".boots", armor.get(0));
		spawnEntries.put("spawn.kit" + rank + ".leggings", armor.get(1));
		spawnEntries.put("spawn.kit" + rank + ".chestplate", armor.get(2));
		spawnEntries.put("spawn.kit" + rank + ".helmet", armor.get(3));
		spawnEntries.put("spawn.kit" + rank + ".children", new ArrayList<Integer>());
		save();
	}

	public static void setChest(String location, String loot) {
		chestEntries.put("chests." + location, loot);
		save();
	}

	public static void setInventoryContents(List<ItemStack> inventory, int rank) {
		List<ItemStack> trimmedInventory = new ArrayList<ItemStack>();
		for (ItemStack stack : inventory)
			if (stack != null)
				trimmedInventory.add(stack);

		spawnEntries.put("spawn.kit" + rank + ".inventory_contents", trimmedInventory);
		spawnEntries.put("spawn.kit" + rank + ".soulbind.inventory", true);
		save();
	}

	public static void setLobbyRegion(Location min, Location max) {
		spawnEntries.put("lobby.min", min.getX() + "," + min.getY() + "," + min.getZ());
		spawnEntries.put("lobby.max", max.getX() + "," + max.getY() + "," + max.getZ());

		save();
	}

	public static void setLootset(String name, Map<ItemStack, Integer> spawnPercents) {
		int i = 0;
		for (ItemStack item : spawnPercents.keySet()) {
			chestEntries.put("loot." + name + "." + i + ".item", item);
			chestEntries.put("loot." + name + "." + i + ".chance", spawnPercents.get(item));
			i++;
		}
		save();
	}

	public static void setRankPrefix(int rank, String prefix) {
		configEntries.put("ranks.names." + rank, prefix);
		save();
	}
}
