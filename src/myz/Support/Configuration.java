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
import myz.Utilities.WorldlessLocation;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
			numbered_spawn_requires_rank, grenade, local_chat, minez_chests;
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

	// TODO ensure all new values are added in reload(), writeUnwrittenValues()
	// and save()

	/**
	 * Setup this configuration object.
	 */
	public static void reload() {
		FileConfiguration config = MyZ.instance.getConfig();
		writeUnwrittenValues(config);

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

		worlds = new ArrayList<String>(config.getStringList("multiworld.worlds"));
		minez_chests = config.getBoolean("download.minez_chests");
		bandage = config.getItemStack("heal.bandage");
		bandage_heal = config.getDouble("heal.bandage_heal_amount");
		local_chat = config.getBoolean("chat.local_enabled");
		local_chat_distance = config.getInt("chat.local_distance");
		radio_color_override = config.getString("localizable.radio_color_override");
		to_prefix = config.getString("localizable.private.to_prefix");
		from_prefix = config.getString("localizable.private.from_prefix");
		radio_name = config.getString("localizable.radio_name");
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

		grenade = config.getBoolean("projectile.enderpearl.become_grenade");

		ointment_color = config.getString("heal.medkit.ointment_color");
		antiseptic_color = config.getString("heal.medkit.antiseptic_color");
		food_heal = config.getDouble("heal.food_heal_amount");

		ranked_helmet.put(0, config.getItemStack("spawn.default_kit.helmet", new ItemStack(Material.LEATHER_HELMET)));
		ranked_chestplate.put(0, config.getItemStack("spawn.default_kit.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE)));
		ranked_leggings.put(0, config.getItemStack("spawn.default_kit.leggings", new ItemStack(Material.LEATHER_LEGGINGS)));
		ranked_boots.put(0, config.getItemStack("spawn.default_kit.boots", new ItemStack(Material.LEATHER_BOOTS)));
		try {
			ranked_inventory.put(0, config.getList("spawn.default_kit.inventory_contents").toArray(new ItemStack[0]));
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

		for (String entry : config.getConfigurationSection("spawn").getKeys(false))
			if (entry.startsWith("kit_"))
				try {
					int position = Integer.parseInt(entry.replace("kit_", ""));
					ranked_helmet.put(position,
							config.getItemStack("spawn.kit_" + position + ".helmet", new ItemStack(Material.LEATHER_HELMET)));
					ranked_chestplate.put(position,
							config.getItemStack("spawn.kit_" + position + ".chestplate", new ItemStack(Material.LEATHER_CHESTPLATE)));
					ranked_leggings.put(position,
							config.getItemStack("spawn.kit_" + position + ".leggings", new ItemStack(Material.LEATHER_LEGGINGS)));
					ranked_boots.put(position,
							config.getItemStack("spawn.kit_" + position + ".boots", new ItemStack(Material.LEATHER_BOOTS)));
					ranked_inventory.put(position, config.getList("spawn.kit_" + position + ".inventory_contents")
							.toArray(new ItemStack[0]));
				} catch (Exception exc) {
					Messenger.sendConsoleMessage("&4spawn.kit_" + entry + " could not be resolved. Please re-configure or remove.");
				}
	}

	/**
	 * Write in all missing values.
	 * 
	 * @param config
	 *            The FileConfiguration to write into.
	 */
	private static void writeUnwrittenValues(FileConfiguration config) {
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
		if (!config.contains("spawn.safespawn_radius"))
			config.set("spawn.safespawn_radius", 30);
		if (!config.contains("spawn.numbered_requires_rank"))
			config.set("spawn.numbered_requires_rank", true);
		if (!config.contains("spawn.default_kit.helmet"))
			config.set("spawn.default_kit.helmet", new ItemStack(Material.LEATHER_HELMET, 1));
		if (!config.contains("spawn.default_kit.chestplate"))
			config.set("spawn.default_kit.chestplate", new ItemStack(Material.LEATHER_CHESTPLATE, 1));
		if (!config.contains("spawn.default_kit.leggings"))
			config.set("spawn.default_kit.leggings", new ItemStack(Material.LEATHER_LEGGINGS, 1));
		if (!config.contains("spawn.default_kit.boots"))
			config.set("spawn.default_kit.boots", new ItemStack(Material.LEATHER_BOOTS, 1));
		if (!config.contains("spawn.default_kit.inventory_contents"))
			config.set("spawn.default_kit.inventory_contents", new ArrayList<ItemStack>());
		if (!config.contains("spawn.potion_effects")) {
			List<String> potion_effects = new ArrayList<String>();
			potion_effects.add("CONFUSION,3,4");
			potion_effects.add("BLINDNESS,1,3");
			potion_effects.add("ABSORPTION,1,5");
			config.set("spawn.potion_effects", potion_effects);
		}

		// Localizable begin.
		if (!config.contains("localizable.radio_name"))
			config.set("localizable.radio_name", "[&8Radio - &7%s.0&8 Hz&f]");
		if (!config.contains("localizable.radio_color_override"))
			config.set("localizable.radio_color_override", "&2");
		if (!config.contains("localizable.private.to_prefix"))
			config.set("localizable.private.to_prefix", "&7To %s:");
		if (!config.contains("localizable.private.from_prefix"))
			config.set("localizable.private.from_prefix", "&7From %s:");
		if (!config.contains("localizable.private.clan_prefix"))
			config.set("localizable.private.clan_prefix", "&8Clan chat:");
		if (!config.contains("localizable.damage.bleed_begin"))
			config.set("localizable.damage.bleed_begin", "&4Ouch! I think I'm bleeding.");
		if (!config.contains("localizable.damage.headshot"))
			config.set("localizable.damage.headshot", "&eHeadshot! 2x damage.");
		if (!config.contains("localizable.damage.poison_begin"))
			config.set("localizable.damage.poison_begin", "&5Wh&ko&r&da, &5&kI &1d&kon&r&3't &kF&r&afeel &4so &kg&r&6oo&cd...");
		if (!config.contains("localizable.damage.bleed_end"))
			config.set("localizable.damage.bleed_end", "That ought to stop the bleeding.");
		if (!config.contains("localizable.damage.poison_end"))
			config.set("localizable.damage.poison_end", "Ah, much better!");
		if (!config.contains("localizable.kick.come_back"))
			config.set("localizable.kick.come_back", "&4Grab a drink. Come back in %s seconds.");
		if (!config.contains("localizable.kick.safe_logout"))
			config.set("localizable.kick.safe_logout", "&eYou have been safely logged out.");
		if (!config.contains("localizable.kick.recur"))
			config.set("localizable.kick.recur", "&4Stop stressing. %s seconds to go.");
		if (!config.contains("localizable.command.spawn.unable_to_spawn"))
			config.set("localizable.command.spawn.unable_to_spawn", "&4Unable to spawn there. Please try again shortly.");
		if (!config.contains("localizable.command.setlobby.requires_cuboid"))
			config.set("localizable.command.setlobby.requires_cuboid", "&4You must make a &ocuboid&r&4 selection with WorldEdit.");
		if (!config.contains("localizable.command.setlobby.updated"))
			config.set("localizable.command.setlobby.updated", "&2The lobby region has been updated.");
		if (!config.contains("localizable.command.spawn.too_far_from_lobby"))
			config.set("localizable.command.spawn.too_far_from_lobby", "&4You are too far from the lobby.");
		if (!config.contains("localizable.command.setrank.success"))
			config.set("localizable.command.setrank.success", "&eYou have successfully updated the player's rank.");
		if (!config.contains("localizable.command.setrank.failure"))
			config.set("localizable.command.setrank.failure",
					"&4You must specify the name of a player that has played before and a rank value greater or equal to 0.");
		if (!config.contains("localizable.private.no_player"))
			config.set("localizable.private.no_player", "&4The player could not be found.");
		if (!config.contains("localizable.safe_logout.cancelled"))
			config.set("localizable.safe_logout.cancelled", "&4Safe logout cancelled due to movement.");
		if (!config.contains("localizable.heal.amount"))
			config.set("localizable.heal.amount", "&ePlayer &2healed&e. You now have %s heals this life.");
		if (!config.contains("localizable.bandit.amount"))
			config.set("localizable.bandit.amount", "&ePlayer &4killed&e. You now have %s kills this life.");
		if (!config.contains("localizable.zombie.kill_amount"))
			config.set("localizable.zombie.kill_amount", "&eZombie down. %s this life.");
		if (!config.contains("localizable.pigman.kill_amount"))
			config.set("localizable.pigman.kill_amount", "&ePigman down. %s this life.");
		if (!config.contains("localizable.giant.kill_amount"))
			config.set("localizable.giant.kill_amount", "&eGiant down. %s this life.");
		if (!config.contains("localizable.safe_logout.beginning"))
			config.set("localizable.safe_logout.beginning", "&2Safe logout will occur in:");
		if (!config.contains("localizable.private.many_players"))
			config.set("localizable.private.many_players", "&4More than one player was found.");
		if (!config.contains("localizable.command.spawn.requires_rank"))
			config.set("localizable.command.spawn.requires_rank",
					"&4This is a donator-only feature. Donate today for the ability to spawn near your friends!");
		if (!config.contains("localizable.command.addspawn.added"))
			config.set("localizable.command.addspawn.added", "&eYour location has been added to the spawnpoints.");
		if (!config.contains("localizable.command.removespawn.removed"))
			config.set("localizable.command.removespawn.removed", "&eThe spawnpoint has been removed.");
		if (!config.contains("localizable.command.removespawn.unable_to_remove"))
			config.set("localizable.command.removespawn.unable_to_remove", "&4The number you specified is out of range.");
		if (!config.contains("localizable.command.removespawn.requires_number"))
			config.set("localizable.command.removespawn.requires_number",
					"&4You must specify a spawnpoint number to remove. See numbers using /spawnpoints.");
		if (!config.contains("localizable.command.addspawn.already_exists"))
			config.set("localizable.command.addspawn.already_exists", "&4This location is already a spawnpoint.");
		if (!config.contains("localizable.special.giant_summoned"))
			config.set("localizable.special.giant_summoned", "&eYou hear the ground shake. A giant is about be summoned.");
		if (!config.contains("localizable.special.giant_could_not_summon"))
			config.set("localizable.special.giant_could_not_summon", "&eThere is not enough space here to summon a giant.");
		if (!config.contains("localizable.special.giant_summon_permission"))
			config.set("localizable.special.giant_summon_permission",
					"&4This is a donator-only feature. Donate today for the ability to spawn the fabled boss mobs.");
		if (!config.contains("localizable.player_npc_killed"))
			config.set("localizable.player_npc_killed", "&e%s has been killed while combat logging.");
		if (!config.contains("localizable.clan.name.too_long"))
			config.set("localizable.clan.name.too_long", "&4Clan names must be less than 20 characters.");
		if (!config.contains("localizable.clan.joined"))
			config.set("localizable.clan.joined", "You have joined '&e%s&r'.");
		if (!config.contains("localizable.clan.joining"))
			config.set("localizable.clan.joining", "Joining clan. Please wait...");
		if (!config.contains("localizable.command.clan.leave"))
			config.set("localizable.command.clan.leave", "You are no longer in a clan.");
		if (!config.contains("localizable.command.clan.not_in"))
			config.set("localizable.command.clan.not_in", "You are not in a clan.");
		if (!config.contains("localizable.command.clan.in"))
			config.set("localizable.command.clan.in", "You are in '&e%s&r' (%s online / %s).");
		if (!config.contains("localizable.player_was_killed_npc"))
			config.set("localizable.player_was_killed_npc", "&eYou were killed while combat logging.");
		if (!config.contains("localizable.command.friend.requires_name"))
			config.set("localizable.command.friend.requires_name", "&4You must specify a name to friend.");
		if (!config.contains("localizable.command.savekit.requires_number"))
			config.set("localizable.command.savekit.requires_number", "&4You must specify a rank number to save for.");
		if (!config.contains("localizable.command.savekit.saved"))
			config.set("localizable.command.savekit.saved",
					"&eThe starting kit for rank %s has been saved as your current inventory contents.");
		if (!config.contains("localizable.command.saverank.requires_number"))
			config.set("localizable.command.saverank.requires_number", "&4You must specify a rank number to save for.");
		if (!config.contains("localizable.command.saverank.requires_prefix"))
			config.set("localizable.command.saverank.requires_prefix", "&4You must specify a prefix to set.");
		if (!config.contains("localizable.command.saverank.saved"))
			config.set("localizable.command.saverank.saved", "&eThe chat prefix for rank number %s has been set to %s.");
		if (!config.contains("localizable.command.friend.non_exist"))
			config.set("localizable.command.friend.non_exist", "&4%s has never played before.");
		if (!config.contains("localizable.friend.added"))
			config.set("localizable.friend.added", "&e%s &9has been added to your friends list.");
		if (!config.contains("localizable.friend.removed"))
			config.set("localizable.friend.removed", "&e%s &9has been removed from your friends list.");

		// Spawning begin.
		if (!config.contains("lobby.min"))
			config.set("lobby.min", "0,0,0");
		if (!config.contains("lobby.max"))
			config.set("lobby.max", "0,0,0");
		if (!config.contains("spawnpoints"))
			config.set("spawnpoints", new ArrayList<String>());

		MyZ.instance.saveConfig();
	}

	/**
	 * Save all the stored options.
	 */
	public static void save() {
		FileConfiguration config = MyZ.instance.getConfig();

		if (!playerdata_is_temporary)
			config.set("datastorage.use_server_specific", use_playerdata);
		/*
		config.set("chat.local_enabled", local_chat);
		config.set("chat.local_distance", local_chat_distance);
		config.set("mobs.zombie.damage", zombie_damage);
		config.set("mobs.giant.damage", giant_damage);
		config.set("mobs.pigman.damage", pigman_damage);
		config.set("mobs.horse.damage", horse_damage);
		config.set("statistics.bandit_kills", bandit_kills);
		config.set("statistics.healer_heals", healer_heals);
		config.set("mobs.zombie.speed", zombie_speed);
		config.set("mobs.horse.speed", horse_speed);
		config.set("performance.use_prelogin_kickban", use_prelogin);
		config.set("kickban.kick_on_death", use_kickban);
		config.set("damage.bleed_damage", bleed_damage);
		config.set("damage.poison_damage", poison_damage);
		config.set("damage.water_damage", water_damage);
		config.set("damage.poison_damage_frequency", poison_damage_frequency);
		config.set("damage.bleed_damage_frequency", bleed_damage_frequency);
		config.set("water.decay_time_seconds", water_decrease);
		config.set("kickban.ban_time_seconds", kickban_seconds);
		config.set("damage.chance_of_bleeding", bleed_chance);
		config.set("damage.chance_of_poison_from_zombie", poison_chance_zombie);
		config.set("damage.chance_of_poison_from_flesh", poison_chance_flesh);
		/*
		config.set("lobby.min", lobby_min);
		config.set("lobby.max", lobby_max);
		/*
		config.set("mysql.user", user);
		config.set("mysql.password", password);
		config.set("mysql.host", host);
		config.set("mysql.database", database);
		config.set("mysql.port", port);
		*/
		config.set("spawnpoints", spawnpoints);
		/*
		config.set("spawn.safespawn_radius", safespawn_radius);
		config.set("friends.autofriend", autofriend);
		config.set("ranks.save_data_of_unranked_players", save_data);
		config.set("water.max_level", max_thirst);
		config.set("spawn.potion_effects", spawn_potion_effects);
		config.set("spawn.numbered_requires_rank", numbered_spawn_requires_rank);

		config.set("radio.itemstack", radio);
		config.set("safe_logout.time", safe_logout_time);
		config.set("safe_logout.itemstack", safe_logout_item);
		*/
		config.set("spawn.default_kit.helmet", ranked_helmet.get(0));
		config.set("spawn.default_kit.chestplate", ranked_chestplate.get(0));
		config.set("spawn.default_kit.leggings", ranked_leggings.get(0));
		config.set("spawn.default_kit.boots", ranked_boots.get(0));
		config.set("spawn.default_kit.inventory_contents", ranked_inventory.get(0));
		/*
		config.set("heal.bandage", bandage);
		config.set("heal.bandage_heal_amount", bandage_heal); 
		config.set("heal.medkit.ointment_color", ointment_color);
		config.set("heal.medkit.antiseptic_color", antiseptic_color);

		config.set("heal.food_heal_amount", food_heal);
		config.set("projectile.enderpearl.become_grenade", grenade);
		*/

		/*
		int pos = 0;
		for (String prefix : rank_prefix.values()) {
			config.set("ranks.names." + pos, prefix);
			pos++;
		}

				for (MedKit kit : MedKit.getKits()) {
					kit.save();
				}
				*/

		for (int position = 1; position < ranked_helmet.size(); position++)
			config.set("spawn.kit_" + position + ".helmet", ranked_helmet.get(position));
		for (int position = 1; position < ranked_chestplate.size(); position++)
			config.set("spawn.kit_" + position + ".chestplate", ranked_chestplate.get(position));
		for (int position = 1; position < ranked_leggings.size(); position++)
			config.set("spawn.kit_" + position + ".leggings", ranked_leggings.get(position));
		for (int position = 1; position < ranked_boots.size(); position++)
			config.set("spawn.kit_" + position + ".boots", ranked_boots.get(position));
		for (int position = 1; position < ranked_inventory.size(); position++)
			config.set("spawn.kit_" + position + ".inventory_contents", ranked_inventory.get(position));

		MyZ.instance.saveConfig();
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
		if (!MyZ.instance.hasInitializedConfig())
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
}
