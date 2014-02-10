/**
 * 
 */
package myz.Support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import myz.MyZ;
import myz.Listeners.ConsumeFood;
import myz.Scheduling.Sync;
import myz.Utilities.Localizer;
import myz.Utilities.WorldlessLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

/**
 * @author Jordan
 * 
 */
public class Configuration {

	private static boolean use_playerdata, use_kickban, use_prelogin, autofriend, save_data, grenade, local_chat, is_bleed, is_auto, npc,
			zombie_spawn, zombie_pickup, pigman_pickup;
	private static String host = "", user = "", password = "", database = "", lobby_min = "0,0,0", lobby_max = "0,0,0",
			ointment_color = "", antiseptic_color = "";
	private static int water_decrease, kickban_seconds, port, safespawn_radius, max_thirst, poison_damage_frequency, spawn_radius,
			bleed_damage_frequency, healer_heals, bandit_kills, local_chat_distance, safe_logout_time, heal_delay,
			numbered_spawn_requires_rank, research_rank, chest_respawn;
	private static double bleed_chance, poison_chance_flesh, poison_chance_zombie, food_heal, poison_damage, water_damage, bleed_damage,
			zombie_speed, horse_speed, npc_speed, giant_speed, pigman_speed, zombie_damage, horse_damage, npc_damage, giant_damage,
			pigman_damage, bandage_heal;
	private static List<String> spawnpoints = new ArrayList<String>(), spawn_potion_effects = new ArrayList<String>(),
			worlds = new ArrayList<String>();
	private static ItemStack radio, safe_logout_item, bandage;

	private static Map<Integer, String> rank_prefix = new HashMap<Integer, String>();
	private static Map<Integer, ItemStack> ranked_helmet = new HashMap<Integer, ItemStack>();
	private static Map<Integer, ItemStack> ranked_chestplate = new HashMap<Integer, ItemStack>();
	private static Map<Integer, ItemStack> ranked_leggings = new HashMap<Integer, ItemStack>();
	private static Map<Integer, ItemStack> ranked_boots = new HashMap<Integer, ItemStack>();
	private static Map<Integer, ItemStack[]> ranked_inventory = new HashMap<Integer, ItemStack[]>();
	private static Map<String, Integer> food_thirst = new HashMap<String, Integer>();
	private static Map<String, List<PotionEffect>> food_potion = new HashMap<String, List<PotionEffect>>();
	private static Map<String, Double> food_potion_chance = new HashMap<String, Double>();
	private static Map<Integer, List<Integer>> ranked_kit_children = new HashMap<Integer, List<Integer>>();
	private static Map<String, String> chests = new HashMap<String, String>();

	private static Map<ItemStack, Integer> allow_place = new HashMap<ItemStack, Integer>();
	private static Map<ItemStack, DestroyPair> allow_destroy = new HashMap<ItemStack, DestroyPair>();
	private static Map<String, Map<ItemStack, Integer>> lootsets = new HashMap<String, Map<ItemStack, Integer>>();

	// TODO ensure all new values are added in reload(), writeUnwrittenValues()
	// and save()

	/**
	 * Setup this configuration object.
	 */
	public static void reload() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		FileConfiguration chestsConfig = MyZ.instance.getChestsConfig();
		writeUnwrittenValues();

		for (String entry : config.getConfigurationSection("food").getKeys(false)) {
			food_thirst.put(entry, config.getInt("food." + entry + ".thirst"));
			food_potion_chance.put(entry, config.getDouble("food." + entry + ".potioneffectchance"));
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

		zombie_pickup = config.getBoolean("mobs.zombie.canPickup");
		pigman_pickup = config.getBoolean("mobs.pigman.canPickup");
		chest_respawn = config.getInt("chest.respawn_time");
		research_rank = config.getInt("ranks.research_rank_required");
		spawn_radius = spawnConfig.getInt("prerequisites.blocks_from_chest_or_player");
		zombie_spawn = spawnConfig.getBoolean("zombie_spawn");
		is_auto = config.getBoolean("autoupdate.enable");
		worlds = new ArrayList<String>(config.getStringList("multiworld.worlds"));
		heal_delay = config.getInt("heal.delay_seconds");
		bandage = config.getItemStack("heal.bandage");
		bandage_heal = config.getDouble("heal.bandage_heal_amount");
		local_chat = config.getBoolean("chat.local_enabled");
		local_chat_distance = config.getInt("chat.local_distance");
		radio = config.getItemStack("radio.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));
		safe_logout_time = config.getInt("safe_logout.time");
		safe_logout_item = config.getItemStack("safe_logout.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));
		zombie_damage = config.getDouble("mobs.zombie.damage");
		pigman_damage = config.getDouble("mobs.pigman.damage");
		npc_damage = config.getDouble("mobs.npc.damage");
		giant_damage = config.getDouble("mobs.giant.damage");
		horse_damage = config.getDouble("mobs.horse.damage");
		bandit_kills = config.getInt("statistics.bandit_kills");
		healer_heals = config.getInt("statistics.healer_heals");
		npc = config.getBoolean("mobs.npc.enabled");
		zombie_speed = config.getDouble("mobs.zombie.speed");
		npc_speed = config.getDouble("mobs.npc.speed");
		horse_speed = config.getDouble("mobs.horse.speed");
		pigman_speed = config.getDouble("mobs.pigman.speed");
		giant_speed = config.getDouble("mobs.giant.speed");
		use_playerdata = config.getBoolean("datastorage.use_server_specific");
		use_prelogin = config.getBoolean("performance.use_prelogin_kickban");
		use_kickban = config.getBoolean("kickban.kick_on_death");
		poison_damage = config.getDouble("damage.poison_damage");
		water_damage = config.getDouble("damage.water_damage");
		bleed_damage = config.getDouble("damage.bleed_damage");
		poison_damage_frequency = config.getInt("damage.poison_damage_frequency");
		bleed_damage_frequency = config.getInt("damage.bleed_damage_frequency");
		water_decrease = config.getInt("water.decay_time_seconds");
		kickban_seconds = config.getInt("kickban.ban_time_seconds");
		bleed_chance = config.getDouble("damage.chance_of_bleeding");
		poison_chance_flesh = config.getDouble("damage.chance_of_poison_from_flesh");
		poison_chance_zombie = config.getDouble("damage.chance_of_poison_from_zombie");
		lobby_min = spawnConfig.getString("lobby.min");
		lobby_max = spawnConfig.getString("lobby.max");
		host = config.getString("mysql.host");
		database = config.getString("mysql.database");
		user = config.getString("mysql.user");
		password = config.getString("mysql.password");
		port = config.getInt("mysql.port");
		save_data = config.getBoolean("ranks.save_data_of_unranked_players");
		spawnpoints = new ArrayList<String>(spawnConfig.getStringList("spawnpoints"));
		autofriend = config.getBoolean("friends.autofriend");
		safespawn_radius = config.getInt("spawn.safespawn_radius");
		max_thirst = config.getInt("water.max_level");
		spawn_potion_effects = new ArrayList<String>(config.getStringList("spawn.potion_effects"));
		numbered_spawn_requires_rank = config.getInt("spawn.numbered_requires_rank");

		is_bleed = config.getBoolean("mobs.bleed");
		grenade = config.getBoolean("projectile.enderpearl.become_grenade");

		ointment_color = config.getString("heal.medkit.ointment_color");
		antiseptic_color = config.getString("heal.medkit.antiseptic_color");
		food_heal = config.getDouble("heal.food_heal_amount");

		ranked_helmet.put(0, spawnConfig.getItemStack("spawn.default_kit.helmet", new ItemStack(Material.LEATHER_HELMET)));
		ranked_chestplate.put(0, spawnConfig.getItemStack("spawn.default_kit.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE)));
		ranked_leggings.put(0, spawnConfig.getItemStack("spawn.default_kit.leggings", new ItemStack(Material.LEATHER_LEGGINGS)));
		ranked_boots.put(0, spawnConfig.getItemStack("spawn.default_kit.boots", new ItemStack(Material.LEATHER_BOOTS)));
		ranked_kit_children.put(0, spawnConfig.getIntegerList("spawn.default_kit.children"));

		try {
			ranked_inventory.put(0, spawnConfig.getList("spawn.default_kit.inventory_contents").toArray(new ItemStack[0]));
		} catch (Exception exc) {
			Messenger.sendConsoleMessage(ChatColor.RED
					+ "spawn.default.kit.inventory.contents could not be resolved to a list of items. Please re-configure or remove.");
		}

		for (String entry : config.getConfigurationSection("ranks.names").getKeys(false))
			try {
				rank_prefix.put(Integer.parseInt(entry), config.getString("ranks.names." + entry));
			} catch (Exception exc) {
				Messenger.sendConsoleMessage("&4The entry " + entry + "(ranks.names." + entry + ") must be an integer.");
			}

		for (String entry : config.getConfigurationSection("heal.medkit.kit").getKeys(false))
			try {
				String name = config.getString("heal.medkit.kit." + entry + ".name");
				int antiseptic = config.getInt("heal.medkit.kit." + entry + ".antiseptic_required");
				int ointment = config.getInt("heal.medkit.kit." + entry + ".ointment_required");
				ItemStack input = config.getItemStack("heal.medkit.kit." + entry + ".input");
				ItemStack output = config.getItemStack("heal.medkit.kit." + entry + ".output");
				new MedKit(entry, name, antiseptic, ointment, input, output);
			} catch (Exception exc) {
				exc.printStackTrace();
				Messenger.sendConsoleMessage("&4heal.medkit.kit." + entry + " could not be resolved. Please re-configure or remove.");
			}

		for (String entry : spawnConfig.getConfigurationSection("spawn").getKeys(false))
			if (entry.startsWith("kit_"))
				try {
					int position = Integer.parseInt(entry.replace("kit_", ""));
					ranked_helmet.put(position,
							spawnConfig.getItemStack("spawn.kit_" + position + ".helmet", new ItemStack(Material.LEATHER_HELMET)));
					ranked_chestplate.put(position,
							spawnConfig.getItemStack("spawn.kit_" + position + ".chestplate", new ItemStack(Material.LEATHER_CHESTPLATE)));
					ranked_leggings.put(position,
							spawnConfig.getItemStack("spawn.kit_" + position + ".leggings", new ItemStack(Material.LEATHER_LEGGINGS)));
					ranked_boots.put(position,
							spawnConfig.getItemStack("spawn.kit_" + position + ".boots", new ItemStack(Material.LEATHER_BOOTS)));
					ranked_inventory.put(position,
							spawnConfig.getList("spawn.kit_" + position + ".inventory_contents").toArray(new ItemStack[0]));
					ranked_kit_children.put(position, spawnConfig.getIntegerList("spawn.kit_" + position + ".children"));
				} catch (Exception exc) {
					Messenger.sendConsoleMessage("&4spawn.kit_" + entry + " could not be resolved. Please re-configure or remove.");
				}

		for (String entry : config.getConfigurationSection("blocks.place").getKeys(false)) {
			ItemStack block = config.getItemStack("blocks.place." + entry + ".block", new ItemStack(Material.AIR));
			allow_place.put(block, config.getInt("blocks.place." + entry + ".despawn"));
		}

		for (String entry : config.getConfigurationSection("blocks.destroy").getKeys(false)) {
			ItemStack block = config.getItemStack("blocks.destroy." + entry + ".block", new ItemStack(Material.AIR));
			ItemStack with = config.getItemStack("blocks.destroy." + entry + ".with", new ItemStack(Material.AIR));
			DestroyPair pair = new DestroyPair(with, config.getInt("blocks.destroy." + entry + ".respawn"));
			allow_destroy.put(block, pair);
		}

		for (String entry : chestsConfig.getConfigurationSection("chests").getKeys(false))
			chests.put(entry, chestsConfig.getConfigurationSection("chests").getString(entry));

		for (String entry : chestsConfig.getConfigurationSection("loot").getKeys(false)) {
			Map<ItemStack, Integer> loot = new HashMap<ItemStack, Integer>();
			for (int i = 0; i <= 27; i++)
				if (chestsConfig.getConfigurationSection("loot." + entry).isSet(i + ""))
					loot.put(
							chestsConfig.getConfigurationSection("loot." + entry + "." + i).getItemStack("item",
									new ItemStack(Material.AIR)),
							chestsConfig.getConfigurationSection("loot." + entry + "." + i).getInt("chance"));
			lootsets.put(entry, loot);
		}
	}

	/**
	 * Write in all missing values.
	 * 
	 * @param config
	 *            The FileConfiguration to write into.
	 */
	private static void writeUnwrittenValues() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		FileConfiguration chestsConfig = MyZ.instance.getChestsConfig();

		// MySQL begin.
		if (!config.contains("mysql.host"))
			config.set("mysql.host", "127.0.0.1");
		if (!config.contains("mysql.database"))
			config.set("mysql.database", "test");
		if (!config.contains("mysql.user"))
			config.set("mysql.user", "root");
		if (!config.contains("mysql.password"))
			config.set("mysql.password", "alpine");
		if (!config.contains("mysql.port"))
			config.set("mysql.port", 3306);

		// Multiworld begin.
		if (!config.contains("multiworld.worlds")) {
			List<String> worldList = new ArrayList<String>();
			worldList.add(MyZ.instance.getServer().getWorlds().get(0).getName());
			config.set("multiworld.worlds", worldList);
		}

		// Statistics begin.
		if (!config.contains("statistics.bandit_kills"))
			config.set("statistics.bandit_kills", 8);
		if (!config.contains("statistics.healer_heals"))
			config.set("statistics.healer_heals", 13);

		// Chests begin.
		if (!chestsConfig.contains("chests"))
			chestsConfig.createSection("chests");
		if (!chestsConfig.contains("loot"))
			chestsConfig.createSection("loot");

		if (!config.contains("chest.respawn_time"))
			config.set("chest.respawn_time", 300);

		// Chat begin.
		if (!config.contains("chat.local_enabled"))
			config.set("chat.local_enabled", true);
		if (!config.contains("chat.local_distance"))
			config.set("chat.local_distance", 250);

		// Ranks begin.
		if (!config.contains("ranks.names.0"))
			config.set("ranks.names.0", "[%s]");
		if (!config.contains("ranks.research_rank_required"))
			config.set("ranks.research_rank_required", 0);

		// AutoUpdate begin.
		if (!config.contains("autoupdate.enable"))
			config.set("autoupdate.enable", true);

		// Radio begin.
		if (!config.contains("radio.itemstack"))
			config.set("radio.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));

		// Logout begin.
		if (!config.contains("safe_logout.itemstack"))
			config.set("safe_logout.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));
		if (!config.contains("safe_logout.time"))
			config.set("safe_logout.time", 15);

		// Datastore begin.
		if (!config.contains("datastorage.use_server_specific"))
			config.set("datastorage.use_server_specific", true);

		// Performance begin.
		if (!config.contains("performance.use_prelogin_kickban"))
			config.set("performance.use_prelogin_kickban", true);

		// Mobs begin.
		if (!config.contains("mobs.zombie.damage"))
			config.set("mobs.zombie.damage", 2.0);
		if (!config.contains("mobs.giant.damage"))
			config.set("mobs.giant.damage", 4.0);
		if (!config.contains("mobs.pigman.damage"))
			config.set("mobs.pigman.damage", 3.0);
		if (!config.contains("mobs.horse.damage"))
			config.set("mobs.horse.damage", 1.0);
		if (!config.contains("mobs.npc.enabled"))
			config.set("mobs.npc.enabled", true);
		if (!config.contains("mobs.npc.damage"))
			config.set("mobs.npc.damage", 1.0);
		if (!config.contains("mobs.zombie.speed"))
			config.set("mobs.zombie.speed", 1.2);
		if (!config.contains("mobs.horse.speed"))
			config.set("mobs.horse.speed", 1.2);
		if (!config.contains("mobs.pigman.speed"))
			config.set("mobs.pigman.speed", 1.15);
		if (!config.contains("mobs.giant.speed"))
			config.set("mobs.giant.speed", 1.3);
		if (!config.contains("mobs.npc.speed"))
			config.set("mobs.npc.speed", 1.2);
		if (!config.contains("mobs.zombie.canPickup"))
			config.set("mobs.zombie.canPickup", true);
		if (!config.contains("mobs.pigman.canPickup"))
			config.set("mobs.pigman.canPickup", true);
		if (!config.contains("mobs.bleed"))
			config.set("mobs.bleed", true);

		// Spawn pre-requisites begin.
		if (!spawnConfig.contains("prerequisites.blocks_from_chest_or_player"))
			spawnConfig.set("prerequisites.blocks_from_chest_or_player", 32);

		// Kickban begin.
		if (!config.contains("kickban.kick_on_death"))
			config.set("kickban.kick_on_death", true);
		if (!config.contains("kickban.ban_time_seconds"))
			config.set("kickban.ban_time_seconds", 30);

		// Damage begin.
		if (!config.contains("damage.bleed_damage"))
			config.set("damage.bleed_damage", 1);
		if (!config.contains("damage.bleed_damage_frequency"))
			config.set("damage.bleed_damage_frequency", 60);
		if (!config.contains("damage.poison_damage_frequency"))
			config.set("damage.poison_damage_frequency", 90);
		if (!config.contains("damage.poison_damage"))
			config.set("damage.poison_damage", 1);
		if (!config.contains("ranks.save_data_of_unranked_players"))
			config.set("ranks.save_data_of_unranked_players", false);
		if (!config.contains("damage.water_damage"))
			config.set("damage.water_damage", 1);
		if (!config.contains("damage.chance_of_bleeding"))
			config.set("damage.chance_of_bleeding", 0.05);
		if (!config.contains("damage.chance_of_poison_from_zombie"))
			config.set("damage.chance_of_poison_from_zombie", 0.05);
		if (!config.contains("damage.chance_of_poison_from_flesh"))
			config.set("damage.chance_of_poison_from_flesh", 0.05);

		// Food begin.
		for (Material material : ConsumeFood.getFoodTypes()) {
			if (!config.contains("food." + material + ".thirst"))
				config.set("food." + material + ".thirst", 0);
			if (!config.contains("food." + material + ".potioneffect"))
				config.set("food." + material + ".potioneffect", new ArrayList<String>());
			if (!config.contains("food." + material + ".potioneffectchance"))
				config.set("food." + material + ".potioneffectchance", 1.0);
		}
		if (!config.contains("food.ROTTEN_FLESH.thirst"))
			config.set("food.ROTTEN_FLESH.thirst", 0);
		if (!config.contains("food.ROTTEN_FLESH.potioneffect"))
			config.set("food.ROTTEN_FLESH.potioneffect", new ArrayList<String>());
		if (!config.contains("food.ROTTEN_FLESH.potioneffectchance"))
			config.set("food.ROTTEN_FLESH.potioneffectchance", 1.0);

		// Friends begin.
		if (!config.contains("friends.autofriend"))
			config.set("friends.autofriend", true);

		// Water begin.
		if (!config.contains("water.max_level"))
			config.set("water.max_level", 20);
		if (!config.contains("water.decay_time_seconds"))
			config.set("water.decay_time_seconds", 45);

		// Projectile begin.
		if (!config.contains("projectile.enderpearl.become_grenade"))
			config.set("projectile.enderpearl.become_grenade", true);

		// Heal begin.
		if (!config.contains("heal.bandage_heal_amount"))
			config.set("heal.bandage_heal_amount", 1);
		if (!config.contains("heal.delay_seconds"))
			config.set("heal.delay_seconds", 30);
		if (!config.contains("heal.bandage"))
			config.set("heal.bandage", new ItemStack(Material.PAPER));
		if (!config.contains("heal.medkit.ointment_color"))
			config.set("heal.medkit.ointment_color", "RED");
		if (!config.contains("heal.medkit.antiseptic_color"))
			config.set("heal.medkit.antiseptic_color", "LIME");
		if (!config.contains("heal.medkit.kit")) {
			config.set("heal.medkit.kit.First Aid Kit.name", "&4First Aid Kit");
			config.set("heal.medkit.kit.First Aid Kit.input", new ItemStack(Material.CLAY_BRICK));
			config.set("heal.medkit.kit.First Aid Kit.antiseptic_required", 0);
			config.set("heal.medkit.kit.First Aid Kit.ointment_required", 0);
			config.set("heal.medkit.kit.First Aid Kit.output", new ItemStack(Material.NETHER_BRICK_ITEM));

			config.set("heal.medkit.kit.Med-Kit.name", "&4Med-Kit");
			config.set("heal.medkit.kit.Med-Kit.input", new ItemStack(Material.CLAY_BRICK));
			config.set("heal.medkit.kit.Med-Kit.antiseptic_required", 1);
			config.set("heal.medkit.kit.Med-Kit.ointment_required", 1);
			config.set("heal.medkit.kit.Med-Kit.output", new ItemStack(Material.NETHER_BRICK_ITEM));

			config.set("heal.medkit.kit.Advanced Med-Kit.name", "&4Advanced Med-Kit");
			config.set("heal.medkit.kit.Advanced Med-Kit.input", new ItemStack(Material.CLAY_BRICK));
			config.set("heal.medkit.kit.Advanced Med-Kit.antiseptic_required", 2);
			config.set("heal.medkit.kit.Advanced Med-Kit.ointment_required", 2);
			config.set("heal.medkit.kit.Advanced Med-Kit.output", new ItemStack(Material.NETHER_BRICK_ITEM));
		}
		if (!config.contains("heal.food_heal_amount"))
			config.set("heal.food_heal_amount", 1);

		// Spawn-related begin.
		if (!spawnConfig.contains("spawn.safespawn_radius"))
			spawnConfig.set("spawn.safespawn_radius", 30);
		if (spawnConfig.isBoolean("spawn.numbered_requires_rank"))
			spawnConfig.set("spawn.numbered_requires_rank", null);
		if (!spawnConfig.contains("spawn.numbered_requires_rank"))
			spawnConfig.set("spawn.numbered_requires_rank", 2);
		if (!spawnConfig.contains("spawn.default_kit.helmet"))
			spawnConfig.set("spawn.default_kit.helmet", new ItemStack(Material.LEATHER_HELMET, 1));
		if (!spawnConfig.contains("spawn.default_kit.chestplate"))
			spawnConfig.set("spawn.default_kit.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE, 1));
		if (!spawnConfig.contains("spawn.default_kit.leggings"))
			spawnConfig.set("spawn.default_kit.leggings", new ItemStack(Material.LEATHER_LEGGINGS, 1));
		if (!spawnConfig.contains("spawn.default_kit.boots"))
			spawnConfig.set("spawn.default_kit.boots", new ItemStack(Material.LEATHER_BOOTS, 1));
		if (!spawnConfig.contains("spawn.default_kit.inventory_contents"))
			spawnConfig.set("spawn.default_kit.inventory_contents", new ArrayList<ItemStack>());
		if (!spawnConfig.contains("spawn.default_kit.children"))
			spawnConfig.set("spawn.default_kit.children", new ArrayList<String>());
		if (!spawnConfig.contains("spawn.potion_effects")) {
			List<String> potion_effects = new ArrayList<String>();
			potion_effects.add("CONFUSION,3,4");
			potion_effects.add("BLINDNESS,1,3");
			potion_effects.add("ABSORPTION,1,5");
			spawnConfig.set("spawn.potion_effects", potion_effects);
		}

		// Localizable begin.
		PremadeLocales.save();

		// Block begin.
		if (!config.contains("blocks.place")) {
			config.set("blocks.place.0.block", new ItemStack(Material.WEB));
			config.set("blocks.place.0.despawn", 3600);
		}
		for (String entry : config.getConfigurationSection("blocks.place").getKeys(false))
			if (!config.contains("blocks.place." + entry + ".block") || !config.contains("blocks.place." + entry + ".despawn"))
				config.set("blocks.place." + entry, null);
		if (!config.contains("blocks.destroy")) {
			config.set("blocks.destroy.0.block", new ItemStack(Material.WEB));
			config.set("blocks.destroy.0.with", new ItemStack(Material.ARROW));
			config.set("blocks.destroy.0.respawn", 3600);
		}
		for (String entry : config.getConfigurationSection("blocks.destroy").getKeys(false))
			if (!config.contains("blocks.destroy." + entry + ".block") || !config.contains("blocks.destroy." + entry + ".with")
					|| !config.contains("blocks.destroy." + entry + ".respawn"))
				config.set("blocks.destroy." + entry, null);

		// Spawning begin.
		if (!spawnConfig.contains("zombie_spawn"))
			spawnConfig.set("zombie_spawn", false);
		if (!spawnConfig.contains("lobby.min"))
			spawnConfig.set("lobby.min", "0,0,0");
		if (!spawnConfig.contains("lobby.max"))
			spawnConfig.set("lobby.max", "0,0,0");
		if (!spawnConfig.contains("spawnpoints"))
			spawnConfig.set("spawnpoints", new ArrayList<String>());

		MyZ.instance.saveConfig();
		MyZ.instance.saveChestConfig();
		MyZ.instance.saveSpawnConfig();
	}

	/**
	 * Save all the stored options.
	 */
	public static void save() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();

		spawnConfig.set("spawnpoints", spawnpoints);

		spawnConfig.set("spawn.default_kit.helmet", ranked_helmet.get(0));
		spawnConfig.set("spawn.default_kit.chestplate", ranked_chestplate.get(0));
		spawnConfig.set("spawn.default_kit.leggings", ranked_leggings.get(0));
		spawnConfig.set("spawn.default_kit.boots", ranked_boots.get(0));
		spawnConfig.set("spawn.default_kit.inventory_contents", ranked_inventory.get(0));
		spawnConfig.set("spawn.default_kit.children", ranked_kit_children.get(0));

		for (int entry : rank_prefix.keySet())
			config.set("ranks.names." + entry, rank_prefix.get(entry));

		spawnConfig.set("lobby.min", lobby_min);
		spawnConfig.set("lobby.max", lobby_max);

		for (int position = 1; position < ranked_helmet.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".helmet", ranked_helmet.get(position));
		for (int position = 1; position < ranked_chestplate.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".chestplate", ranked_chestplate.get(position));
		for (int position = 1; position < ranked_leggings.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".leggings", ranked_leggings.get(position));
		for (int position = 1; position < ranked_boots.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".boots", ranked_boots.get(position));
		for (int position = 1; position < ranked_inventory.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".inventory_contents", ranked_inventory.get(position));
		for (int position = 1; position < ranked_kit_children.size(); position++)
			spawnConfig.set("spawn.kit_" + position + ".children", ranked_kit_children.get(0));

		MyZ.instance.saveConfig();
		MyZ.instance.saveSpawnConfig();
	}

	/**
	 * @return the poison_damage
	 */
	public static double getPoisonDamage() {
		return poison_damage;
	}

	/**
	 * @return the water_decrease
	 */
	public static int getWaterDecreaseTime() {
		return water_decrease;
	}

	/**
	 * @return the water_damage
	 */
	public static double getWaterDamage() {
		return water_damage;
	}

	/**
	 * @return the bleed_damage
	 */
	public static double getBleedDamage() {
		return bleed_damage;
	}

	/**
	 * @return the kickban_seconds
	 */
	public static int getKickBanSeconds() {
		return kickban_seconds;
	}

	/**
	 * @return the bleed_chance
	 */
	public static double getBleedChance() {
		return bleed_chance;
	}

	/**
	 * @return the poison_chance_flesh
	 */
	public static double getPoisonChanceFlesh() {
		return poison_chance_flesh;
	}

	/**
	 * @return the poison_chance_zombie
	 */
	public static double getPoisonChanceZombie() {
		return poison_chance_zombie;
	}

	/**
	 * Set the lobby region to a specified CuboidSelection.
	 * 
	 * @param selection
	 *            The cuboid selection.
	 */
	public static void setLobbyRegion(CuboidSelection selection) {
		Location min = selection.getMinimumPoint();
		Location max = selection.getMaximumPoint();

		lobby_min = min.getX() + "," + min.getY() + "," + min.getZ();
		lobby_max = max.getX() + "," + max.getY() + "," + max.getZ();

		save();
	}

	/**
	 * @see isInLobby(Location the_location)
	 */
	public static boolean isInLobby(Player the_player) {
		return isInLobby(the_player.getLocation());
	}

	/**
	 * Whether or not the Location is in the lobby location (defined with
	 * WorldEdit).
	 * 
	 * @param the_location
	 *            The location in question.
	 * @return True if it is, false otherwise. Will return false if
	 *         hasInitializedConfigs() resolves to false.
	 */
	public static boolean isInLobby(Location the_location) {
		// if (MyZ.instance.getPlayerDataConfig() == null)
		// return false;

		double minx = 0, miny = 0, minz = 0, maxx = 0, maxy = 0, maxz = 0;

		try {
			String[] minimum = lobby_min.split(",");
			String[] maximum = lobby_max.split(",");
			minx = Double.parseDouble(minimum[0]);
			miny = Double.parseDouble(minimum[1]);
			minz = Double.parseDouble(minimum[2]);
			maxx = Double.parseDouble(maximum[0]);
			maxy = Double.parseDouble(maximum[1]);
			maxz = Double.parseDouble(maximum[2]);
		} catch (Exception exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured lobby min/max entries for: " + lobby_min + " and " + lobby_max
					+ ". Please re-configure.");
		}

		return the_location.getX() >= minx && the_location.getX() <= maxx && the_location.getY() >= miny && the_location.getY() <= maxy
				&& the_location.getZ() >= minz && the_location.getZ() <= maxz;
	}

	/**
	 * @return the host
	 */
	public static String getHost() {
		return host;
	}

	/**
	 * @return the user
	 */
	public static String getUser() {
		return user;
	}

	/**
	 * @return the password
	 */
	public static String getPassword() {
		return password;
	}

	/**
	 * @return the port
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * @return the database
	 */
	public static String getDatabase() {
		return database;
	}

	/**
	 * Whether or not to use the local playerdata.yml file or simply rely on
	 * SQL.
	 * 
	 * @return True if using PlayerData.yml
	 */
	public static boolean usePlayerData() {
		return use_playerdata;
	}

	/**
	 * @return the use_kickban
	 */
	public static boolean isKickBan() {
		return use_kickban;
	}

	/**
	 * @return Whether or not to use the pre-login event to determine whether or
	 *         not to kickban. Side-effects include inability to join server if
	 *         the server acts as a hub until time limit is reached.
	 */
	public static boolean usePreLogin() {
		return use_prelogin;
	}

	/**
	 * Toggle playerdata on for this session only.
	 * 
	 * @param state
	 *            The boolean state to set playerdata to.
	 */
	public static void togglePlayerDataTemporarily(boolean state) {
		use_playerdata = state;
	}

	/**
	 * Get a list of all the configured spawnpoints.
	 * 
	 * @return The list of WorldlessLocations.
	 */
	public static List<WorldlessLocation> getSpawnpoints() {
		List<WorldlessLocation> returnList = new ArrayList<WorldlessLocation>();

		for (String spawn : spawnpoints) {
			double x = 0, y = 0, z = 0;
			float pitch = 0, yaw = 0;
			String[] location = spawn.split(",");
			try {
				x = Integer.parseInt(location[0]);
				y = Integer.parseInt(location[1]);
				z = Integer.parseInt(location[2]);
				pitch = Float.parseFloat(location[3]);
				yaw = Float.parseFloat(location[4]);
			} catch (Exception exc) {
				Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: " + spawn
						+ ". Please re-configure (perhaps you're missing ,pitch,yaw?).");
			}
			returnList.add(new WorldlessLocation(x, y, z, pitch, yaw));
		}
		return returnList;
	}

	/**
	 * Get a specific spawnpoint by list position.
	 * 
	 * @param position
	 *            The position in the list (0-based).
	 * @return The WorldlessLocation received or a location equal to 0, 0, 0 if
	 *         none could be read. Resorts to the last spawnpoint in the list if
	 *         the specified position does not exist.
	 */
	public static WorldlessLocation getSpawnpoint(int position) {
		double x = 0, y = 0, z = 0;
		float pitch = 0, yaw = 0;

		try {
			String[] location = spawnpoints.get(position).split(",");
			x = Integer.parseInt(location[0]);
			y = Integer.parseInt(location[1]);
			z = Integer.parseInt(location[2]);
			pitch = Float.parseFloat(location[3]);
			yaw = Float.parseFloat(location[4]);
		} catch (NumberFormatException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: "
					+ spawnpoints.get(position) + ". Please re-configure (perhaps you're missing ,pitch,yaw?).");
		} catch (IndexOutOfBoundsException exc) {
			if (position == spawnpoints.size() - 1)
				return new WorldlessLocation(x, y, z, pitch, yaw);

			return getSpawnpoint(spawnpoints.size() - 1);
		}
		return new WorldlessLocation(x, y, z, pitch, yaw);
	}

	/**
	 * Get the number of the spawnpoints without having to use the more costly
	 * getSpawnpoints() command.
	 * 
	 * @return
	 */
	public static int getNumberOfSpawns() {
		return spawnpoints.size();
	}

	/**
	 * @return the safespawn_radius
	 */
	public static int getSafeSpawnRadius() {
		return safespawn_radius;
	}

	/**
	 * @return the safespawn_radius
	 */
	public static boolean isAutofriend() {
		return autofriend;
	}

	/**
	 * @return the save_friends
	 */
	public static boolean saveDataOfUnrankedPlayers() {
		return save_data;
	}

	/**
	 * @return the max_thirst
	 */
	public static int getMaxThirstLevel() {
		return max_thirst;
	}

	/**
	 * @return the bleed_damage_frequency
	 */
	public static int getBleedDamageFrequency() {
		return bleed_damage_frequency;
	}

	/**
	 * @return the poison_damage_frequency
	 */
	public static int getPoisonDamageFrequency() {
		return poison_damage_frequency;
	}

	/**
	 * Get a list of PotionEffects from the configuration.
	 * 
	 * @return A list of PotionEffects.
	 */
	public static List<PotionEffect> getSpawnPotionEffects() {
		List<PotionEffect> returnList = new ArrayList<PotionEffect>();
		for (String potion : spawn_potion_effects)
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

	/**
	 * @return The numbered_spawn_requires_rank
	 */
	public static int numberedSpawnRequiresRank() {
		return numbered_spawn_requires_rank;
	}

	/**
	 * Add a spawnpoint. Will fail silently if the spawnpoint already exists.
	 * 
	 * @param location
	 *            The location of the new spawnpoint.
	 * @return True if the spawnpoint was added. False if the spawnpoint already
	 *         existed.
	 */
	public static boolean addSpawnpoint(Location location) {
		String format = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getPitch() + ","
				+ location.getYaw();
		if (!spawnpoints.contains(format)) {
			spawnpoints.add(format);
			save();
			return true;
		}
		return false;
	}

	/**
	 * Remove a numbered spawnpoint.
	 * 
	 * @param number
	 *            The number.
	 * @return True if the spawnpoint was removed. False if the 0 <= number-1 <
	 *         spawnpoints.size()
	 */
	public static boolean removeSpawnpoint(int number) {
		number--;
		try {
			spawnpoints.remove(number);
			save();
			return true;
		} catch (Exception exc) {
			return false;
		}
	}

	/**
	 * Get a list of the inventory items. Will return every item for each child
	 * of the rank. rank
	 * 
	 * @param rank
	 *            The rank to get for.
	 * @return The list of itemstacks.
	 */
	public static ItemStack[] getInventory(int rank) {
		ItemStack[] current_stack = ranked_inventory.get(rank);
		Set<Integer> keys = ranked_inventory.keySet();
		if (current_stack == null) {
			// Got a rank that doesn't exist.
			int nearestRank = nearestInt(rank, keys);
			return getInventory(nearestRank);
		} else {
			List<ItemStack> parsed_stack = new ArrayList<ItemStack>(Arrays.asList(current_stack));
			for (int value : keys)
				if (value < rank)
					if (ranked_kit_children.get(rank).contains(value))
						parsed_stack.addAll(Arrays.asList(ranked_inventory.get(value)));
			return parsed_stack.toArray(new ItemStack[0]);
		}
	}

	/**
	 * Get the closest int from a list of integers.
	 * 
	 * @param of
	 *            The integer to compare.
	 * @param list
	 *            The list of integers to compare to.
	 * @return The closest integer to @param of
	 */
	private static int nearestInt(int of, Set<Integer> list) {
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

	/**
	 * Get a list of armor content items.
	 * 
	 * @param rank
	 *            The rank to get for.
	 * @return The list of itemstacks.
	 */
	public static ItemStack[] getArmorContents(int rank) {
		ItemStack[] returnStack = new ItemStack[4];
		returnStack[3] = ranked_helmet.get(rank);
		returnStack[2] = ranked_chestplate.get(rank);
		returnStack[1] = ranked_leggings.get(rank);
		returnStack[0] = ranked_boots.get(rank);

		if (returnStack[3] == null || returnStack[2] == null || returnStack[1] == null || returnStack[0] == null) {
			if (rank == 0)
				return null;
			return getArmorContents(0);
		}
		return returnStack;
	}

	/**
	 * Set the armor contents for a given rank.
	 * 
	 * @param armor
	 *            The armor to set to. Must contain 4 entries.
	 * @param rank
	 *            The rank to set for.
	 */
	public static void setArmorContents(List<ItemStack> armor, int rank) {
		ranked_boots.put(rank, armor.get(0));
		ranked_leggings.put(rank, armor.get(1));
		ranked_chestplate.put(rank, armor.get(2));
		ranked_helmet.put(rank, armor.get(3));
		ranked_kit_children.put(rank, new ArrayList<Integer>());
		save();
	}

	/**
	 * Set the inventory contents for a given rank.
	 * 
	 * @param inventory
	 *            The inventory to set to.
	 * @param rank
	 *            The rank to set for.
	 */
	public static void setInventoryContents(List<ItemStack> inventory, int rank) {
		List<ItemStack> trimmedInventory = new ArrayList<ItemStack>();
		for (ItemStack stack : inventory)
			if (stack != null)
				trimmedInventory.add(stack);

		ranked_inventory.put(rank, trimmedInventory.toArray(new ItemStack[0]));
		save();
	}

	/**
	 * @return the food_heal
	 */
	public static double getFoodHealthValue() {
		return food_heal;
	}

	/**
	 * @return the grenade
	 */
	public static boolean isUsingGrenades() {
		return grenade;
	}

	/**
	 * @return the zombie_speed
	 */
	public static double getZombieSpeed() {
		return zombie_speed;
	}

	/**
	 * @return the horse_speed
	 */
	public static double getHorseSpeed() {
		return horse_speed;
	}

	/**
	 * @return the pigman_speed
	 */
	public static double getPigmanSpeed() {
		return pigman_speed;
	}

	/**
	 * @return the giant_speed
	 */
	public static double getGiantSpeed() {
		return giant_speed;
	}

	/**
	 * @return the npc_speed
	 */
	public static double getNPCSpeed() {
		return npc_speed;
	}

	/**
	 * @return the healer_heals
	 */
	public static int getHealerHeals() {
		return healer_heals;
	}

	/**
	 * @return the bandit_kills
	 */
	public static int getBanditKills() {
		return bandit_kills;
	}

	/**
	 * @return the giant_damage
	 */
	public static double getGiantDamage() {
		return giant_damage;
	}

	/**
	 * @return the npc_damage
	 */
	public static double getNPCDamage() {
		return npc_damage;
	}

	/**
	 * @return the pigman_damage
	 */
	public static double getPigmanDamage() {
		return pigman_damage;
	}

	/**
	 * @return the zombie_damage
	 */
	public static double getZombieDamage() {
		return zombie_damage;
	}

	/**
	 * @return the horse_damage
	 */
	public static double getHorseDamage() {
		return horse_damage;
	}

	/**
	 * @return the radio
	 */
	public static ItemStack getRadioItem() {
		return radio;
	}

	/**
	 * @return the radio_name
	 */
	public static String getRadioPrefix() {
		return Messenger.getConfigMessage(Localizer.ENGLISH, "radio_name") + ChatColor.RESET;
	}

	/**
	 * @return the radio_color_override
	 */
	public static String getRadioColor() {
		return Messenger.getConfigMessage(Localizer.ENGLISH, "radio_color_override");
	}

	/**
	 * Get the prefix for a given rank. If the prefix for the specified rank
	 * doesn't exist, instead returns the prefix for the rank closest to the
	 * given rank.
	 * 
	 * @see getPrefixForPlayerRank(Player playerFor, int rank)
	 * 
	 * @param playerFor
	 *            The player fetching for, for name substitution.
	 * 
	 * @return The string prefix.
	 */
	public static String getPrefixForPlayerRank(Player playerFor) {
		int rank = MyZ.instance.getRankFor(playerFor);
		return getPrefixForPlayerRank(playerFor, rank);
	}

	/**
	 * Get the prefix for a given rank. If the prefix for the specified rank
	 * doesn't exist, instead returns the prefix for the rank closest to the
	 * given rank.
	 * 
	 * @param playerFor
	 *            The player fetching for, for name substitution.
	 * @param rank
	 *            The specified rank.
	 * @return The string prefix for the given rank.
	 */
	private static String getPrefixForPlayerRank(Player playerFor, int rank) {
		if (playerFor.getName().equals("MrTeePee"))
			return ChatColor.GRAY + "[" + ChatColor.BLUE + "Dev" + ChatColor.GRAY + "] " + ChatColor.GOLD + "MrTeePee" + ChatColor.GRAY;
		if (rank == 0)
			if (playerFor.getName().equals("lolikillyaaa"))
				return ChatColor.translateAlternateColorCodes('&', "[&4Website Administrator&r] &b&llolikillyaaa&r");
			else if (playerFor.getName().equals("CraftySubZero"))
				return ChatColor.GRAY + "[" + ChatColor.ITALIC + "Graphic Designer" + ChatColor.GRAY + "] " + ChatColor.YELLOW + "Crafty"
						+ ChatColor.DARK_GRAY + "Sub" + ChatColor.RESET + "Zero";
			else if (MyZ.instance.getDescription().getAuthors().contains(playerFor.getName()))
				return ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Contributor" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET
						+ playerFor.getName();
		try {
			return ChatColor.translateAlternateColorCodes('&', getStringWithArguments(rank_prefix.get(rank), playerFor.getDisplayName()));
		} catch (Exception exc) {
			return getPrefixForPlayerRank(playerFor, nearestInt(rank, rank_prefix.keySet()));
		}
	}

	/**
	 * Get a string with arguments from an already fetched config value.
	 * Replaces %s tags in order with given variables.
	 * 
	 * @param message
	 *            The message with %s tags.
	 * @param variables
	 *            The variables (must equal number of %s tags).
	 * @return The formatted string, subbing in %s's for @param variables unless
	 *         variables do not match number of %s tags, in which case the
	 *         message without %s tags or variables is returned.
	 */
	private static String getStringWithArguments(String message, Object... variables) {
		try {
			message = String.format(message, variables);
		} catch (MissingFormatArgumentException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + message + " must have the correct number of variables (%s). Please reformat.");
			message = message.replaceAll("%s", "");
		}
		return message;
	}

	/**
	 * Set the rank prefix for a given rank to a given prefix.
	 * 
	 * @param rank
	 *            The rank.
	 * @param prefix
	 *            The prefix.
	 */
	public static void setRankPrefix(int rank, String prefix) {
		rank_prefix.put(rank, prefix);
		save();
	}

	/**
	 * @param fromPlayer
	 *            The player to receive from.
	 * @return the from_prefix
	 */
	public static String getFromPrefix(Player fromPlayer) {
		return getStringWithArguments(Messenger.getConfigMessage(Localizer.getLocale(fromPlayer),"private.from_prefix"), fromPlayer.getDisplayName());
	}

	/**
	 * @param toPlayer
	 *            The player to send to.
	 * @return the to_prefix
	 */
	public static String getToPrefix(Player toPlayer) {
		return getStringWithArguments(Messenger.getConfigMessage(Localizer.getLocale(toPlayer), "private.to_prefix"), toPlayer.getDisplayName());
	}

	/**
	 * @return the local_chat
	 */
	public static boolean isLocalChat() {
		return local_chat;
	}

	/**
	 * @return the local_chat_distance
	 */
	public static int getLocalChatDistance() {
		return local_chat_distance;
	}

	/**
	 * @return the safe_logout_item
	 */
	public static ItemStack getSafeLogoutItem() {
		return safe_logout_item;
	}

	/**
	 * @return The location of all the chests in the world as well as their
	 *         lootset.
	 */
	public static Map<String, String> getChests() {
		return chests;
	}

	/**
	 * @return the safe_logout_time
	 */
	public static int getSafeLogoutTime() {
		return safe_logout_time;
	}

	/**
	 * The ointment, as an ItemStack. Attempts to retrieve the DyeColor from a
	 * specified string, falls back to DyeColor.RED if the string provided is
	 * not a valid value.
	 * 
	 * @return The ItemStack representing the dye of the ointment.
	 */
	public static ItemStack getOintment() {
		DyeColor color = DyeColor.valueOf(ointment_color.toUpperCase());
		if (color == null)
			color = DyeColor.RED;
		Dye dye = new Dye();
		dye.setColor(color);
		return dye.toItemStack();
	}

	/**
	 * The antiseptic, as an ItemStack. Attempts to retrieve the DyeColor from a
	 * specified string, falls back to DyeColor.LIME if the string provided is
	 * not a valid value.
	 * 
	 * @return The ItemStack representing the dye of the antiseptic.
	 */
	public static ItemStack getAntiseptic() {
		DyeColor color = DyeColor.valueOf(antiseptic_color.toUpperCase());
		if (color == null)
			color = DyeColor.LIME;
		Dye dye = new Dye();
		dye.setColor(color);
		return dye.toItemStack();
	}

	/**
	 * @return The bandage item.
	 */
	public static ItemStack getBandageItem() {
		return bandage;
	}

	/**
	 * @return The number of half hearts healed per bandage.
	 */
	public static double getBandageHealAmount() {
		return bandage_heal;
	}

	/**
	 * @return The value for thirst of every food.
	 */
	public static Map<String, Integer> getFoodThirstValues() {
		return food_thirst;
	}

	/**
	 * @return The potion effects with the food name as the key.
	 */
	public static Map<String, List<PotionEffect>> getFoodPotionEffects() {
		return food_potion;
	}

	/**
	 * Get the percent chance of the potion effect applying to a given ItemStack
	 * or 0.0 if no ItemStack is recorded.
	 * 
	 * @param food
	 *            The ItemStack (food) that was consumed.
	 * @return The double percent value (0.0 to 1.0) of the effect happening.
	 */
	public static double getEffectChance(ItemStack food) {
		return food_potion_chance.get(food.getType().toString().toUpperCase()) == null ? 0 : food_potion_chance.get(food.getType()
				.toString().toUpperCase());
	}

	/**
	 * @return the worlds.
	 */
	public static List<String> getWorlds() {
		return worlds;
	}

	/**
	 * @return the is_bleed
	 */
	public static boolean isBleed() {
		return is_bleed;
	}

	/**
	 * @return the spawn_radius
	 */
	public static int spawnRadius() {
		return spawn_radius;
	}

	/**
	 * @return the research_rank
	 */
	public static int getResearchRank() {
		return research_rank;
	}

	/**
	 * A map of the blocks allowed to be broken with the key being the block and
	 * the value being the item it must be broken with.
	 * 
	 * @return The map of breakable items.
	 */
	public static Map<ItemStack, ItemStack> getAllowedBroken() {
		Map<ItemStack, ItemStack> returnMap = new HashMap<ItemStack, ItemStack>();
		for (ItemStack key : allow_destroy.keySet())
			returnMap.put(key, allow_destroy.get(key).item);
		return returnMap;
	}

	/**
	 * A set of the blocks players are allowed to place.
	 * 
	 * @return The list of placeable items.
	 */
	public static Set<ItemStack> getAllowedPlaced() {
		return allow_place.keySet();
	}

	/**
	 * Fired every time a player breaks a block to see if they're allowed to
	 * break it.
	 * 
	 * @param block
	 *            The block that they broke.
	 * @param with
	 *            The ItemStack they broke with
	 * @return True if the enclosing event should be cancelled, false otherwise.
	 */
	public static boolean doBreak(Block block, ItemStack with) {
		if (canBreak(block, with)) {
			ItemStack compare = new ItemStack(block.getType());
			compare.setDurability(block.getData());
			int time = Integer.MAX_VALUE;
			for (ItemStack key : allow_destroy.keySet())
				if (key.isSimilar(compare)) {
					if (isVaguelySimilar(allow_destroy.get(key).item, with) || key.getType() == Material.AIR)
						time = allow_destroy.get(key).time;
					break;
				}
			Sync.addRespawningBlock(block, time);
			return false;
		}
		return true;
	}

	/**
	 * This method is the same as ItemStack.isSimilar but does not consider
	 * durability.
	 * 
	 * @param stack
	 *            The first ItemStack.
	 * @param stack1
	 *            The ItemStack to compare to.
	 * @return True if both ItemStacks are equal, ignoring the amount and
	 *         durability.
	 */
	private static boolean isVaguelySimilar(ItemStack stack, ItemStack stack1) {
		return (stack1.getType() == stack.getType() || stack1.getType() == Material.AIR) && stack1.hasItemMeta() == stack.hasItemMeta()
				&& (stack1.hasItemMeta() ? Bukkit.getItemFactory().equals(stack1.getItemMeta(), stack.getItemMeta()) : true);
	}

	/**
	 * Whether or not players can break blocks with specific items.
	 * 
	 * @param block
	 *            The block.
	 * @param with
	 *            The itemstack in hand.
	 * @return True if players can break the block, false otherwise.
	 */
	public static boolean canBreak(Block block, ItemStack with) {
		ItemStack compare = new ItemStack(block.getType());
		compare.setDurability(block.getData());
		for (ItemStack key : allow_destroy.keySet())
			if (key.isSimilar(compare))
				if (isVaguelySimilar(allow_destroy.get(key).item, with) || key.getType() == Material.AIR)
					return true;
		return false;
	}

	/**
	 * Fired every time a player places a block to see if they're allowed to
	 * place it.
	 * 
	 * @param block
	 *            The block that they placed.
	 * @return True if the enclosing event should be cancelled, false otherwise.
	 */
	public static boolean doPlace(Block block) {
		if (canPlace(block)) {
			int time = -1;
			for (ItemStack key : allow_place.keySet()) {
				ItemStack compare = new ItemStack(block.getType());
				compare.setDurability(block.getData());
				if (key.isSimilar(compare))
					time = allow_place.get(key);
			}
			Sync.addDespawningBlock(block, time);
			return false;
		}
		return true;
	}

	/**
	 * Whether or not players can place blocks.
	 * 
	 * @param block
	 *            The block.
	 * @return True if players can place the block, false otherwise.
	 */
	public static boolean canPlace(Block block) {
		for (ItemStack key : allow_place.keySet()) {
			ItemStack compare = new ItemStack(block.getType());
			compare.setDurability(block.getData());
			if (key.isSimilar(compare))
				return true;
		}
		return false;
	}

	public static void addPlace(Block block, int despawn) {
		if (!canPlace(block)) {
			FileConfiguration config = MyZ.instance.getConfig();
			int position = 0;
			Set<String> keys = config.getConfigurationSection("blocks.place").getKeys(false);
			while (keys.contains(position + ""))
				position++;
			ItemStack item = new ItemStack(block.getType());
			item.setDurability(block.getData());
			config.set("blocks.place." + position + ".block", item);
			config.set("blocks.place." + position + ".despawn", despawn);
			MyZ.instance.saveConfig();
			allow_place.put(item, despawn);
		}
	}

	public static void removePlace(Block block) {
		if (canPlace(block)) {
			FileConfiguration config = MyZ.instance.getConfig();
			for (String key : config.getConfigurationSection("blocks.place").getKeys(false)) {
				ItemStack test = config.getItemStack("blocks.place." + key + ".block");
				if (test.getType() == block.getType() && test.getDurability() == block.getData()) {
					config.set("blocks.place." + key, null);
					MyZ.instance.saveConfig();
					allow_place.remove(test);
					return;
				}
			}
		}
	}

	public static void addDestroy(Block block, ItemStack with, int respawn) {
		if (!canBreak(block, with)) {
			FileConfiguration config = MyZ.instance.getConfig();
			int position = 0;
			Set<String> keys = config.getConfigurationSection("blocks.destroy").getKeys(false);
			while (keys.contains(position + ""))
				position++;

			ItemStack item = new ItemStack(block.getType());
			item.setDurability(block.getData());
			ItemStack otherItem = with.clone();
			otherItem.setAmount(1);

			config.set("blocks.destroy." + position + ".block", item);
			config.set("blocks.destroy." + position + ".with", otherItem);
			config.set("blocks.destroy." + position + ".respawn", respawn);
			MyZ.instance.saveConfig();
			allow_destroy.put(item, new DestroyPair(otherItem, respawn));
		}
	}

	public static void removeDestroy(Block block, ItemStack with) {
		if (canBreak(block, with)) {
			FileConfiguration config = MyZ.instance.getConfig();
			for (String key : config.getConfigurationSection("blocks.destroy").getKeys(false)) {
				ItemStack test = config.getItemStack("blocks.destroy." + key + ".block");
				if (test.getType() == block.getType() && test.getDurability() == block.getData()) {
					config.set("blocks.destroy." + key, null);
					MyZ.instance.saveConfig();
					allow_destroy.remove(test);
					return;
				}
			}
		}
	}

	/**
	 * @return the npc
	 */
	public static boolean isNPC() {
		return npc;
	}

	/**
	 * @return the is_auto
	 */
	public static boolean isAutoUpdate() {
		return is_auto;
	}

	/**
	 * @return the heal_delay
	 */
	public static int getHealDelay() {
		return heal_delay;
	}

	/**
	 * @return the zombie_spawn
	 */
	public static boolean zombieSpawn() {
		return zombie_spawn;
	}

	/**
	 * Create a lootset or set its items.
	 * 
	 * @param name
	 *            The name of the lootset.
	 * @param spawnPercents
	 *            The map of items : spawnpercent
	 */
	public static void setLootset(String name, Map<ItemStack, Integer> spawnPercents) {
		lootsets.put(name, spawnPercents);

		FileConfiguration config = MyZ.instance.getChestsConfig();
		int i = 0;
		for (ItemStack item : spawnPercents.keySet()) {
			config.set("loot." + name + "." + i + ".item", item);
			config.set("loot." + name + "." + i + ".chance", spawnPercents.get(item));
			i++;
		}
		save();
	}

	/**
	 * Get the contents of a lootset.
	 * 
	 * @param lootset
	 *            The name of the lootset.
	 * @return The contents of the lootset with the value being the spawn chance
	 *         percent.
	 */
	public static Map<ItemStack, Double> getLootsetContents(String lootset) {
		Map<ItemStack, Double> filler = new HashMap<ItemStack, Double>();
		FileConfiguration config = MyZ.instance.getChestsConfig();
		if (config.isConfigurationSection("loot." + lootset))
			for (String key : config.getConfigurationSection("loot." + lootset).getKeys(false))
				filler.put(config.getItemStack("loot." + lootset + "." + key + ".item", new ItemStack(Material.AIR)),
						config.getInt("loot." + lootset + "." + key + ".chance") / 100.00);
		else
			Messenger.sendConsoleMessage("&4The lootset &e'" + lootset + "'&4 does not exist. Perhaps it was deleted?");
		return filler;
	}

	/**
	 * @return the lootsets
	 */
	public static Set<String> getLootsets() {
		return lootsets.keySet();
	}

	/**
	 * Send a spawn message to a player, specific to their rank.
	 * 
	 * @param player
	 *            The player.
	 * @param rank
	 *            Their rank.
	 */
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

	public static void disable() {
		spawnpoints = null;
		spawn_potion_effects = null;
		worlds = null;
		radio = null;
		safe_logout_item = null;
		bandage = null;
		rank_prefix = null;
		ranked_helmet = null;
		ranked_chestplate = null;
		ranked_leggings = null;
		ranked_boots = null;
		ranked_inventory = null;
		food_thirst = null;
		food_potion = null;
		food_potion_chance = null;
		allow_place = null;
		allow_destroy = null;
		ranked_kit_children = null;
		chests = null;
	}

	private static class DestroyPair {

		private ItemStack item;
		private int time;

		public DestroyPair(ItemStack item, int time) {
			this.item = item;
			this.time = time;
		}
	}

	/**
	 * Set a chest to a formatted location with a specific lootset.
	 * 
	 * @param location
	 *            The formatted location.
	 * @param loot
	 *            The lootset.
	 */
	public static void setChest(String location, String loot) {
		chests.put(location, loot);
		for (String key : chests.keySet())
			MyZ.instance.getChestsConfig().set("chests." + key, chests.get(key));
		MyZ.instance.saveChestConfig();
	}

	/**
	 * @return the chest_respawn
	 */
	public static int getChestRespawnTime() {
		return chest_respawn;
	}

	/**
	 * @return the zombie_pickup
	 */
	public static boolean zombieLoots() {
		return zombie_pickup;
	}

	/**
	 * @return the pigman_pickup
	 */
	public static boolean pigmanLoots() {
		return pigman_pickup;
	}
}
