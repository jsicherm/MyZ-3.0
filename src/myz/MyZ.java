/**
 * 
 */
package myz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import myz.API.PlayerBeginBleedingEvent;
import myz.API.PlayerBeginPoisonEvent;
import myz.API.PlayerSpawnInWorldEvent;
import myz.API.PlayerWaterDecayEvent;
import myz.Commands.AddSpawnCommand;
import myz.Commands.FriendCommand;
import myz.Commands.FriendsCommand;
import myz.Commands.RemoveSpawnCommand;
import myz.Commands.SaveKitCommand;
import myz.Commands.SaveRankCommand;
import myz.Commands.SetLobbyCommand;
import myz.Commands.SpawnCommand;
import myz.Commands.SpawnsCommand;
import myz.Listeners.AutoFriend;
import myz.Listeners.BandageSelf;
import myz.Listeners.CancelPlayerEvents;
import myz.Listeners.CancelZombieDamage;
import myz.Listeners.Chat;
import myz.Listeners.ConsumeFood;
import myz.Listeners.EntityHurtPlayer;
import myz.Listeners.EntitySpawn;
import myz.Listeners.JoinQuit;
import myz.Listeners.Movement;
import myz.Listeners.PlayerDeath;
import myz.Listeners.PlayerHurtEntity;
import myz.Listeners.PlayerKillEntity;
import myz.Listeners.PlayerSummonGiant;
import myz.Listeners.ThrowProjectile;
import myz.Scheduling.Sync;
import myz.Scheduling.aSync;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.Support.Teleport;
import myz.Utilities.SQLManager;
import myz.Utilities.Utilities;
import myz.Utilities.WorldlessLocation;
import myz.mobs.CustomEntityPlayer;
import myz.mobs.CustomEntityType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

/**
 * @author Jordan
 * 
 *         The main plugin and the interface for most convenience methods.
 */
public class MyZ extends JavaPlugin {

	// TODO giants
	// TODO clans
	// TODO track stats
	// TODO block/entity protection
	// TODO sound attraction

	public static MyZ instance;
	private List<String> online_players = new ArrayList<String>();
	private FileConfiguration playerdata;
	private SQLManager sql;
	private static final Random random = new Random();
	private List<CustomEntityPlayer> NPCs = new ArrayList<CustomEntityPlayer>();
	private List<String> flags = new ArrayList<String>();

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		loadPlayerData();
		Configuration.reload();

		sql = new SQLManager(Configuration.getHost(), Configuration.getPort(), Configuration.getDatabase(), Configuration.getUser(),
				Configuration.getPassword());
		if (!sql.isConnected() && !Configuration.usePlayerData()) {
			Messenger.sendConsoleMessage(ChatColor.RED
					+ "MySQL is not connected and PlayerData is disabled. Enabling PlayerData for this session.");
			Configuration.togglePlayerDataTemporarily(true);
		} else if (sql.isConnected() && Configuration.usePlayerData()) {
			Messenger.sendConsoleMessage(ChatColor.RED + "MySQL and PlayerData are enabled. Disabling PlayerData for this session.");
			Configuration.togglePlayerDataTemporarily(false);
		}

		/*
		 * Add all players that weren't already in the playerdata to it (in case of a reload).
		 */
		for (Player player : Bukkit.getOnlinePlayers()) {
			addPlayer(player);
			PlayerData data = null;
			if ((data = PlayerData.getDataFor(player)) == null || sql.isConnected() && !sql.isIn(player.getName())) {
				if (data == null && Configuration.usePlayerData()) {
					PlayerData.createDataFor(player, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0L, new ArrayList<String>(), 0,
							Configuration.getMaxThirstLevel());
					putPlayerAtSpawn(player, false);
				}
				if (sql.isConnected() && !sql.isIn(player.getName())) {
					sql.add(player);
					putPlayerAtSpawn(player, false);
				}
			}
		}

		/*
		 * Register threads.
		 */
		getServer().getScheduler().runTaskTimerAsynchronously(this, new aSync(), 20L, 20L);
		getServer().getScheduler().runTaskTimer(this, new Sync(), 20L, 20L);

		/*
		 * Register all listeners.
		 */
		PluginManager p = getServer().getPluginManager();
		p.registerEvents(new JoinQuit(), this);
		p.registerEvents(new AutoFriend(), this);
		p.registerEvents(new BandageSelf(), this);
		p.registerEvents(new CancelZombieDamage(), this);
		p.registerEvents(new ConsumeFood(), this);
		p.registerEvents(new PlayerHurtEntity(), this);
		p.registerEvents(new ThrowProjectile(), this);
		p.registerEvents(new PlayerKillEntity(), this);
		p.registerEvents(new EntityHurtPlayer(), this);
		p.registerEvents(new PlayerDeath(), this);
		p.registerEvents(new EntitySpawn(), this);
		p.registerEvents(new PlayerSummonGiant(), this);
		p.registerEvents(new Chat(), this);
		p.registerEvents(new CancelPlayerEvents(), this);
		p.registerEvents(new Movement(), this);

		/*
		 * Register all commands.
		 */
		getCommand("friend").setExecutor(new FriendCommand());
		getCommand("friends").setExecutor(new FriendsCommand());
		getCommand("start").setExecutor(new SpawnCommand());
		getCommand("setlobby").setExecutor(new SetLobbyCommand());
		getCommand("addspawn").setExecutor(new AddSpawnCommand());
		getCommand("removespawn").setExecutor(new RemoveSpawnCommand());
		getCommand("spawnpoints").setExecutor(new SpawnsCommand());
		getCommand("savekit").setExecutor(new SaveKitCommand());
		getCommand("saverank").setExecutor(new SaveRankCommand());

		/*
		 * Register our custom mobs.
		 */
		CustomEntityType.registerEntities();
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);

		// Remove all entities in all worlds as reloads will cause classloader
		// issues to
		// do with overriding the pathfinding and entity.
		for (CustomEntityPlayer player : NPCs) {
			player.getBukkitEntity().remove();
		}
		NPCs.clear();
		for (World world : getServer().getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (entity instanceof LivingEntity
						&& (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.GIANT
								|| entity.getType() == EntityType.HORSE || entity.getType() == EntityType.PIG_ZOMBIE)) {
					entity.remove();
				}
			}
		}
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
			saveResource("playerdata.yml", true);
		playerdata = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "playerdata.yml"));
	}

	/**
	 * Get the playerdata YAML.
	 * 
	 * @return The FileConfiguration for the playerdata.yml or null if not
	 *         loaded.
	 */
	public FileConfiguration getPlayerDataConfig() {
		return playerdata;
	}

	/**
	 * Ensure the playerdata YAML is loaded.
	 * 
	 * @return True if the playerdata.yml is non-null.
	 */
	public boolean hasInitializedConfig() {
		return playerdata != null;
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
		return data != null && data.isBleeding() || sql.isConnected() && sql.getBoolean(player.getName(), "isBleeding");
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
		return data != null && data.isPoisoned() || sql.isConnected() && sql.getBoolean(player.getName(), "isPoisoned");
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
		PlayerBeginBleedingEvent event = new PlayerBeginBleedingEvent(player);
		getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			if (!isBleeding(player)) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null) {
					data.setBleeding(true);
					Messenger.sendConfigMessage(player, "damage.bleed_begin");
				}
				if (sql.isConnected())
					sql.set(player.getName(), "isBleeding", true, true);
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
		PlayerBeginPoisonEvent event = new PlayerBeginPoisonEvent(player);
		getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled())
			if (!isPoisoned(player)) {
				PlayerData data = PlayerData.getDataFor(player);
				if (data != null) {
					data.setPoisoned(true);
					Messenger.sendConfigMessage(player, "damage.poison_begin");
				}
				if (sql.isConnected())
					sql.set(player.getName(), "isPoisoned", true, true);
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
			if (data != null) {
				data.setPoisoned(false);
				Messenger.sendConfigMessage(player, "damage.poison_end");
			}
			if (sql.isConnected())
				sql.set(player.getName(), "isPoisoned", false, true);
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
			if (data != null) {
				data.setBleeding(false);
				Messenger.sendConfigMessage(player, "damage.bleed_end");
			}
			if (sql.isConnected())
				sql.set(player.getName(), "isBleeding", false, true);
		}
	}

	/**
	 * Add a player to the MyZ game. Also checks for kickban if the useprelogin
	 * configuration option is disabled.
	 * 
	 * @param player
	 *            The player.
	 */
	public void addPlayer(Player player) {
		PlayerData playerdata = PlayerData.getDataFor(player.getName());

		if (!Configuration.usePreLogin()) {
			/*
			 * Check if the player is still banned against the playerdata and sql.
			 */
			long now = System.currentTimeMillis();
			long timeOfKickExpiry;
			if (playerdata != null
					&& (timeOfKickExpiry = playerdata.getTimeOfKickban() + Configuration.getKickBanSeconds() * 1000) >= now
					|| MyZ.instance.getSQLManager().isConnected()
					&& (timeOfKickExpiry = MyZ.instance.getSQLManager().getLong(player.getName(), "timeOfKickban")
							+ Configuration.getKickBanSeconds() * 1000) >= now) {
				player.kickPlayer(Messenger.getConfigMessage("kick.recur", (timeOfKickExpiry - now) / 1000));
				return;
			}
		}
		online_players.add(player.getName());

		/*
		 * Add the player to the dataset if they're not in it yet. If they weren't in it, put them at the spawn.
		 */
		if (playerdata == null && Configuration.usePlayerData()) {
			playerdata = PlayerData.createDataFor(player, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0L,
					new ArrayList<String>(), 0, 20);
			putPlayerAtSpawn(player, false);
		}
		if (sql.isConnected() && !sql.isIn(player.getName())) {
			sql.add(player);
			putPlayerAtSpawn(player, false);
		}

		setThirst(player);

		if (playerdata != null && playerdata.getTimeOfKickban() != 0)
			playerdata.setTimeOfKickban(0L);
		if (sql.isConnected() && sql.getLong(player.getName(), "timeOfKickban") != 0)
			sql.set(player.getName(), "timeOfKickban", 0L, true);

		/*
		 * Teleport the player back to the world spawn if they were killed by an NPC logout.
		 */
		if (playerdata != null && playerdata.wasKilledNPC() || sql.isConnected() && sql.getBoolean(player.getName(), "wasNPCKilled")) {
			Messenger.sendConfigMessage(player, "player_was_killed_npc");
			putPlayerAtSpawn(player, true);
		}
	}

	/**
	 * Remove a player from the MyZ game. Will fail if the player is not in the
	 * game.
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player was removed, false otherwise.
	 */
	public boolean removePlayer(Player player) {
		if (online_players.contains(player.getName())) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null) {
				data.setBleeding(false);
				data.setPoisoned(false);
				data.setThirst(20);
			}
			if (sql.isConnected()) {
				sql.set(player.getName(), "isBleeding", false, true);
				sql.set(player.getName(), "isPoisoned", false, true);
				sql.set(player.getName(), "thirst", 20, true);
			}

			if (Configuration.isKickBan()) {
				if (data != null)
					data.setTimeOfKickban(System.currentTimeMillis());
				if (sql.isConnected())
					sql.set(player.getName(), "timeOfKickban", System.currentTimeMillis(), true);
			}

			if (!Configuration.saveDataOfUnrankedPlayers() && getRankFor(player) <= 0) {
				if (data != null) {
					for (String friend : data.getFriends())
						data.removeFriend(friend);
					data.setAutosave(false, false);
					data.setDeaths(0);
					data.setGiantKills(0);
					data.setGiantKillsLife(0);
					data.setPigmanKills(0);
					data.setPigmanKillsLife(0);
					data.setPlayerKills(0);
					data.setPlayerKillsLife(0);
					data.setPlays(0);
					data.setZombieKills(0);
					data.setZombieKillsLife(0);
					data.setAutosave(true, true);
				}
				if (sql.isConnected()) {
					sql.set(player.getName(), "friends", "''", true);
					sql.set(player.getName(), "deaths", 0, true);
					sql.set(player.getName(), "giant_kills", 0, true);
					sql.set(player.getName(), "giant_kills_life", 0, true);
					sql.set(player.getName(), "pigman_kills", 0, true);
					sql.set(player.getName(), "pigman_kills_life", 0, true);
					sql.set(player.getName(), "player_kills", 0, true);
					sql.set(player.getName(), "player_kills_life", 0, true);
					sql.set(player.getName(), "plays", 0, true);
					sql.set(player.getName(), "zombie_kills", 0, true);
					sql.set(player.getName(), "zombie_kills_life", 0, true);
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
	public void putPlayerAtSpawn(Player player, boolean wasDeath) {
		Teleport.teleport(player, player.getWorld().getSpawnLocation(), false);
		if (getServer().getPluginManager().isPluginEnabled("essentials"))
			Bukkit.dispatchCommand(player, "spawn");
		wipeBuffs(player);

		if (wasDeath) {
			PlayerData data = PlayerData.getDataFor(player);
			if (data != null)
				data.setDeaths(data.getDeaths() + 1);
			if (sql.isConnected())
				sql.set(player.getName(), "deaths", sql.getInt(player.getName(), "deaths") + 1, true);

			boolean wasNPCKilled = false;
			if (data != null) {
				wasNPCKilled = data.wasKilledNPC();
			}
			if (sql.isConnected()) {
				wasNPCKilled = sql.getBoolean(player.getName(), "wasNPCKilled");
			}
			/*
			 * Kick the player if kickban is enabled and log their time of kick.
			 */
			if (Configuration.isKickBan() && !wasNPCKilled)
				if ((data != null && data.getRank() <= 0) || (sql.isConnected() && sql.getInt(player.getName(), "rank") <= 0)) {
					removePlayer(player);
					if (data != null)
						data.setTimeOfKickban(System.currentTimeMillis());
					if (sql.isConnected())
						sql.set(player.getName(), "timeOfKickban", System.currentTimeMillis(), true);
					flags.add(player.getName());
					player.kickPlayer(Messenger.getConfigMessage("kick.come_back", Configuration.getKickBanSeconds()));
				}
			if (data != null) {
				data.setWasKilledNPC(false);
			}
			if (sql.isConnected()) {
				sql.set(player.getName(), "wasNPCKilled", false, true);
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
	private void wipeBuffs(Player player) {
		stopBleeding(player);
		stopPoison(player);
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
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
		if (Configuration.numberedSpawnRequiresRank() && withInitiallySpecifiedSpawnpoint) {
			if (data != null)
				if (data.getRank() == 0) {
					Messenger.sendConfigMessage(player, "command.spawn.requires_rank");
					return;
				}
			if (sql.isConnected())
				if (sql.getInt(player.getName(), "rank") == 0) {
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
		if (Utilities.isPlayerNearby(player, spawn, Configuration.getSafeSpawnRadius())) {
			if (withInitiallySpecifiedSpawnpoint || spawningAttempts >= 25)
				Messenger.sendConfigMessage(player, "command.spawn.unable_to_spawn");
			else
				spawnPlayer(player, spawningAttempts + 1);
			return;
		}

		PlayerSpawnInWorldEvent event = new PlayerSpawnInWorldEvent(player);
		getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			wipeBuffs(player);
			setThirst(player, Configuration.getMaxThirstLevel());

			Teleport.teleport(player, spawn, false);

			for (PotionEffect potioneffect : Configuration.getSpawnPotionEffects())
				player.addPotionEffect(potioneffect);

			int rank = 0;
			if (data != null)
				rank = data.getRank();
			if (sql.isConnected())
				rank = sql.getInt(player.getName(), "rank");

			try {
				player.getInventory().setArmorContents(Configuration.getArmorContents(rank));
			} catch (NullPointerException exc) {

			}
			try {
				player.getInventory().setContents(Configuration.getInventory(rank));
			} catch (NullPointerException exc) {

			}
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
		if (level != Configuration.getMaxThirstLevel()) {
			PlayerWaterDecayEvent event = new PlayerWaterDecayEvent(player);
			getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
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
			friender.sendMessage(Messenger.getConfigMessage("friend.added", friended));
		}
		if (sql.isConnected() && !sql.getStringList(friender.getName(), "friends").contains(friended)) {
			String current = sql.getString(friender.getName(), "friends");
			sql.set(friender.getName(), "friends", current + (current.isEmpty() ? "" : ",") + friended, true);
			friender.sendMessage(Messenger.getConfigMessage("friend.added", friended));
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
			unfriender.sendMessage(Messenger.getConfigMessage("friend.removed", unfriended));
		}
		if (sql.isConnected() && sql.getStringList(unfriender.getName(), "friends").contains(unfriended)) {
			sql.set(unfriender.getName(), "friends", sql.getString(unfriender.getName(), "friends").replaceAll("," + unfriended, "")
					.replaceAll(unfriended + ",", ""), true);
			unfriender.sendMessage(Messenger.getConfigMessage("friend.removed", unfriended));
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
		return isFriend(player, name);
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
			return sql.getInt(player, "heals_life") >= Configuration.getHealerHeals();
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
			return sql.getInt(player, "player_kills_life") >= Configuration.getBanditKills();
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
		if (sql.isConnected()) { return sql.getInt(player.getName(), "rank"); }
		return 0;
	}
}
