/**
 * 
 */
package myz.Support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import myz.MyZ;
import myz.Listeners.ConsumeFood;
import myz.Scheduling.Sync;
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

	private static boolean use_playerdata, use_kickban, playerdata_is_temporary, use_prelogin, autofriend, save_data,
			numbered_spawn_requires_rank, grenade, local_chat, minez_chests, is_bleed, is_auto;
	private static String host = "", user = "", password = "", database = "", lobby_min = "0,0,0", lobby_max = "0,0,0", radio_name = "",
			radio_color_override = "", to_prefix = "", from_prefix = "", ointment_color = "", antiseptic_color = "";
	private static int water_decrease, kickban_seconds, port, safespawn_radius, max_thirst, poison_damage_frequency,
			bleed_damage_frequency, healer_heals, bandit_kills, local_chat_distance, safe_logout_time;
	private static double bleed_chance, poison_chance_flesh, poison_chance_zombie, food_heal, poison_damage, water_damage, bleed_damage,
			zombie_speed, horse_speed, giant_speed, pigman_speed, zombie_damage, horse_damage, giant_damage, pigman_damage, bandage_heal;
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

	private static Map<ItemStack, Integer> allow_place = new HashMap<ItemStack, Integer>();
	private static Map<ItemStack, DestroyPair> allow_destroy = new HashMap<ItemStack, DestroyPair>();

	// TODO ensure all new values are added in reload(), writeUnwrittenValues()
	// and save()

	/**
	 * Setup this configuration object.
	 */
	public static void reload() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration localizableConfig = MyZ.instance.getLocalizableConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();
		writeUnwrittenValues();

		playerdata_is_temporary = false;

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

		is_auto = config.getBoolean("autoupdate.enable");
		worlds = new ArrayList<String>(config.getStringList("multiworld.worlds"));
		minez_chests = config.getBoolean("download.minez_chests");
		bandage = config.getItemStack("heal.bandage");
		bandage_heal = config.getDouble("heal.bandage_heal_amount");
		local_chat = config.getBoolean("chat.local_enabled");
		local_chat_distance = config.getInt("chat.local_distance");
		radio_color_override = localizableConfig.getString("localizable.radio_color_override");
		to_prefix = localizableConfig.getString("localizable.private.to_prefix");
		from_prefix = localizableConfig.getString("localizable.private.from_prefix");
		radio_name = localizableConfig.getString("localizable.radio_name");
		radio = config.getItemStack("radio.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));
		safe_logout_time = config.getInt("safe_logout.time");
		safe_logout_item = config.getItemStack("safe_logout.itemstack", new ItemStack(Material.EYE_OF_ENDER, 1));
		zombie_damage = config.getDouble("mobs.zombie.damage");
		pigman_damage = config.getDouble("mobs.pigman.damage");
		giant_damage = config.getDouble("mobs.giant.damage");
		horse_damage = config.getDouble("mobs.horse.damage");
		bandit_kills = config.getInt("statistics.bandit_kills");
		healer_heals = config.getInt("statistics.healer_heals");
		zombie_speed = config.getDouble("mobs.zombie.speed");
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
		lobby_min = config.getString("lobby.min");
		lobby_max = config.getString("lobby.max");
		host = config.getString("mysql.host");
		database = config.getString("mysql.database");
		user = config.getString("mysql.user");
		password = config.getString("mysql.password");
		port = config.getInt("mysql.port");
		save_data = config.getBoolean("ranks.save_data_of_unranked_players");
		spawnpoints = new ArrayList<String>(config.getStringList("spawnpoints"));
		autofriend = config.getBoolean("friends.autofriend");
		safespawn_radius = config.getInt("spawn.safespawn_radius");
		max_thirst = config.getInt("water.max_level");
		spawn_potion_effects = new ArrayList<String>(config.getStringList("spawn.potion_effects"));
		numbered_spawn_requires_rank = config.getBoolean("spawn.numbered_requires_rank");

		is_bleed = config.getBoolean("mobs.bleed");
		grenade = config.getBoolean("projectile.enderpearl.become_grenade");

		ointment_color = config.getString("heal.medkit.ointment_color");
		antiseptic_color = config.getString("heal.medkit.antiseptic_color");
		food_heal = config.getDouble("heal.food_heal_amount");

		ranked_helmet.put(0, spawnConfig.getItemStack("spawn.default_kit.helmet", new ItemStack(Material.LEATHER_HELMET)));
		ranked_chestplate.put(0, spawnConfig.getItemStack("spawn.default_kit.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE)));
		ranked_leggings.put(0, spawnConfig.getItemStack("spawn.default_kit.leggings", new ItemStack(Material.LEATHER_LEGGINGS)));
		ranked_boots.put(0, spawnConfig.getItemStack("spawn.default_kit.boots", new ItemStack(Material.LEATHER_BOOTS)));
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
	}

	/**
	 * Write in all missing values.
	 * 
	 * @param config
	 *            The FileConfiguration to write into.
	 */
	private static void writeUnwrittenValues() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration localizableConfig = MyZ.instance.getLocalizableConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();

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

		// Chat begin.
		if (!config.contains("chat.local_enabled"))
			config.set("chat.local_enabled", true);
		if (!config.contains("chat.local_distance"))
			config.set("chat.local_distance", 250);

		// Ranks begin.
		if (!config.contains("ranks.names.0"))
			config.set("ranks.names.0", "[%s]");

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

		// Download begin.
		if (!config.contains("download.minez_chests"))
			config.set("download.minez_chests", true);

		// Mobs begin.
		if (!config.contains("mobs.zombie.damage"))
			config.set("mobs.zombie.damage", 2.0);
		if (!config.contains("mobs.giant.damage"))
			config.set("mobs.giant.damage", 4.0);
		if (!config.contains("mobs.pigman.damage"))
			config.set("mobs.pigman.damage", 3.0);
		if (!config.contains("mobs.horse.damage"))
			config.set("mobs.horse.damage", 1.0);
		if (!config.contains("mobs.zombie.speed"))
			config.set("mobs.zombie.speed", 1.2);
		if (!config.contains("mobs.horse.speed"))
			config.set("mobs.horse.speed", 1.2);
		if (!config.contains("mobs.pigman.speed"))
			config.set("mobs.pigman.speed", 1.15);
		if (!config.contains("mobs.giant.speed"))
			config.set("mobs.giant.speed", 1.3);
		if (!config.contains("mobs.bleed"))
			config.set("mobs.bleed", true);

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
		if (!config.contains("heal.bandage"))
			config.set("heal.bandage", new ItemStack(Material.PAPER));
		if (!config.contains("heal.bandage_heal_amount"))
			config.set("heal.bandage_heal_amount", 1);
		if (!config.contains("heal.medkit.localizable.regeneration"))
			config.set("heal.medkit.localizable.regeneration", "Regeneration");
		if (!config.contains("heal.medkit.localizable.heal"))
			config.set("heal.medkit.localizable.heal", "Heal");
		if (!config.contains("heal.medkit.localizable.antiseptic"))
			config.set("heal.medkit.localizable.antiseptic", "Antiseptic");
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
		if (!spawnConfig.contains("spawn.numbered_requires_rank"))
			spawnConfig.set("spawn.numbered_requires_rank", true);
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
		if (!spawnConfig.contains("spawn.potion_effects")) {
			List<String> potion_effects = new ArrayList<String>();
			potion_effects.add("CONFUSION,3,4");
			potion_effects.add("BLINDNESS,1,3");
			potion_effects.add("ABSORPTION,1,5");
			spawnConfig.set("spawn.potion_effects", potion_effects);
		}

		// Localizable begin.
		if (!localizableConfig.contains("localizable.science_gui"))
			localizableConfig.set("localizable.science_gui", "Research Centre - %s points");
		if (!localizableConfig.contains("localizable.research.fail"))
			localizableConfig.set("localizable.research.fail", "The Science Gods refuse your offering.");
		if (!localizableConfig.contains("localizable.research.success"))
			localizableConfig.set("localizable.research.success", "You gain a better understanding of the disease and %s research points.");
		if (!localizableConfig.contains("localizable.radio_name"))
			localizableConfig.set("localizable.radio_name", "[&8Radio - &7%s.0&8 Hz&f]");
		if (!localizableConfig.contains("localizable.radio_color_override"))
			localizableConfig.set("localizable.radio_color_override", "&2");
		if (!localizableConfig.contains("localizable.private.to_prefix"))
			localizableConfig.set("localizable.private.to_prefix", "&7To %s:");
		if (!localizableConfig.contains("localizable.private.from_prefix"))
			localizableConfig.set("localizable.private.from_prefix", "&7From %s:");
		if (!localizableConfig.contains("localizable.private.clan_prefix"))
			localizableConfig.set("localizable.private.clan_prefix", "&8Clan chat:");
		if (!localizableConfig.contains("localizable.damage.bleed_begin"))
			localizableConfig.set("localizable.damage.bleed_begin", "&4Ouch! I think I'm bleeding.");
		if (!localizableConfig.contains("localizable.damage.headshot"))
			localizableConfig.set("localizable.damage.headshot", "&eHeadshot! 2x damage.");
		if (!localizableConfig.contains("localizable.damage.poison_begin"))
			localizableConfig.set("localizable.damage.poison_begin", "&5Wh&ko&r&da, &5&kI &1d&kon&r&3't &kF&r&afeel &4so &kg&r&6oo&cd...");
		if (!localizableConfig.contains("localizable.damage.bleed_end"))
			localizableConfig.set("localizable.damage.bleed_end", "That ought to stop the bleeding.");
		if (!localizableConfig.contains("localizable.damage.poison_end"))
			localizableConfig.set("localizable.damage.poison_end", "Ah, much better!");
		if (!localizableConfig.contains("localizable.kick.come_back"))
			localizableConfig.set("localizable.kick.come_back", "&4Grab a drink. Come back in %s seconds.");
		if (!localizableConfig.contains("localizable.kick.safe_logout"))
			localizableConfig.set("localizable.kick.safe_logout", "&eYou have been safely logged out.");
		if (!localizableConfig.contains("localizable.kick.recur"))
			localizableConfig.set("localizable.kick.recur", "&4Stop stressing. %s seconds to go.");
		if (!localizableConfig.contains("localizable.command.spawn.unable_to_spawn"))
			localizableConfig.set("localizable.command.spawn.unable_to_spawn", "&4Unable to spawn there. Please try again shortly.");
		if (!localizableConfig.contains("localizable.command.allowed.breakable"))
			localizableConfig.set("localizable.command.allowed.breakable", "&eYou can break:");
		if (!localizableConfig.contains("localizable.command.base.help"))
			localizableConfig.set("localizable.command.base.help", "=== MyZ Help ===");
		if (!localizableConfig.contains("localizable.command.stats.header"))
			localizableConfig.set("localizable.command.stats.header", "==== Statistics for &e%s&r ====");
		if (!localizableConfig.contains("localizable.command.stats.kills_header"))
			localizableConfig.set("localizable.command.stats.kills_header", "==== &eKILLS&r ====");
		if (!localizableConfig.contains("localizable.command.stats.kills"))
			localizableConfig.set("localizable.command.stats.kills", "Zombie: &e%s&r  Pigman: &e%s&r  Giant: &e%s&r  Player: &e%s");
		if (!localizableConfig.contains("localizable.command.stats.time_header"))
			localizableConfig.set("localizable.command.stats.time_header", "==== &eTIME SURVIVED&r ====");
		if (!localizableConfig.contains("localizable.command.stats.time"))
			localizableConfig.set("localizable.command.stats.time", "Total: &e%s minutes&r  This life: &e%s minutes");
		if (!localizableConfig.contains("localizable.command.stats.footer"))
			localizableConfig.set("localizable.command.stats.footer", "See complete stats at http://my-z.org/scores.php?user=%s");
		if (!localizableConfig.contains("localizable.command.allowed.placeable"))
			localizableConfig.set("localizable.command.allowed.placeable", "&eYou can place:");
		if (!localizableConfig.contains("localizable.command.block.arguments"))
			localizableConfig.set("localizable.command.block.arguments", "&4Usage: /blockallow <place/destroy>");
		if (!localizableConfig.contains("localizable.command.block.place.arguments"))
			localizableConfig.set("localizable.command.block.place.arguments",
					"&4Usage: /blockallow place <add [seconds until despawn]/remove>");
		if (!localizableConfig.contains("localizable.command.research.arguments"))
			localizableConfig.set("localizable.command.research.arguments",
					"&4Usage: /setresearch <addreward [point cost]/add [point value]/remove>");
		if (!localizableConfig.contains("localizable.command.research.reward.added"))
			localizableConfig.set("localizable.command.research.reward.added", "&ePlayers can now research %s with %s research points.");
		if (!localizableConfig.contains("localizable.command.research.added"))
			localizableConfig.set("localizable.command.research.added", "&ePlayers can now do research with %s for %s research points.");
		if (!localizableConfig.contains("localizable.command.research.removed"))
			localizableConfig.set("localizable.command.research.removed", "&ePlayers can no longer research %s.");
		if (!localizableConfig.contains("localizable.command.research.item"))
			localizableConfig.set("localizable.command.research.item",
					"&eYou must be holding the item you wish to add/remove from research.");
		if (!localizableConfig.contains("localizable.command.research.item_exists"))
			localizableConfig.set("localizable.command.research.item_exists", "&4That item is already researchable.");
		if (!localizableConfig.contains("localizable.command.research.item_no_exists"))
			localizableConfig.set("localizable.command.research.item_no_exists", "&4That item isn't researchable.");
		if (!localizableConfig.contains("localizable.command.block.destroy.arguments"))
			localizableConfig.set("localizable.command.block.destroy.arguments",
					"&4Usage: /blockallow destroy <add [seconds until respawn]/remove>");
		if (!localizableConfig.contains("localizable.command.block.destroy.add.help"))
			localizableConfig.set("localizable.command.block.destroy.add.help",
					"&eNow left-click the block you want to whitelist with the item you want to allow breaking with.");
		if (!localizableConfig.contains("localizable.command.block.destroy.remove.help"))
			localizableConfig.set("localizable.command.block.destroy.remove.help",
					"&eNow left-click the block you want blacklist with the item that you can currently break with.");
		if (!localizableConfig.contains("localizable.command.block.place.add.help"))
			localizableConfig.set("localizable.command.block.place.add.help", "&eNow place the block you would like to whitelist.");
		if (!localizableConfig.contains("localizable.command.block.place.remove.help"))
			localizableConfig.set("localizable.command.block.place.remove.help", "&eNow place the block you would like to blacklist.");
		if (!localizableConfig.contains("localizable.command.block.destroy.add.summary"))
			localizableConfig.set("localizable.command.block.destroy.add.summary", "&ePlayers can now destroy %s blocks with %ss.");
		if (!localizableConfig.contains("localizable.command.block.destroy.remove.summary"))
			localizableConfig
					.set("localizable.command.block.destroy.remove.summary", "&ePlayers can no longer destroy %s blocks with %ss.");
		if (!localizableConfig.contains("localizable.command.block.place.add.summary"))
			localizableConfig.set("localizable.command.block.place.add.summary", "&ePlayers can now place %s blocks.");
		if (!localizableConfig.contains("localizable.command.block.place.remove.summary"))
			localizableConfig.set("localizable.command.block.place.remove.summary", "&ePlayers can no longer place %s blocks.");
		if (!localizableConfig.contains("localizable.command.block.destroy.add.fail"))
			localizableConfig.set("localizable.command.block.destroy.add.fail", "&ePlayers can already break %s blocks with %ss.");
		if (!localizableConfig.contains("localizable.command.block.destroy.remove.fail"))
			localizableConfig.set("localizable.command.block.destroy.remove.fail", "&ePlayers cannot destroy %s blocks with %ss.");
		if (!localizableConfig.contains("localizable.command.block.place.add.fail"))
			localizableConfig.set("localizable.command.block.place.add.fail", "&ePlayers can already place %s blocks.");
		if (!localizableConfig.contains("localizable.command.block.place.remove.fail"))
			localizableConfig.set("localizable.command.block.place.remove.fail", "&ePlayers cannot place %s blocks.");
		if (!localizableConfig.contains("localizable.command.setlobby.requires_cuboid"))
			localizableConfig.set("localizable.command.setlobby.requires_cuboid",
					"&4You must make a &ocuboid&r&4 selection with WorldEdit.");
		if (!localizableConfig.contains("localizable.command.setlobby.updated"))
			localizableConfig.set("localizable.command.setlobby.updated", "&2The lobby region has been updated.");
		if (!localizableConfig.contains("localizable.command.spawn.too_far_from_lobby"))
			localizableConfig.set("localizable.command.spawn.too_far_from_lobby", "&4You are too far from the lobby.");
		if (!localizableConfig.contains("localizable.command.setrank.success"))
			localizableConfig.set("localizable.command.setrank.success", "&eYou have successfully updated the player's rank.");
		if (!localizableConfig.contains("localizable.command.setrank.failure"))
			localizableConfig.set("localizable.command.setrank.failure",
					"&4You must specify the name of a player that has played before and a rank value greater or equal to 0.");
		if (!localizableConfig.contains("localizable.private.no_player"))
			localizableConfig.set("localizable.private.no_player", "&4The player could not be found.");
		if (!localizableConfig.contains("localizable.safe_logout.cancelled"))
			localizableConfig.set("localizable.safe_logout.cancelled", "&4Safe logout cancelled due to movement.");
		if (!localizableConfig.contains("localizable.heal.amount"))
			localizableConfig.set("localizable.heal.amount", "&ePlayer &2healed&e. You now have %s heals this life.");
		if (!localizableConfig.contains("localizable.bandit.amount"))
			localizableConfig.set("localizable.bandit.amount", "&ePlayer &4killed&e. You now have %s kills this life.");
		if (!localizableConfig.contains("localizable.zombie.kill_amount"))
			localizableConfig.set("localizable.zombie.kill_amount", "&eZombie down. %s this life.");
		if (!localizableConfig.contains("localizable.pigman.kill_amount"))
			localizableConfig.set("localizable.pigman.kill_amount", "&ePigman down. %s this life.");
		if (!localizableConfig.contains("localizable.giant.kill_amount"))
			localizableConfig.set("localizable.giant.kill_amount", "&eGiant down. %s this life.");
		if (!localizableConfig.contains("localizable.safe_logout.beginning"))
			localizableConfig.set("localizable.safe_logout.beginning", "&2Safe logout will occur in:");
		if (!localizableConfig.contains("localizable.private.many_players"))
			localizableConfig.set("localizable.private.many_players", "&4More than one player was found.");
		if (!localizableConfig.contains("localizable.command.spawn.requires_rank"))
			localizableConfig.set("localizable.command.spawn.requires_rank",
					"&4This is a donator-only feature. Donate today for the ability to spawn near your friends!");
		if (!localizableConfig.contains("localizable.command.addspawn.added"))
			localizableConfig.set("localizable.command.addspawn.added", "&eYour location has been added to the spawnpoints.");
		if (!localizableConfig.contains("localizable.command.removespawn.removed"))
			localizableConfig.set("localizable.command.removespawn.removed", "&eThe spawnpoint has been removed.");
		if (!localizableConfig.contains("localizable.command.removespawn.unable_to_remove"))
			localizableConfig.set("localizable.command.removespawn.unable_to_remove", "&4The number you specified is out of range.");
		if (!localizableConfig.contains("localizable.command.removespawn.requires_number"))
			localizableConfig.set("localizable.command.removespawn.requires_number",
					"&4You must specify a spawnpoint number to remove. See numbers using /spawnpoints.");
		if (!localizableConfig.contains("localizable.command.addspawn.already_exists"))
			localizableConfig.set("localizable.command.addspawn.already_exists", "&4This location is already a spawnpoint.");
		if (!localizableConfig.contains("localizable.special.giant_summoned"))
			localizableConfig.set("localizable.special.giant_summoned", "&eYou hear the ground shake. A giant is about be summoned.");
		if (!localizableConfig.contains("localizable.special.giant_could_not_summon"))
			localizableConfig.set("localizable.special.giant_could_not_summon", "&eThere is not enough space here to summon a giant.");
		if (!localizableConfig.contains("localizable.special.giant_summon_permission"))
			localizableConfig.set("localizable.special.giant_summon_permission",
					"&4This is a donator-only feature. Donate today for the ability to spawn the fabled boss mobs.");
		if (!localizableConfig.contains("localizable.player_npc_killed"))
			localizableConfig.set("localizable.player_npc_killed", "&e%s has been killed while combat logging.");
		if (!localizableConfig.contains("localizable.clan.name.too_long"))
			localizableConfig.set("localizable.clan.name.too_long", "&4Clan names must be less than 20 characters.");
		if (!localizableConfig.contains("localizable.clan.joined"))
			localizableConfig.set("localizable.clan.joined", "You have joined '&e%s&r'.");
		if (!localizableConfig.contains("localizable.clan.joining"))
			localizableConfig.set("localizable.clan.joining", "Joining clan. Please wait...");
		if (!localizableConfig.contains("localizable.command.clan.leave"))
			localizableConfig.set("localizable.command.clan.leave", "You are no longer in a clan.");
		if (!localizableConfig.contains("localizable.command.clan.not_in"))
			localizableConfig.set("localizable.command.clan.not_in", "You are not in a clan.");
		if (!localizableConfig.contains("localizable.command.clan.in"))
			localizableConfig.set("localizable.command.clan.in", "You are in '&e%s&r' (%s online / %s).");
		if (!localizableConfig.contains("localizable.player_was_killed_npc"))
			localizableConfig.set("localizable.player_was_killed_npc", "&eYou were killed while combat logging.");
		if (!localizableConfig.contains("localizable.command.friend.requires_name"))
			localizableConfig.set("localizable.command.friend.requires_name", "&4You must specify a name to friend.");
		if (!localizableConfig.contains("localizable.command.savekit.requires_number"))
			localizableConfig.set("localizable.command.savekit.requires_number", "&4You must specify a rank number to save for.");
		if (!localizableConfig.contains("localizable.command.savekit.saved"))
			localizableConfig.set("localizable.command.savekit.saved",
					"&eThe starting kit for rank %s has been saved as your current inventory contents.");
		if (!localizableConfig.contains("localizable.command.saverank.requires_number"))
			localizableConfig.set("localizable.command.saverank.requires_number", "&4You must specify a rank number to save for.");
		if (!localizableConfig.contains("localizable.command.saverank.requires_prefix"))
			localizableConfig.set("localizable.command.saverank.requires_prefix", "&4You must specify a prefix to set.");
		if (!localizableConfig.contains("localizable.command.saverank.saved"))
			localizableConfig.set("localizable.command.saverank.saved", "&eThe chat prefix for rank number %s has been set to %s.");
		if (!localizableConfig.contains("localizable.command.friend.non_exist"))
			localizableConfig.set("localizable.command.friend.non_exist", "&4%s has never played before.");
		if (!localizableConfig.contains("localizable.friend.added"))
			localizableConfig.set("localizable.friend.added", "&e%s &9has been added to your friends list.");
		if (!localizableConfig.contains("localizable.friend.removed"))
			localizableConfig.set("localizable.friend.removed", "&e%s &9has been removed from your friends list.");

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
		if (!config.contains("lobby.min"))
			config.set("lobby.min", "0,0,0");
		if (!config.contains("lobby.max"))
			config.set("lobby.max", "0,0,0");
		if (!config.contains("spawnpoints"))
			config.set("spawnpoints", new ArrayList<String>());

		MyZ.instance.saveConfig();
		MyZ.instance.saveLocalizableConfig();
		MyZ.instance.saveSpawnConfig();
	}

	/**
	 * Save all the stored options.
	 */
	public static void save() {
		FileConfiguration config = MyZ.instance.getConfig();
		FileConfiguration spawnConfig = MyZ.instance.getSpawnConfig();

		if (!playerdata_is_temporary)
			config.set("datastorage.use_server_specific", use_playerdata);

		spawnConfig.set("spawnpoints", spawnpoints);

		spawnConfig.set("spawn.default_kit.helmet", ranked_helmet.get(0));
		spawnConfig.set("spawn.default_kit.chestplate", ranked_chestplate.get(0));
		spawnConfig.set("spawn.default_kit.leggings", ranked_leggings.get(0));
		spawnConfig.set("spawn.default_kit.boots", ranked_boots.get(0));
		spawnConfig.set("spawn.default_kit.inventory_contents", ranked_inventory.get(0));

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

		MyZ.instance.saveConfig();
		MyZ.instance.saveLocalizableConfig();
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

		Configuration.lobby_min = min.getX() + "," + min.getY() + "," + min.getZ();
		Configuration.lobby_max = max.getX() + "," + max.getY() + "," + max.getZ();

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
		if (MyZ.instance.getPlayerDataConfig() == null)
			return false;

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
		playerdata_is_temporary = true;
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
			String[] location = spawn.split(",");
			try {
				x = Integer.parseInt(location[0]);
				y = Integer.parseInt(location[1]);
				z = Integer.parseInt(location[2]);
			} catch (Exception exc) {
				Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: " + spawn
						+ ". Please re-configure.");
			}
			returnList.add(new WorldlessLocation(x, y, z));
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

		try {
			String[] location = spawnpoints.get(position).split(",");
			x = Integer.parseInt(location[0]);
			y = Integer.parseInt(location[1]);
			z = Integer.parseInt(location[2]);
		} catch (NumberFormatException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: "
					+ spawnpoints.get(position) + ". Please re-configure.");
		} catch (IndexOutOfBoundsException exc) {
			if (position == spawnpoints.size() - 1)
				return new WorldlessLocation(x, y, z);

			return getSpawnpoint(spawnpoints.size() - 1);
		}
		return new WorldlessLocation(x, y, z);
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
	public static boolean numberedSpawnRequiresRank() {
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
		String format = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
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
	 * Get a list of the inventory items. Will return every item for rank <= @param
	 * rank
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
	 * @return the zombie speed
	 */
	public static double getZombieSpeed() {
		return zombie_speed;
	}

	/**
	 * @return the horse speed
	 */
	public static double getHorseSpeed() {
		return horse_speed;
	}

	/**
	 * @return the pigman speed
	 */
	public static double getPigmanSpeed() {
		return pigman_speed;
	}

	/**
	 * @return the giant speed
	 */
	public static double getGiantSpeed() {
		return giant_speed;
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
		return ChatColor.translateAlternateColorCodes('&', radio_name) + ChatColor.RESET;
	}

	/**
	 * @return the radio_color_override
	 */
	public static String getRadioColor() {
		return radio_color_override;
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
		else if (playerFor.getName().equals("lolikillyaaa"))
			return ChatColor.translateAlternateColorCodes('&', "[&4Website Administrator&r] &b&llolikillyaaa&r");
		else if (playerFor.getName().equals("CraftySubZero"))
			return ChatColor.GRAY + "[" + ChatColor.ITALIC + "Graphic Designer" + ChatColor.GRAY + "] " + ChatColor.YELLOW + "Crafty"
					+ ChatColor.DARK_GRAY + "Sub" + ChatColor.RESET + "Zero";
		else if (MyZ.instance.getDescription().getAuthors().contains(playerFor.getName()))
			return ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Contributor" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET
					+ playerFor.getName();
		try {
			return ChatColor.translateAlternateColorCodes('&', getStringWithArguments(rank_prefix.get(rank), playerFor.getDisplayName()))
					+ ChatColor.RESET;
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
		return getStringWithArguments(ChatColor.translateAlternateColorCodes('&', from_prefix), fromPlayer.getDisplayName());
	}

	/**
	 * @param toPlayer
	 *            The player to send to.
	 * @return the to_prefix
	 */
	public static String getToPrefix(Player toPlayer) {
		return getStringWithArguments(ChatColor.translateAlternateColorCodes('&', to_prefix), toPlayer.getDisplayName());
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
	 * @return the minez_chests.
	 */
	public static boolean isDownloadMineZChests() {
		return minez_chests;
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
			int time = -1;
			for (ItemStack key : allow_destroy.keySet())
				if (key.isSimilar(compare))
					time = allow_destroy.get(key).time;
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
		return stack1.getType() == stack.getType() && stack1.hasItemMeta() == stack.hasItemMeta()
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
		for (ItemStack key : allow_destroy.keySet()) {
			ItemStack compare = new ItemStack(block.getType());
			compare.setDurability(block.getData());
			if (key.isSimilar(compare))
				if (isVaguelySimilar(allow_destroy.get(key).item, with))
					return true;
		}
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
	 * @return the is_auto
	 */
	public static boolean isAutoUpdate() {
		return is_auto;
	}

	private static class DestroyPair {

		private ItemStack item;
		private int time;

		public DestroyPair(ItemStack item, int time) {
			this.item = item;
			this.time = time;
		}
	}
}
