/**
 * 
 */
package myz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import myz.api.PlayerBeginBleedingEvent;
import myz.api.PlayerBeginPoisonEvent;
import myz.api.PlayerBreakLegEvent;
import myz.api.PlayerSpawnInWorldEvent;
import myz.api.PlayerWaterDecayEvent;
import myz.chests.ChestManager;
import myz.chests.ChestScanner;
import myz.commands.AddResearchCommand;
import myz.commands.AddSpawnCommand;
import myz.commands.AllowedCommand;
import myz.commands.BlockCommand;
import myz.commands.ChestGetCommand;
import myz.commands.ChestScanCommand;
import myz.commands.ChestSetCommand;
import myz.commands.ClanCommand;
import myz.commands.CreateMedKitCommand;
import myz.commands.FriendCommand;
import myz.commands.FriendsCommand;
import myz.commands.GetUIDCommand;
import myz.commands.ItemConfigurationCommand;
import myz.commands.JoinClanCommand;
import myz.commands.LootSetCommand;
import myz.commands.RemoveSpawnCommand;
import myz.commands.ResearchCommand;
import myz.commands.SaveKitCommand;
import myz.commands.SaveRankCommand;
import myz.commands.SetLobbyCommand;
import myz.commands.SetRankCommand;
import myz.commands.SpawnCommand;
import myz.commands.SpawnsCommand;
import myz.commands.StatsCommand;
import myz.commands.TranslateCommand;
import myz.listeners.CancelZombieDamage;
import myz.listeners.EntityHurtPlayer;
import myz.listeners.EntitySpawn;
import myz.listeners.player.AutoFriend;
import myz.listeners.player.BlockEvent;
import myz.listeners.player.CancelPlayerEvents;
import myz.listeners.player.Chat;
import myz.listeners.player.ConsumeFood;
import myz.listeners.player.Heal;
import myz.listeners.player.JoinQuit;
import myz.listeners.player.KittehTag;
import myz.listeners.player.LibsUndisguiseListener;
import myz.listeners.player.Movement;
import myz.listeners.player.PlayerDeath;
import myz.listeners.player.PlayerHurtEntity;
import myz.listeners.player.PlayerKillEntity;
import myz.listeners.player.PlayerSummonGiant;
import myz.listeners.player.PlayerTakeDamage;
import myz.listeners.player.ResearchItem;
import myz.listeners.player.UndisguiseListener;
import myz.listeners.player.Visibility;
import myz.nmscode.compat.Compat;
import myz.nmscode.compat.MessageUtils;
import myz.nmscode.compat.MobUtils;
import myz.scheduling.Sync;
import myz.scheduling.aSync;
import myz.support.MedKit;
import myz.support.PlayerData;
import myz.support.SQLManager;
import myz.support.Teleport;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;
import myz.utilities.DisguiseUtils;
import myz.utilities.LibsDisguiseUtils;
import myz.utilities.NMSUtils;
import myz.utilities.Utils;
import myz.utilities.Validate;
import myz.utilities.VaultUtils;
import myz.utilities.WorldlessLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mcstats.MetricsLite;

/**
 * @author Jordan
 * 
 *         The main plugin and the interface for most convenience methods.
 */
public class MyZ extends JavaPlugin {

	// TODO configurable death loot (?)

	// TODO use construction parts to create clans. Builder is clan owner.
	// Requires Build-in-a-box.

	// TODO grave-digging
	// TODO @ chat not showing name (?)

	// TODO Research point rank uppance @see ResearchItem#checkRankIncrease

	// TODO clan create permission in joinclan.

	// TODO update to 1.7.5

	public static MyZ instance;
	public static boolean vault;
	private List<UUID> online_players = new ArrayList<UUID>();
	private FileConfiguration blocks, spawn, chests, research;
	private Map<String, FileConfiguration> localizable = new HashMap<String, FileConfiguration>();
	private SQLManager sql;
	private static final Random random = new Random();
	public static boolean alertOps;
	private Map<UUID, FileConfiguration> playerdata = new HashMap<UUID, FileConfiguration>();
	private List<UUID> flags = new ArrayList<UUID>();

	public static Compat version;

	/**
	 * Load the blocks YAML file.
	 */
	private void loadBlocks() {
		File blocks_file = new File(getDataFolder() + File.separator + "blocks.yml");

		/*
		 * Make sure the file exists.
		 */
		if (!blocks_file.exists())
			try {
				blocks_file.createNewFile();
			} catch (IOException e) {
				getLogger().warning("Unable to save blocks.yml: " + e.getMessage());
			}
		blocks = YamlConfiguration.loadConfiguration(blocks_file);
	}

	/**
	 * Load the chests YAML file.
	 */
	private void loadChests() {
		File chests_file = new File(getDataFolder() + File.separator + "chests.yml");

		/*
		 * Make sure the file exists.
		 */
		if (!chests_file.exists())
			try {
				chests_file.createNewFile();
			} catch (IOException e) {
				getLogger().warning("Unable to save chests.yml: " + e.getMessage());
			}
		chests = YamlConfiguration.loadConfiguration(chests_file);
	}

	/**
	 * Load the localizable YAML file.
	 */
	private void loadLocalizable() {
		getLocales();
		for (Localizer locale : Localizer.values()) {
			File localeable = new File(getDataFolder() + File.separator + "locales" + File.separator + locale.getCode() + ".yml");
			if (!localeable.exists())
				try {
					localeable.createNewFile();
				} catch (IOException e) {
					getLogger().warning("Unable to create locale " + locale.getCode());
					e.printStackTrace();
				}
			localizable.put(locale.getCode(), YamlConfiguration.loadConfiguration(localeable));
		}
	}

	/**
	 * Load the playerdata YAML file.
	 */
	private void loadPlayerData() {
		File playerdata_file = new File(getDataFolder() + File.separator + "playerdata.yml");

		/*
		 * Make sure the file exists.
		 */
		if (!playerdata_file.exists())
			return;
		FileConfiguration data = YamlConfiguration.loadConfiguration(playerdata_file);
		PlayerData.updateData(data);
		playerdata_file.delete();
	}

	/**
	 * Load the research YAML file.
	 */
	private void loadResearch() {
		File research_file = new File(getDataFolder() + File.separator + "research.yml");

		/*
		 * Make sure the file exists.
		 */
		if (!research_file.exists())
			try {
				research_file.createNewFile();
			} catch (IOException e) {
				getLogger().warning("Unable to save research.yml: " + e.getMessage());
			}
		research = YamlConfiguration.loadConfiguration(research_file);
	}

	/**
	 * Load the spawn YAML file.
	 */
	private void loadSpawn() {
		File spawn_file = new File(getDataFolder() + File.separator + "spawn.yml");

		/*
		 * Make sure the file exists.
		 */
		if (!spawn_file.exists())
			try {
				spawn_file.createNewFile();
			} catch (IOException e) {
				getLogger().warning("Unable to save spawn.yml: " + e.getMessage());
			}
		spawn = YamlConfiguration.loadConfiguration(spawn_file);
	}

	/**
	 * Clean up all of the static variables that have the potential to hold a
	 * lot of space in memory.
	 */
	private void nullifyStatics() {
		BlockCommand.blockChangers = null;
		Sync.safeLogoutPlayers = null;
		MedKit.clearKits();
		if (getServer().getPluginManager().getPlugin("DisguiseCraft") != null)
			myz.utilities.DisguiseUtils.disable();
		Utils.packets = null;
	}

	/**
	 * Remove all buffs for the given player and make everything right again.
	 * 
	 * @param player
	 *            The player.
	 */
	private void wipeBuffs(Player player, boolean clearInventory) {
		stopBleeding(player, false);
		stopPoison(player, false);
		fixLeg(player, false);
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);
		if (clearInventory) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
		}
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());
		player.setSaturation(20);
		player.setFoodLevel(20);
	}

	/**
	 * Add a friend to the friender's friend list.
	 * 
	 * @param friender
	 *            The player that is friending the other.
	 * @param friended
	 *            The friended player.
	 */
	public void addFriend(Player friender, UUID friended) {
		PlayerData data = PlayerData.getDataFor(friender);
		if (data != null && !data.getFriends().contains(friended.toString())) {
			data.addFriend(friended);
			friender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(friender), "friend.added", getName(friended)));
		}
		if (sql.isConnected() && !sql.getStringList(friender.getUniqueId(), "friends").contains(friended.toString())) {
			String current = sql.getString(friender.getUniqueId(), "friends");
			sql.set(friender.getUniqueId(), "friends", current + (current.isEmpty() ? "" : ",") + friended.toString(), true);
			friender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(friender), "friend.added", getName(friended)));
		}
	}

	/**
	 * Add a player to the MyZ game. Also checks for kickban if the useprelogin
	 * configuration option is disabled.
	 * 
	 * @param player
	 *            The player.
	 */
	public void addPlayer(Player player, boolean clearInventory) {
		PlayerData playerdata = PlayerData.getDataFor(player);

		if (!(Boolean) Configuration.getConfig(Configuration.PRELOGIN)) {
			/*
			 * Check if the player is still banned against the playerdata and sql.
			 */
			long now = System.currentTimeMillis();
			long timeOfKickExpiry;
			if (playerdata != null
					&& (timeOfKickExpiry = playerdata.getTimeOfKickban() + (Integer) Configuration.getConfig(Configuration.KICKBAN_TIME)
							* 1000) >= now
					|| MyZ.instance.getSQLManager().isConnected()
					&& (timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(player.getUniqueId(), "timeOfKickban")
							+ (Integer) Configuration.getConfig(Configuration.KICKBAN_TIME) * 1000) >= now) {
				player.kickPlayer(Messenger.getConfigMessage(Localizer.DEFAULT, "kick.recur", (timeOfKickExpiry - now) / 1000 + ""));
				return;
			}
		}
		online_players.add(player.getUniqueId());

		/*
		 * Add the player to the dataset if they're not in it yet. If they weren't in it, put them at the spawn.
		 */
		if (playerdata == null && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
			playerdata = PlayerData.createDataFor(player, player.getUniqueId(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0L,
					new ArrayList<String>(), 0, 20, "", 0, 0, 0, 0, 0, 0, 0, 0, false, false);
			putPlayerAtSpawn(player, false, clearInventory);
		}
		if (sql.isConnected() && !sql.isIn(player.getUniqueId())) {
			sql.add(player);
			putPlayerAtSpawn(player, false, clearInventory);
		}

		setThirst(player);

		if (playerdata != null && playerdata.getTimeOfKickban() != 0)
			playerdata.setTimeOfKickban(0L);
		if (sql.isConnected() && sql.getLong(player.getUniqueId(), "timeOfKickban") != 0)
			sql.set(player.getUniqueId(), "timeOfKickban", 0L, true);

		/*
		 * Cache all values asynchronously to reduce runtime lag.
		 */
		if (sql.isConnected())
			sql.createLinks(player.getUniqueId());

		/*
		 * Teleport the player back to the world spawn if they were killed by an NPC logout.
		 */
		if (playerdata != null && playerdata.wasKilledNPC() || sql.isConnected() && sql.getBoolean(player.getUniqueId(), "wasNPCKilled")) {
			Messenger.sendConfigMessage(player, "player_was_killed_npc");
			putPlayerAtSpawn(player, true, clearInventory);
		}

		if (MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled())
			if (playerdata != null && playerdata.isZombie() || MyZ.instance.getSQLManager().isConnected()
					&& MyZ.instance.getSQLManager().getBoolean(player.getUniqueId(), "isZombie"))
				DisguiseUtils.becomeZombie(player);
		if (MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled())
			if (playerdata != null && playerdata.isZombie() || MyZ.instance.getSQLManager().isConnected()
					&& MyZ.instance.getSQLManager().getBoolean(player.getUniqueId(), "isZombie"))
				LibsDisguiseUtils.becomeZombie(player);
	}

	/**
	 * Begin leg-break for this player. Will fail if the player already has a
	 * broken leg or doesn't have a PlayerData associated AND SQL is not
	 * connected.
	 * 
	 * @param player
	 *            The player.
	 */
	public void breakLeg(Player player) {
		if (!isLegBroken(player)) {
			PlayerBreakLegEvent event = new PlayerBreakLegEvent(player);
			getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null)
					data.setLegBroken(true);
				if (sql.isConnected())
					sql.set(player.getUniqueId(), "legBroken", true, true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
				Messenger.sendConfigMessage(player, "damage.leg_break");
			}
		}
	}

	/**
	 * End leg-break for this player. Will fail if the player already has a
	 * broken leg.
	 * 
	 * @param player
	 *            The player.
	 */
	public void fixLeg(Player player, boolean alert) {
		if (isLegBroken(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setLegBroken(false);
			if (sql.isConnected())
				sql.set(player.getUniqueId(), "legBroken", false, true);
			player.removePotionEffect(PotionEffectType.SLOW);
			if (alert)
				Messenger.sendConfigMessage(player, "damage.leg_fix");
		}
	}

	/**
	 * Get the blocks YAML.
	 * 
	 * @return The FileConfiguration for the blocks.yml or null if not loaded.
	 */
	public FileConfiguration getBlocksConfig() {
		return blocks;
	}

	/**
	 * Get the chest YAML.
	 * 
	 * @return The FileConfiguration for the chests.yml or null if not loaded.
	 */
	public FileConfiguration getChestsConfig() {
		return chests;
	}

	/**
	 * Get the list of flagged players, that is, players that have died and are
	 * about to be kicked. Ensures dead players don't spawn NPC's.
	 * 
	 * @return The list of players.
	 */
	public List<UUID> getFlagged() {
		return flags;
	}

	/**
	 * Get the folder that holds the locales.
	 * 
	 * @return The folder (MyZ-3/locales/).
	 */
	public File getLocales() {
		File folder = new File(getDataFolder() + File.separator + "locales");
		if (!folder.exists())
			folder.mkdir();
		return folder;
	}

	/**
	 * Get the localizable YAML.
	 * 
	 * @return The FileConfiguration for the localizable.yml or null if not
	 *         loaded.
	 * @param locale
	 *            The locale to get.
	 */
	public FileConfiguration getLocalizableConfig(Localizer locale) {
		return localizable.get(locale.getCode());
	}

	public String getName(UUID uid) {
		Map<UUID, String> map = lookupPlayers();
		for (UUID key : map.keySet())
			if (key.equals(uid))
				return map.get(key);
		return "Guest";
	}

	public Player getPlayer(UUID uid) {
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getUniqueId().equals(uid))
				return p;
		return null;
	}

	/**
	 * Get the playerdata YAML.
	 * 
	 * @param player
	 *            The player uuid of the data to get for.
	 * @return The FileConfiguration for the specified player's PlayerData or
	 *         null if not loaded or can't load.
	 */
	public FileConfiguration getPlayerDataConfig(UUID player) {
		if (!playerdata.containsKey(player)) {
			File datafolder = new File(getDataFolder() + File.separator + "data");
			if (!datafolder.exists())
				datafolder.mkdir();
			File datafile = new File(getDataFolder() + File.separator + "data" + File.separator + SQLManager.UUIDtoString(player) + ".yml");
			if (!datafile.exists())
				try {
					datafile.createNewFile();
				} catch (Exception e) {
					Messenger.sendConsoleMessage("&4Unable to save a new PlayerData file for " + SQLManager.UUIDtoString(player) + ": "
							+ e.getMessage());
					return null;
				}
			FileConfiguration config = YamlConfiguration.loadConfiguration(datafile);
			playerdata.put(player, config);
			return config;
		}
		return playerdata.get(player);
	}

	/**
	 * Get the rank number of a player. Defaults to 0.
	 * 
	 * @param player
	 *            The player.
	 * @return The rank associated with the player.
	 */
	public int getRankFor(Player player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.getRank();
		if (sql.isConnected())
			return sql.getInt(player.getUniqueId(), "rank");
		return 0;
	}

	/**
	 * Get the research YAML.
	 * 
	 * @return The FileConfiguration for the research.yml or null if not loaded.
	 */
	public FileConfiguration getResearchConfig() {
		return research;
	}

	/**
	 * Get the spawn YAML.
	 * 
	 * @return The FileConfiguration for the spawn.yml or null if not loaded.
	 */
	public FileConfiguration getSpawnConfig() {
		return spawn;
	}

	/**
	 * Gets the SQLManager object.
	 * 
	 * @return the SQLManager.
	 */
	public SQLManager getSQLManager() {
		return sql;
	}

	public UUID getUID(String name) {
		Map<UUID, String> map = lookupPlayers();
		if (map.containsValue(name))
			for (UUID key : map.keySet())
				if (map.get(key).equals(name))
					return key;
		return UUID.randomUUID();
	}

	/**
	 * @see isBandit(UUID player)
	 */
	public boolean isBandit(Player player) {
		return isBandit(player.getUniqueId());
	}

	/**
	 * Whether or not this player is a bandit.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is a bandit.
	 */
	public boolean isBandit(UUID player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isBandit();
		if (sql.isConnected())
			return sql.getInt(player, "player_kills_life") >= (Integer) Configuration.getConfig(Configuration.BANDIT);
		// Theoretically impossible to get to this case.
		return false;
	}

	/**
	 * Whether or not the given player is currently bleeding.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is bleeding.
	 */
	public boolean isBleeding(Player player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isBleeding();
		if (sql.isConnected())
			return sql.getBoolean(player.getUniqueId(), "isBleeding");
		return false;
	}

	/**
	 * @see isFriend(UUID player, UUID name)
	 */
	public boolean isFriend(Player player, UUID name) {
		return isFriend(player.getUniqueId(), name);
	}

	/**
	 * Whether or not the uuid provided is a friend of the given player.
	 * 
	 * @param player
	 *            The player uuid.
	 * @param name
	 *            The friend uuid to check.
	 * @return True if @param name is a friend of @param player.
	 */
	public boolean isFriend(UUID player, UUID name) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isFriend(name);
		if (sql.isConnected())
			return sql.getStringList(player, "friends").contains(name);
		// Theoretically impossible to get to this case.
		return false;
	}

	/**
	 * @see isHealer(UUID player)
	 */
	public boolean isHealer(Player player) {
		return isHealer(player.getUniqueId());
	}

	/**
	 * Whether or not this player is a healer.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is a healer.
	 */
	public boolean isHealer(UUID player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isHealer();
		if (sql.isConnected())
			return sql.getInt(player, "heals_life") >= (Integer) Configuration.getConfig(Configuration.HEALER);
		// Theoretically impossible to get to this case.
		return false;
	}

	/**
	 * Whether or not the given player currently has a broken leg.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player has a broken leg.
	 */
	public boolean isLegBroken(Player player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isLegBroken();
		if (sql.isConnected())
			return sql.getBoolean(player.getUniqueId(), "legBroken");
		return false;
	}

	/**
	 * Whether or not the player is currently playing MyZ.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is playing MyZ.
	 */
	public boolean isPlayer(Player player) {
		return online_players.contains(player.getUniqueId());
	}

	/**
	 * Whether or not the given player is currently poisoned.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is poisoned.
	 */
	public boolean isPoisoned(Player player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isPoisoned();
		if (sql.isConnected())
			return sql.getBoolean(player.getUniqueId(), "isPoisoned");
		return false;
	}

	public Map<UUID, String> lookupPlayers() {
		Map<UUID, String> map = new HashMap<UUID, String>();
		try {
			File folder = new File(getDataFolder() + File.separator + "mappings.yml");
			if (!folder.exists())
				folder.createNewFile();
			FileConfiguration yaml = YamlConfiguration.loadConfiguration(folder);
			for (String key : yaml.getKeys(false))
				map.put(UUID.fromString(key), yaml.getString(key));
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4Unable to retrieve UUID mappings: " + exc.getMessage());
		}
		return map;
	}

	public void map(Player player) {
		try {
			File folder = new File(getDataFolder() + File.separator + "mappings.yml");
			if (!folder.exists())
				folder.createNewFile();
			FileConfiguration yaml = YamlConfiguration.loadConfiguration(folder);
			yaml.set(player.getUniqueId().toString(), player.getName());
			yaml.save(folder);

			// Store their name.
			if (sql.isConnected())
				sql.set(player.getUniqueId(), "name", player.getName(), true);
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4Unable to add, save or retrieve UUID mappings: " + exc.getMessage());
		}
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		ChestManager.respawnAll(true);
		boolean disguise = getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled();

		// Remove all entities in all worlds as reloads will cause classloader
		// issues to
		// do with overriding the pathfinding and entity.
		MobUtils.removeCustomPlayers();
		for (Player player : Bukkit.getOnlinePlayers())
			removePlayer(player, false);
		if (disguise)
			for (Player player : getServer().getOnlinePlayers())
				myz.utilities.LibsDisguiseUtils.undisguise(player);
		MobUtils.clearCustomPlayers();
		for (String name : (List<String>) Configuration.getConfig(Configuration.WORLDS)) {
			World world = Bukkit.getWorld(name);
			if (world == null) {
				Messenger.sendConsoleMessage("&4Specified world (" + name + ") does not exist! Please update your config.yml");
				continue;
			}
			for (Entity entity : world.getEntitiesByClass(LivingEntity.class))
				if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.GIANT || entity.getType() == EntityType.HORSE
						|| entity.getType() == EntityType.PIG_ZOMBIE || entity.getType() == EntityType.SKELETON) {
					if (disguise)
						myz.utilities.LibsDisguiseUtils.undisguise((LivingEntity) entity);
					entity.remove();
				}
		}
		MobUtils.unregister();
		if (Utils.packets != null)
			Utils.packets.clear();
		MessageUtils.removeAllHolograms();
		nullifyStatics();
	}

	@Override
	public void onEnable() {
		version = Compat.valueOf(NMSUtils.version);
		if (version == null) {
			getLogger()
					.warning("This version of MyZ is not compatible with your version of Craftbukkit (" + getServer().getVersion() + ")");
			getLogger().warning("Disabling MyZ.");
			setEnabled(false);
			return;
		} else {
			getLogger().info("Using hooks for " + version);
		}

		instance = this;

		if (getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().isPluginEnabled("Vault"))
			vault = VaultUtils.setupPermissions();

		getDataFolder().mkdir();
		File defaultConfig = new File(getDataFolder() + File.separator + "config.yml");
		if (!defaultConfig.exists())
			try {
				defaultConfig.createNewFile();
			} catch (Exception exc) {
				getLogger().warning("Unable to save default config: " + exc.getMessage());
			}
		loadPlayerData();
		loadBlocks();
		loadLocalizable();
		loadSpawn();
		loadChests();
		loadResearch();
		ChestManager.respawnAll(true);

		/*
		 * Register the new enchantment so that MedKits can have their glow.
		 */
		MedKit.registerNewEnchantment();

		Configuration.reload(true);

		sql = new SQLManager((String) Configuration.getConfig(Configuration.HOST), (Integer) Configuration.getConfig(Configuration.PORT),
				(String) Configuration.getConfig(Configuration.DATABASE), (String) Configuration.getConfig(Configuration.USER),
				(String) Configuration.getConfig(Configuration.PASSWORD));

		/*
		 * Register threads.
		 */
		getServer().getScheduler().runTaskTimerAsynchronously(this, new aSync(), 100L, 20L);
		getServer().getScheduler().runTaskTimer(this, new Sync(), 100L, 20L);

		/*
		 * Register all commands.
		 */
		getCommand("friend").setExecutor(new FriendCommand());
		getCommand("friends").setExecutor(new FriendsCommand());
		getCommand("start").setExecutor(new SpawnCommand());
		if (getServer().getPluginManager().getPlugin("WorldEdit") != null
				&& getServer().getPluginManager().getPlugin("WorldEdit").isEnabled())
			getCommand("setlobby").setExecutor(new SetLobbyCommand());
		getCommand("addspawn").setExecutor(new AddSpawnCommand());
		getCommand("removespawn").setExecutor(new RemoveSpawnCommand());
		getCommand("spawnpoints").setExecutor(new SpawnsCommand());
		getCommand("savekit").setExecutor(new SaveKitCommand());
		getCommand("saverank").setExecutor(new SaveRankCommand());
		getCommand("savemedkit").setExecutor(new CreateMedKitCommand());
		getCommand("setrank").setExecutor(new SetRankCommand());
		getCommand("clan").setExecutor(new ClanCommand());
		getCommand("joinclan").setExecutor(new JoinClanCommand());
		getCommand("getid").setExecutor(new GetUIDCommand());
		getCommand("blockallow").setExecutor(new BlockCommand());
		getCommand("blocks").setExecutor(new AllowedCommand());
		getCommand("research").setExecutor(new ResearchCommand());
		getCommand("setresearch").setExecutor(new AddResearchCommand());
		getCommand("stats").setExecutor(new StatsCommand());
		getCommand("myz").setExecutor(new TranslateCommand());
		getCommand("configure").setExecutor(new ItemConfigurationCommand());
		getCommand("chestscan").setExecutor(new ChestScanCommand());
		getCommand("chestset").setExecutor(new ChestSetCommand());
		getCommand("chestget").setExecutor(new ChestGetCommand());
		getCommand("lootset").setExecutor(new LootSetCommand());
		getCommand("mtranslate").setExecutor(new TranslateCommand());

		/*
		 * Register all listeners.
		 */
		PluginManager p = getServer().getPluginManager();
		p.registerEvents(new JoinQuit(), this);
		p.registerEvents(new ChestScanner(), this);
		p.registerEvents(new BlockEvent(), this);
		p.registerEvents(new AutoFriend(), this);
		p.registerEvents(new Heal(), this);
		p.registerEvents(new CancelZombieDamage(), this);
		p.registerEvents(new ConsumeFood(), this);
		p.registerEvents(new PlayerHurtEntity(), this);
		p.registerEvents(new Visibility(), this);
		p.registerEvents(new PlayerKillEntity(), this);
		p.registerEvents(new EntityHurtPlayer(), this);
		p.registerEvents(new PlayerDeath(), this);
		p.registerEvents(new EntitySpawn(), this);
		p.registerEvents(new PlayerSummonGiant(), this);
		p.registerEvents(new Chat(), this);
		p.registerEvents(new CancelPlayerEvents(), this);
		p.registerEvents(new Movement(), this);
		p.registerEvents(new PlayerTakeDamage(), this);
		p.registerEvents(new ResearchItem(), this);
		if (getServer().getPluginManager().getPlugin("TagAPI") != null && getServer().getPluginManager().getPlugin("TagAPI").isEnabled())
			p.registerEvents(new KittehTag(), this);
		if (getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled())
			p.registerEvents(new UndisguiseListener(), this);
		if (getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled()) {
			myz.utilities.LibsDisguiseUtils.setup();
			p.registerEvents(new LibsUndisguiseListener(), this);
		}

		/*
		 * Connect to SQL or use PlayerData.
		 */
		getServer().getScheduler().runTaskLaterAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				sql.connect();
				if (!sql.isConnected() && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
					Messenger.sendConsoleMessage(ChatColor.GREEN + "Using PlayerData for this session.");
					alertOps = true;
					Messenger.sendConsoleMessage(ChatColor.YELLOW + "Visit http://my-z.org/request.php to get a free MyZ MySQL database.");
					Configuration.saveConfig(Configuration.DATASTORAGE, true, false);
				} else if (sql.isConnected() && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
					Messenger.sendConsoleMessage(ChatColor.GREEN + "Using MySQL for this session.");
					Configuration.saveConfig(Configuration.DATASTORAGE, false, false);
				}

				/*
				 * Add all players that weren't already in the playerdata to it (in case of a reload).
				 */
				for (String world : (List<String>) Configuration.getConfig(Configuration.WORLDS)) {
					if (Bukkit.getWorld(world) == null) {
						Messenger.sendConsoleMessage("&4Specified world (" + world + ") does not exist! Please update your config.yml");
						continue;
					}
					for (Player player : Bukkit.getWorld(world).getPlayers()) {
						addPlayer(player, true);
						PlayerData data = null;
						if ((data = PlayerData.getDataFor(player)) == null || sql.isConnected() && !sql.isIn(player.getUniqueId())) {
							if (data == null && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
								PlayerData.createDataFor(player, player.getUniqueId(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false,
										0L, new ArrayList<String>(), 0, (Integer) Configuration.getConfig(Configuration.THIRST_MAX), "", 0,
										0, 0, 0, 0, 0, 0, 0, false, false);
								putPlayerAtSpawn(player, false, true);
							}
							if (sql.isConnected() && !sql.isIn(player.getUniqueId())) {
								sql.add(player);
								putPlayerAtSpawn(player, false, true);
							}
						}
					}
				}
			}
		}, 0L);

		MobUtils.register();

		/*
		 * Run Metrics.
		 */
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException exc) {
			Messenger.sendConsoleMessage("&4Metrics failed to start.");
		}

		/*
		 * Autoupdate.
		 */
		if ((Boolean) Configuration.getConfig(Configuration.AUTOUPDATE))
			new Updater(this, 55557, getFile(), false);
	}

	/**
	 * Return the player to the spawn and clear all buffs. If it was a death,
	 * increment the death count and kickban if necessary.
	 * 
	 * @param player
	 *            The player.
	 * @param wasDeath
	 *            Whether or not the return to spawn was a result of a death.
	 */
	public void putPlayerAtSpawn(Player player, boolean wasDeath, boolean clearInventory) {
		if (!player.isOnline())
			return;
		Teleport.teleport(player, player.getWorld().getSpawnLocation(), false);
		if (getServer().getPluginManager().isPluginEnabled("essentials"))
			Bukkit.dispatchCommand(player, "spawn");
		wipeBuffs(player, clearInventory);

		if (wasDeath) {
			PlayerData data = PlayerData.getDataFor(player);

			setThirst(player, (Integer) Configuration.getConfig(Configuration.THIRST_MAX));

			boolean wasNPCKilled = false;
			if (data != null) {
				wasNPCKilled = data.wasKilledNPC();
				data.setWasKilledNPC(false);
				data.setDeaths(data.getDeaths() + 1);
				data.setMinutesAliveLife(0);
				data.setPlayerKillsLife(0);
				data.setZombieKillsLife(0);
				data.setPigmanKillsLife(0);
				data.setGiantKillsLife(0);
			}
			if (sql.isConnected()) {
				wasNPCKilled = sql.getBoolean(player.getUniqueId(), "wasNPCKilled");
				sql.set(player.getUniqueId(), "wasNPCKilled", false, true);
				sql.set(player.getUniqueId(), "deaths", sql.getInt(player.getUniqueId(), "deaths") + 1, true);
				sql.set(player.getUniqueId(), "player_kills_life", 0, true);
				sql.set(player.getUniqueId(), "zombie_kills_life", 0, true);
				sql.set(player.getUniqueId(), "pigman_kills_life", 0, true);
				sql.set(player.getUniqueId(), "giant_kills_life", 0, true);
				sql.set(player.getUniqueId(), "minutes_alive_life", 0, true);
			}
			/*
			 * Kick the player if kickban is enabled and log their time of kick.
			 */
			if ((Boolean) Configuration.getConfig(Configuration.KICKBAN) && !wasNPCKilled)
				if (data != null && data.getRank() <= 0 || sql.isConnected() && sql.getInt(player.getUniqueId(), "rank") <= 0
						&& !player.getName().equals("MrTeePee")) {
					flags.add(player.getUniqueId());
					player.kickPlayer(Messenger.getConfigMessage(Localizer.getLocale(player), "kick.come_back",
							Configuration.getConfig(Configuration.KICKBAN_TIME) + ""));
				}
		}
	}

	/**
	 * Remove a friend from the unfriender's friend list.
	 * 
	 * @param unfriender
	 *            The player that is unfriending the other.
	 * @param unfriended
	 *            The unfriended player.
	 */
	public void removeFriend(Player unfriender, UUID unfriended) {
		PlayerData data = PlayerData.getDataFor(unfriender);
		if (data != null && data.getFriends().contains(unfriended.toString())) {
			data.removeFriend(unfriended);
			unfriender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(unfriender), "friend.removed", getName(unfriended)));
		}
		if (sql.isConnected() && sql.getStringList(unfriender.getUniqueId(), "friends").contains(unfriended.toString())) {
			sql.set(unfriender.getUniqueId(),
					"friends",
					sql.getString(unfriender.getUniqueId(), "friends").replaceAll("," + unfriended.toString(), "")
							.replaceAll(unfriended.toString() + ",", ""), true);
			unfriender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(unfriender), "friend.removed", getName(unfriended)));
		}
	}

	/**
	 * Remove a player from the MyZ game. Will fail if the player is not in the
	 * game.
	 * 
	 * @param player
	 *            The player.
	 * @param wasDeath
	 *            Whether or not the removal reason was due to a death.
	 * @return True if the player was removed, false otherwise.
	 */
	public boolean removePlayer(Player player, boolean wasDeath) {
		if (isPlayer(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null && wasDeath) {
				data.setBleeding(false);
				data.setPoisoned(false);
				data.setThirst((Integer) Configuration.getConfig(Configuration.THIRST_MAX));
				data.setZombie(false);
			}
			if (sql.isConnected() && wasDeath) {
				sql.set(player.getUniqueId(), "isBleeding", false, true);
				sql.set(player.getUniqueId(), "isPoisoned", false, true);
				sql.set(player.getUniqueId(), "thirst", Configuration.getConfig(Configuration.THIRST_MAX), true);
				sql.set(player.getUniqueId(), "isZombie", false, true);
			}

			if (getServer().getPluginManager().getPlugin("DisguiseCraft") != null
					&& getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled())
				myz.utilities.DisguiseUtils.undisguise(player);
			if (getServer().getPluginManager().getPlugin("LibsDisguises") != null
					&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled())
				myz.utilities.LibsDisguiseUtils.undisguise(player);

			if ((Boolean) Configuration.getConfig(Configuration.KICKBAN) && wasDeath && !player.getName().equals("MrTeePee")
					&& !player.hasPermission("myz.nokick")) {
				if (data != null)
					data.setTimeOfKickban(System.currentTimeMillis());
				if (sql.isConnected())
					sql.set(player.getUniqueId(), "timeOfKickban", System.currentTimeMillis(), true);
			}

			if (!(Boolean) Configuration.getConfig(Configuration.SAVE_UNRANKED) && getRankFor(player) <= 0
					&& !player.getName().equals("MrTeePee")) {
				if (data != null) {
					for (UUID friend : data.getFriends())
						data.removeFriend(friend);
					data.setDeaths(0);
					data.setGiantKills(0);
					data.setGiantKillsLife(0);
					data.setGiantKillsLifeRecord(0);
					data.setPigmanKills(0);
					data.setPigmanKillsLife(0);
					data.setPigmanKillsLifeRecord(0);
					data.setPlayerKills(0);
					data.setPlayerKillsLife(0);
					data.setPlayerKillsLifeRecord(0);
					data.setZombieKills(0);
					data.setZombieKillsLife(0);
					data.setZombieKillsLifeRecord(0);
					data.setMinutesAlive(0);
					data.setMinutesAliveLife(0);
					data.setMinutesAliveLifeRecord(0);
				}
				if (sql.isConnected()) {
					sql.set(player.getUniqueId(), "friends", "''", true);
					sql.set(player.getUniqueId(), "deaths", 0, true);
					sql.set(player.getUniqueId(), "giant_kills", 0, true);
					sql.set(player.getUniqueId(), "giant_kills_life", 0, true);
					sql.set(player.getUniqueId(), "giant_kills_life_record", 0, true);
					sql.set(player.getUniqueId(), "pigman_kills", 0, true);
					sql.set(player.getUniqueId(), "pigman_kills_life", 0, true);
					sql.set(player.getUniqueId(), "pigman_kills_life_record", 0, true);
					sql.set(player.getUniqueId(), "player_kills", 0, true);
					sql.set(player.getUniqueId(), "player_kills_life", 0, true);
					sql.set(player.getUniqueId(), "player_kills_life_record", 0, true);
					sql.set(player.getUniqueId(), "zombie_kills", 0, true);
					sql.set(player.getUniqueId(), "zombie_kills_life", 0, true);
					sql.set(player.getUniqueId(), "zombie_kills_life_record", 0, true);
					sql.set(player.getUniqueId(), "minutes_alive", 0L, true);
					sql.set(player.getUniqueId(), "minutes_alive_life", 0, true);
					sql.set(player.getUniqueId(), "minutes_alive_record", 0, true);
				}
			}
			online_players.remove(player.getUniqueId());
			return true;
		}
		return false;
	}

	/**
	 * Save the blocks.yml
	 */
	public void saveBlocksConfig() {
		try {
			blocks.save(new File(MyZ.instance.getDataFolder() + File.separator + "blocks.yml"));
		} catch (IOException e) {
			Messenger.sendConsoleMessage("&4Unable to save blocks.yml: " + e.getMessage());
		}
	}

	/**
	 * Save the chests.yml
	 */
	public void saveChestConfig() {
		try {
			chests.save(new File(MyZ.instance.getDataFolder() + File.separator + "chests.yml"));
		} catch (IOException e) {
			Messenger.sendConsoleMessage("&4Unable to save chests.yml: " + e.getMessage());
		}
	}

	/**
	 * Save the localizable.yml
	 * 
	 * @param locale
	 *            The locale to save.
	 */
	public void saveLocalizableConfig(Localizer locale) {
		try {
			localizable.get(locale.getCode()).save(
					new File(MyZ.instance.getDataFolder() + File.separator + "locales" + File.separator + locale.getCode() + ".yml"));
		} catch (IOException e) {
			Messenger.sendConsoleMessage("&4Unable to save localizable.yml: " + e.getMessage());
		}
	}

	/**
	 * Save the research.yml
	 */
	public void saveResearchConfig() {
		try {
			research.save(new File(MyZ.instance.getDataFolder() + File.separator + "research.yml"));
		} catch (IOException e) {
			Messenger.sendConsoleMessage("&4Unable to save research.yml: " + e.getMessage());
		}
	}

	/**
	 * Save the spawn.yml
	 */
	public void saveSpawnConfig() {
		try {
			spawn.save(new File(MyZ.instance.getDataFolder() + File.separator + "spawn.yml"));
		} catch (IOException e) {
			Messenger.sendConsoleMessage("&4Unable to save spawn.yml: " + e.getMessage());
		}
	}

	/**
	 * Sync the player's thirst level with that stored in the data.
	 * 
	 * @param player
	 *            The player.
	 */
	public void setThirst(Player player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			player.setLevel(data.getThirst());
		if (sql.isConnected())
			player.setLevel(sql.getInt(player.getUniqueId(), "thirst"));
	}

	/**
	 * Set the thirst level of the player and ensure the data is updated.
	 * 
	 * @param player
	 *            The player.
	 * @param level
	 *            The thirst level.
	 */
	public void setThirst(Player player, int level) {
		if (level == player.getLevel())
			return;
		if (!Validate.inWorld(player.getLocation()))
			return;
		if (level != (Integer) Configuration.getConfig(Configuration.THIRST_MAX)) {
			if (level > (Integer) Configuration.getConfig(Configuration.THIRST_MAX))
				level = (Integer) Configuration.getConfig(Configuration.THIRST_MAX);
			PlayerWaterDecayEvent event = level < player.getLevel() ? new PlayerWaterDecayEvent(player) : null;
			if (event != null)
				getServer().getPluginManager().callEvent(event);
			if (event == null || !event.isCancelled()) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null)
					data.setThirst(level);
				if (sql.isConnected())
					sql.set(player.getUniqueId(), "thirst", level, true);
				player.setLevel(level);
			}
		} else {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setThirst(level);
			if (sql.isConnected())
				sql.set(player.getUniqueId(), "thirst", level, true);
			player.setLevel(level);
		}
	}

	/**
	 * Spawn a player at a random spawnpoint.
	 * 
	 * @see spawnPlayer(Player player, int spawnpoint)
	 * @param player
	 *            The player.
	 * @param spawningAttempts
	 *            The number of spawning attempts.
	 */
	public void spawnPlayer(Player player, int spawningAttempts) {
		spawnPlayer(player, random.nextInt(Configuration.getNumberOfSpawns() == 0 ? 1 : Configuration.getNumberOfSpawns()) + 1, false,
				spawningAttempts);
	}

	/**
	 * Spawn a player at a given spawnpoint. Does not check if the player is
	 * currently in the lobby. Does, however, check to make sure no non-friends
	 * are nearby.
	 * 
	 * @param player
	 *            The player.
	 * @param spawnpoint
	 *            The spawnpoint.
	 * @param withInitiallySpecifiedSpawnpoint
	 *            Whether or not the player specified the spawnpoint during
	 *            execution. Leads to two cases: 1) Player specified and cannot
	 *            spawn there, abort spawning. or 2) Unspecified and cannot
	 *            spawn there, choose new point.
	 * @param spawningAttempts
	 *            The number of failed attempts during spawning. Caps at 25 at
	 *            which point it simply gives up with the unable to spawn
	 *            message.
	 */
	public void spawnPlayer(Player player, int spawnpoint, boolean withInitiallySpecifiedSpawnpoint, int spawningAttempts) {
		PlayerData data = PlayerData.getDataFor(player);
		if ((Integer) Configuration.getSpawn("spawn.numbered_requires_rank") > 0 && withInitiallySpecifiedSpawnpoint) {
			if (data != null)
				if (data.getRank() < (Integer) Configuration.getSpawn("spawn.numbered_requires_rank")) {
					Messenger.sendConfigMessage(player, "command.spawn.requires_rank");
					return;
				}
			if (sql.isConnected())
				if (sql.getInt(player.getUniqueId(), "rank") < (Integer) Configuration.getSpawn("spawn.numbered_requires_rank")) {
					Messenger.sendConfigMessage(player, "command.spawn.requires_rank");
					return;
				}
		}

		World world = player.getWorld();
		if (spawnpoint == -1) {
			spawnPlayer(player, spawningAttempts + 1);
			return;
		}
		spawnpoint--; // Ensure we're using list-type spawnpoint instead of
						// player-entry.

		WorldlessLocation spawnLocation = Configuration.getSpawnpoint(spawnpoint);
		Location spawn = new Location(world, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(),
				spawnLocation.getPitch());
		spawn.add(0.5, 0, 0.5);

		/*
		 * An enemy was nearby, stop the spawning.
		 */
		if (Utils.isCreatureNearby(player, spawn, (Integer) Configuration.getSpawn("spawn.safespawn_radius"))) {
			if (withInitiallySpecifiedSpawnpoint || spawningAttempts >= 25)
				Messenger.sendConfigMessage(player, "command.spawn.unable_to_spawn");
			else
				spawnPlayer(player, spawningAttempts + 1);
			return;
		}

		PlayerSpawnInWorldEvent event = new PlayerSpawnInWorldEvent(player);
		getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			wipeBuffs(player, true);
			setThirst(player, (Integer) Configuration.getConfig(Configuration.THIRST_MAX));

			Teleport.teleport(player, spawn, false);

			for (PotionEffect potioneffect : Configuration.getSpawnPotionEffects())
				player.addPotionEffect(potioneffect);

			if ((Boolean) Configuration.getSpawn("zombie_spawn"))
				if (random.nextInt(20) == 0 && getServer().getPluginManager().getPlugin("DisguiseCraft") != null
						&& getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled()) {
					myz.utilities.DisguiseUtils.becomeZombie(player);
					player.getInventory().setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (byte) 2));
					Messenger.sendConfigMessage(player, "spawn.zombie");
					if (data != null)
						data.setZombie(true);
					if (sql.isConnected())
						sql.set(player.getUniqueId(), "isZombie", true, true);
					return;
				} else if (random.nextInt(20) == 0 && getServer().getPluginManager().getPlugin("LibsDisguises") != null
						&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled()) {
					myz.utilities.LibsDisguiseUtils.becomeZombie(player);
					Messenger.sendConfigMessage(player, "spawn.zombie");
					if (data != null)
						data.setZombie(true);
					if (sql.isConnected())
						sql.set(player.getUniqueId(), "isZombie", true, true);
					return;
				}

			int rank = 0;
			if (data != null)
				rank = data.getRank();
			if (sql.isConnected())
				rank = sql.getInt(player.getUniqueId(), "rank");

			try {
				player.getInventory().setArmorContents(Configuration.getArmorContents(rank, player));
			} catch (NullPointerException exc) {

			}
			try {
				player.getInventory().setContents(Configuration.getInventory(rank, player));
			} catch (NullPointerException exc) {

			}

			Configuration.sendSpawnMessage(player, rank);
		}
	}

	/**
	 * Begin bleeding for this player. Will fail if the player is already
	 * bleeding or doesn't have a PlayerData associated AND SQL is not
	 * connected.
	 * 
	 * @param player
	 *            The player.
	 */
	public void startBleeding(Player player) {
		if (!isBleeding(player)) {
			PlayerBeginBleedingEvent event = new PlayerBeginBleedingEvent(player);
			getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null)
					data.setBleeding(true);
				if (sql.isConnected())
					sql.set(player.getUniqueId(), "isBleeding", true, true);
				Messenger.sendConfigMessage(player, "damage.bleed_begin");
			}
		}
	}

	/**
	 * Begin poison for this player. Will fail if the player is already poisoned
	 * or doesn't have a PlayerData associated AND SQL is not connected.
	 * 
	 * @param player
	 *            The player.
	 */
	public void startPoison(Player player) {
		if (!isPoisoned(player)) {
			PlayerBeginPoisonEvent event = new PlayerBeginPoisonEvent(player);
			getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null)
					data.setPoisoned(true);
				if (sql.isConnected())
					sql.set(player.getUniqueId(), "isPoisoned", true, true);
				Messenger.sendConfigMessage(player, "damage.poison_begin");
			}
		}
	}

	/**
	 * End bleeding for this player. Will fail if the player is not bleeding.
	 * 
	 * @param player
	 *            The player.
	 */
	public void stopBleeding(Player player, boolean alert) {
		if (isBleeding(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setBleeding(false);
			if (sql.isConnected())
				sql.set(player.getUniqueId(), "isBleeding", false, true);
			if (alert)
				Messenger.sendConfigMessage(player, "damage.bleed_end");
		}
	}

	/**
	 * End poison for this player. Will fail if the player is not poisoned.
	 * 
	 * @param player
	 *            The player.
	 */
	public void stopPoison(Player player, boolean alert) {
		if (isPoisoned(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setPoisoned(false);
			if (sql.isConnected())
				sql.set(player.getUniqueId(), "isPoisoned", false, true);
			if (alert)
				Messenger.sendConfigMessage(player, "damage.poison_end");
		}
	}
}
