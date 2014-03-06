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

import myz.api.PlayerBeginBleedingEvent;
import myz.api.PlayerBeginPoisonEvent;
import myz.api.PlayerSpawnInWorldEvent;
import myz.api.PlayerWaterDecayEvent;
import myz.chests.ChestManager;
import myz.chests.ChestScanner;
import myz.commands.AddResearchCommand;
import myz.commands.AddSpawnCommand;
import myz.commands.AllowedCommand;
import myz.commands.BaseCommand;
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
import myz.mobs.CustomEntityPlayer;
import myz.mobs.CustomEntityType;
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
import myz.utilities.Utils;
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
import org.mcstats.MetricsLite;

/**
 * @author Jordan
 * 
 *         The main plugin and the interface for most convenience methods.
 */
public class MyZ extends JavaPlugin {

	// TODO configurable death loot (?)
	// TODO sound attraction to (trap)doors.
	// TODO research point rank uppance @see ResearchItem#checkRankIncrease
	// TODO grave-digging
	// TODO use construction parts to create clans. Builder is clan owner.
	// Requires Build-in-a-box.
	// TODO 6. Save medkit and npc i there own yml files. Hard to rename npc the
	// way it is now.

	public static MyZ instance;
	private List<String> online_players = new ArrayList<String>();
	private FileConfiguration blocks, spawn, chests, research;
	private Map<String, FileConfiguration> localizable = new HashMap<String, FileConfiguration>();
	private SQLManager sql;
	private static final Random random = new Random();
	private List<CustomEntityPlayer> NPCs = new ArrayList<CustomEntityPlayer>();
	private Map<String, FileConfiguration> playerdata = new HashMap<String, FileConfiguration>();
	private List<String> flags = new ArrayList<String>();

	@Override
	public void onEnable() {
		if (!Bukkit.getServer().getClass().getPackage().getName().contains("v1_7_R1")) {
			getLogger()
					.warning("This version of MyZ is not compatible with your version of Craftbukkit (" + getServer().getVersion() + ")");
			getLogger().warning("Disabling MyZ 3");
			setEnabled(false);
			return;
		}

		instance = this;
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
		getCommand("myz").setExecutor(new BaseCommand());
		getCommand("configure").setExecutor(new ItemConfigurationCommand());
		getCommand("chestscan").setExecutor(new ChestScanCommand());
		getCommand("chestset").setExecutor(new ChestSetCommand());
		getCommand("chestget").setExecutor(new ChestGetCommand());
		getCommand("lootset").setExecutor(new LootSetCommand());

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
					Messenger.sendConsoleMessage(ChatColor.RED
							+ "MySQL is not connected and PlayerData is disabled. Enabling PlayerData for this session.");
					Configuration.saveConfig(Configuration.DATASTORAGE, true, false);
				} else if (sql.isConnected() && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
					Messenger
							.sendConsoleMessage(ChatColor.RED + "MySQL and PlayerData are enabled. Disabling PlayerData for this session.");
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
						if ((data = PlayerData.getDataFor(player)) == null || sql.isConnected() && !sql.isIn(player.getName())) {
							if (data == null && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
								PlayerData.createDataFor(player, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0L,
										new ArrayList<String>(), 0, (Integer) Configuration.getConfig(Configuration.THIRST_MAX), "", 0, 0,
										0, 0, 0, 0, 0, 0, false);
								putPlayerAtSpawn(player, false, true);
							}
							if (sql.isConnected() && !sql.isIn(player.getName())) {
								sql.add(player);
								putPlayerAtSpawn(player, false, true);
							}
						}
					}
				}
			}
		}, 0L);

		/*
		 * Register our custom mobs.
		 */
		CustomEntityType.registerEntities();

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

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		ChestManager.respawnAll(true);
		boolean disguise = getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled();

		// Remove all entities in all worlds as reloads will cause classloader
		// issues to
		// do with overriding the pathfinding and entity.
		for (CustomEntityPlayer player : NPCs)
			player.getBukkitEntity().remove();
		for (Player player : Bukkit.getOnlinePlayers()) {
			removePlayer(player, false);
		}
		if (disguise)
			for (Player player : getServer().getOnlinePlayers())
				myz.utilities.LibsDisguiseUtils.undisguise(player);
		NPCs.clear();
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
		// Attempt to clean up the custom classes.
		CustomEntityType.unregisterEntities();
		if (Utils.packets != null)
			Utils.packets.clear();
		nullifyStatics();
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
	 * Gets the SQLManager object.
	 * 
	 * @return the SQLManager.
	 */
	public SQLManager getSQLManager() {
		return sql;
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
	 * Load the localizable YAML file.
	 */
	private void loadLocalizable() {
		File folder = new File(getDataFolder() + File.separator + "locales");
		if (!folder.exists())
			folder.mkdir();
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
	 * Get the playerdata YAML.
	 * 
	 * @param player
	 *            The player name of the data to get for.
	 * @return The FileConfiguration for the specified player's PlayerData or
	 *         null if not loaded or can't load.
	 */
	public FileConfiguration getPlayerDataConfig(String player) {
		if (!playerdata.containsKey(player)) {
			File datafolder = new File(getDataFolder() + File.separator + "data");
			if (!datafolder.exists())
				datafolder.mkdir();
			File datafile = new File(getDataFolder() + File.separator + "data" + File.separator + player + ".yml");
			if (!datafile.exists())
				try {
					datafile.createNewFile();
				} catch (Exception e) {
					Messenger.sendConsoleMessage("&4Unable to save a new PlayerData file for " + player + ": " + e.getMessage());
					return null;
				}
			FileConfiguration config = YamlConfiguration.loadConfiguration(datafile);
			playerdata.put(player, config);
			return config;
		}
		return playerdata.get(player);
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

	/**
	 * Get the spawn YAML.
	 * 
	 * @return The FileConfiguration for the spawn.yml or null if not loaded.
	 */
	public FileConfiguration getSpawnConfig() {
		return spawn;
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
	 * Get the research YAML.
	 * 
	 * @return The FileConfiguration for the research.yml or null if not loaded.
	 */
	public FileConfiguration getResearchConfig() {
		return research;
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
			return sql.getBoolean(player.getName(), "isBleeding");
		return false;
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
			return sql.getBoolean(player.getName(), "isPoisoned");
		return false;
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
					sql.set(player.getName(), "isBleeding", true, true);
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
					sql.set(player.getName(), "isPoisoned", true, true);
				Messenger.sendConfigMessage(player, "damage.poison_begin");
			}
		}
	}

	/**
	 * End poison for this player. Will fail if the player is not poisoned.
	 * 
	 * @param player
	 *            The player.
	 */
	public void stopPoison(Player player) {
		if (isPoisoned(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setPoisoned(false);
			if (sql.isConnected())
				sql.set(player.getName(), "isPoisoned", false, true);
			Messenger.sendConfigMessage(player, "damage.poison_end");
		}
	}

	/**
	 * End bleeding for this player. Will fail if the player is not bleeding.
	 * 
	 * @param player
	 *            The player.
	 */
	public void stopBleeding(Player player) {
		if (isBleeding(player)) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setBleeding(false);
			if (sql.isConnected())
				sql.set(player.getName(), "isBleeding", false, true);
			Messenger.sendConfigMessage(player, "damage.bleed_end");
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
		PlayerData playerdata = PlayerData.getDataFor(player.getName());

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
					&& (timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(player.getName(), "timeOfKickban")
							+ (Integer) Configuration.getConfig(Configuration.KICKBAN_TIME) * 1000) >= now) {
				player.kickPlayer(Messenger.getConfigMessage(Localizer.getLocale(player), "kick.recur", (timeOfKickExpiry - now) / 1000
						+ ""));
				return;
			}
		}
		online_players.add(player.getName());

		/*
		 * Add the player to the dataset if they're not in it yet. If they weren't in it, put them at the spawn.
		 */
		if (playerdata == null && (Boolean) Configuration.getConfig(Configuration.DATASTORAGE)) {
			playerdata = PlayerData.createDataFor(player, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0L, new ArrayList<String>(),
					0, 20, "", 0, 0, 0, 0, 0, 0, 0, 0, false);
			putPlayerAtSpawn(player, false, clearInventory);
		}
		if (sql.isConnected() && !sql.isIn(player.getName())) {
			sql.add(player);
			putPlayerAtSpawn(player, false, clearInventory);
		}

		setThirst(player);

		if (playerdata != null && playerdata.getTimeOfKickban() != 0)
			playerdata.setTimeOfKickban(0L);
		if (sql.isConnected() && sql.getLong(player.getName(), "timeOfKickban") != 0)
			sql.set(player.getName(), "timeOfKickban", 0L, true);

		/*
		 * Cache all values asynchronously to reduce runtime lag.
		 */
		if (sql.isConnected())
			sql.createLinks(player.getName());

		/*
		 * Teleport the player back to the world spawn if they were killed by an NPC logout.
		 */
		if (playerdata != null && playerdata.wasKilledNPC() || sql.isConnected() && sql.getBoolean(player.getName(), "wasNPCKilled")) {
			Messenger.sendConfigMessage(player, "player_was_killed_npc");
			putPlayerAtSpawn(player, true, clearInventory);
		}

		if (MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled())
			if (playerdata != null && playerdata.isZombie() || MyZ.instance.getSQLManager().isConnected()
					&& MyZ.instance.getSQLManager().getBoolean(player.getName(), "isZombie"))
				DisguiseUtils.becomeZombie(player);
		if (MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled())
			if (playerdata != null && playerdata.isZombie() || MyZ.instance.getSQLManager().isConnected()
					&& MyZ.instance.getSQLManager().getBoolean(player.getName(), "isZombie"))
				LibsDisguiseUtils.becomeZombie(player);
	}

	/**
	 * Whether or not the player is currently playing MyZ.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is playing MyZ.
	 */
	public boolean isPlayer(Player player) {
		return online_players.contains(player.getName());
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
				sql.set(player.getName(), "isBleeding", false, true);
				sql.set(player.getName(), "isPoisoned", false, true);
				sql.set(player.getName(), "thirst", Configuration.getConfig(Configuration.THIRST_MAX), true);
				sql.set(player.getName(), "isZombie", false, true);
			}

			if (getServer().getPluginManager().getPlugin("DisguiseCraft") != null
					&& getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled())
				myz.utilities.DisguiseUtils.undisguise(player);
			if (getServer().getPluginManager().getPlugin("LibsDisguises") != null
					&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled())
				myz.utilities.LibsDisguiseUtils.undisguise(player);

			if ((Boolean) Configuration.getConfig(Configuration.KICKBAN) && wasDeath && !player.getName().equals("MrTeePee")) {
				if (data != null)
					data.setTimeOfKickban(System.currentTimeMillis());
				if (sql.isConnected())
					sql.set(player.getName(), "timeOfKickban", System.currentTimeMillis(), true);
			}

			if (!(Boolean) Configuration.getConfig(Configuration.SAVE_UNRANKED) && getRankFor(player) <= 0
					&& !player.getName().equals("MrTeePee")) {
				if (data != null) {
					for (String friend : data.getFriends())
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
					sql.set(player.getName(), "friends", "''", true);
					sql.set(player.getName(), "deaths", 0, true);
					sql.set(player.getName(), "giant_kills", 0, true);
					sql.set(player.getName(), "giant_kills_life", 0, true);
					sql.set(player.getName(), "giant_kills_life_record", 0, true);
					sql.set(player.getName(), "pigman_kills", 0, true);
					sql.set(player.getName(), "pigman_kills_life", 0, true);
					sql.set(player.getName(), "pigman_kills_life_record", 0, true);
					sql.set(player.getName(), "player_kills", 0, true);
					sql.set(player.getName(), "player_kills_life", 0, true);
					sql.set(player.getName(), "player_kills_life_record", 0, true);
					sql.set(player.getName(), "zombie_kills", 0, true);
					sql.set(player.getName(), "zombie_kills_life", 0, true);
					sql.set(player.getName(), "zombie_kills_life_record", 0, true);
					sql.set(player.getName(), "minutes_alive", 0L, true);
					sql.set(player.getName(), "minutes_alive_life", 0, true);
					sql.set(player.getName(), "minutes_alive_record", 0, true);
				}
			}
			online_players.remove(player.getName());
			return true;
		}
		return false;
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
				wasNPCKilled = sql.getBoolean(player.getName(), "wasNPCKilled");
				sql.set(player.getName(), "wasNPCKilled", false, true);
				sql.set(player.getName(), "deaths", sql.getInt(player.getName(), "deaths") + 1, true);
				sql.set(player.getName(), "player_kills_life", 0, true);
				sql.set(player.getName(), "zombie_kills_life", 0, true);
				sql.set(player.getName(), "pigman_kills_life", 0, true);
				sql.set(player.getName(), "giant_kills_life", 0, true);
				sql.set(player.getName(), "minutes_alive_life", 0, true);
			}
			/*
			 * Kick the player if kickban is enabled and log their time of kick.
			 */
			if ((Boolean) Configuration.getConfig(Configuration.KICKBAN) && !wasNPCKilled)
				if (data != null && data.getRank() <= 0 || sql.isConnected() && sql.getInt(player.getName(), "rank") <= 0
						&& !player.getName().equals("MrTeePee")) {
					flags.add(player.getName());
					player.kickPlayer(Messenger.getConfigMessage(Localizer.getLocale(player), "kick.come_back",
							Configuration.getConfig(Configuration.KICKBAN_TIME) + ""));
				}
		}
	}

	/**
	 * Get the list of NPCs on the server.
	 * 
	 * @return The list of NPCs.
	 */
	public List<CustomEntityPlayer> getNPCs() {
		return NPCs;
	}

	/**
	 * Get the list of flagged players, that is, players that have died and are
	 * about to be kicked. Ensures dead players don't spawn NPC's.
	 * 
	 * @return The list of players.
	 */
	public List<String> getFlagged() {
		return flags;
	}

	/**
	 * Remove all buffs for the given player and make everything right again.
	 * 
	 * @param player
	 *            The player.
	 */
	private void wipeBuffs(Player player, boolean clearInventory) {
		stopBleeding(player);
		stopPoison(player);
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
				if (sql.getInt(player.getName(), "rank") < (Integer) Configuration.getSpawn("spawn.numbered_requires_rank")) {
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
		Location spawn = new Location(world, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
		spawn.add(0.5, 0, 0.5);

		/*
		 * An enemy was nearby, stop the spawning.
		 */
		if (Utils.isPlayerNearby(player, spawn, (Integer) Configuration.getSpawn("spawn.safespawn_radius"))) {
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
						sql.set(player.getName(), "isZombie", true, true);
					return;
				} else if (random.nextInt(20) == 0 && getServer().getPluginManager().getPlugin("LibsDisguises") != null
						&& getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled()) {
					myz.utilities.LibsDisguiseUtils.becomeZombie(player);
					Messenger.sendConfigMessage(player, "spawn.zombie");
					if (data != null)
						data.setZombie(true);
					if (sql.isConnected())
						sql.set(player.getName(), "isZombie", true, true);
					return;
				}

			int rank = 0;
			if (data != null)
				rank = data.getRank();
			if (sql.isConnected())
				rank = sql.getInt(player.getName(), "rank");

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
					sql.set(player.getName(), "thirst", level, true);
				player.setLevel(level);
			}
		} else {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setThirst(level);
			if (sql.isConnected())
				sql.set(player.getName(), "thirst", level, true);
			player.setLevel(level);
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
			player.setLevel(sql.getInt(player.getName(), "thirst"));
	}

	/**
	 * Add a friend to the friender's friend list.
	 * 
	 * @param friender
	 *            The player that is friending the other.
	 * @param friended
	 *            The friended player.
	 */
	public void addFriend(Player friender, String friended) {
		PlayerData data = PlayerData.getDataFor(friender);
		if (data != null && !data.getFriends().contains(friended)) {
			data.addFriend(friended);
			friender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(friender), "friend.added", friended));
		}
		if (sql.isConnected() && !sql.getStringList(friender.getName(), "friends").contains(friended)) {
			String current = sql.getString(friender.getName(), "friends");
			sql.set(friender.getName(), "friends", current + (current.isEmpty() ? "" : ",") + friended, true);
			friender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(friender), "friend.added", friended));
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
	public void removeFriend(Player unfriender, String unfriended) {
		PlayerData data = PlayerData.getDataFor(unfriender);
		if (data != null && data.getFriends().contains(unfriended)) {
			data.removeFriend(unfriended);
			unfriender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(unfriender), "friend.removed", unfriended));
		}
		if (sql.isConnected() && sql.getStringList(unfriender.getName(), "friends").contains(unfriended)) {
			sql.set(unfriender.getName(), "friends", sql.getString(unfriender.getName(), "friends").replaceAll("," + unfriended, "")
					.replaceAll(unfriended + ",", ""), true);
			unfriender.sendMessage(Messenger.getConfigMessage(Localizer.getLocale(unfriender), "friend.removed", unfriended));
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
	 * @see isFriend(String player, String name)
	 */
	public boolean isFriend(Player player, String name) {
		return isFriend(player.getName(), name);
	}

	/**
	 * Whether or not the name provided is a friend of the given player.
	 * 
	 * @param player
	 *            The player name.
	 * @param name
	 *            The friend name to check.
	 * @return True if @param name is a friend of @param player.
	 */
	public boolean isFriend(String player, String name) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isFriend(name);
		if (sql.isConnected())
			return sql.getStringList(player, "friends").contains(name);
		// Theoretically impossible to get to this case.
		return false;
	}

	/**
	 * @see isHealer(Player player)
	 */
	public boolean isHealer(Player player) {
		return isHealer(player.getName());
	}

	/**
	 * Whether or not this player is a healer.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is a healer.
	 */
	public boolean isHealer(String player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isHealer();
		if (sql.isConnected())
			return sql.getInt(player, "heals_life") >= (Integer) Configuration.getConfig(Configuration.HEALER);
		// Theoretically impossible to get to this case.
		return false;
	}

	/**
	 * @see isBandit(Player player)
	 */
	public boolean isBandit(Player player) {
		return isBandit(player.getName());
	}

	/**
	 * Whether or not this player is a bandit.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player is a bandit.
	 */
	public boolean isBandit(String player) {
		PlayerData data = PlayerData.getDataFor(player);
		if (data != null)
			return data.isBandit();
		if (sql.isConnected())
			return sql.getInt(player, "player_kills_life") >= (Integer) Configuration.getConfig(Configuration.BANDIT);
		// Theoretically impossible to get to this case.
		return false;
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
			return sql.getInt(player.getName(), "rank");
		return 0;
	}
}
