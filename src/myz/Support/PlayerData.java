/**
 * 
 */
package myz.Support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import myz.MyZ;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class PlayerData {

	private final String name;
	private int player_kills, zombie_kills, pigman_kills, giant_kills, player_kills_life, zombie_kills_life, pigman_kills_life,
			giant_kills_life, player_kills_life_record, zombie_kills_life_record, pigman_kills_life_record, giant_kills_life_record, plays,
			deaths, rank, heals_life, thirst, minutes_alive_life, minutes_alive_life_record;
	private boolean isBleeding, isPoisoned, wasKilledNPC, autosave = true;
	private long timeOfKickban, minutes_alive;
	private String clan;
	private List<String> friends = new ArrayList<String>();

	private PlayerData(String name, int player_kills, int zombie_kills, int pigman_kills, int giant_kills, int player_kills_life,
			int zombie_kills_life, int pigman_kills_life, int giant_kills_life, int plays, int deaths, int rank, boolean isBleeding,
			boolean isPoisoned, boolean wasKilledNPC, long timeOfKickban, List<String> friends, int heals_life, int thirst, String clan,
			long minutes_alive, int minutes_alive_life, int minutes_alive_life_record, int player_kills_life_record,
			int zombie_kills_life_record, int pigman_kills_life_record, int giant_kills_life_record) {
		this.name = name;
		this.player_kills = player_kills;
		this.zombie_kills = zombie_kills;
		this.pigman_kills = pigman_kills;
		this.giant_kills = giant_kills;
		this.player_kills_life = player_kills_life;
		this.zombie_kills_life = zombie_kills_life;
		this.pigman_kills_life = pigman_kills_life;
		this.giant_kills_life = giant_kills_life;
		this.player_kills_life_record = player_kills_life_record;
		this.zombie_kills_life_record = zombie_kills_life_record;
		this.pigman_kills_life_record = pigman_kills_life_record;
		this.giant_kills_life_record = giant_kills_life_record;
		this.plays = plays;
		this.deaths = deaths;
		this.rank = rank;
		this.isBleeding = isBleeding;
		this.isPoisoned = isPoisoned;
		this.wasKilledNPC = wasKilledNPC;
		this.timeOfKickban = timeOfKickban;
		this.heals_life = heals_life;
		this.thirst = thirst;
		this.clan = clan;
		this.friends = new ArrayList<String>(friends);
		this.minutes_alive = minutes_alive;
		this.minutes_alive_life = minutes_alive_life;
		this.minutes_alive_life_record = minutes_alive_life_record;
	}

	/**
	 * @see getDataFor(String player)
	 */
	public static PlayerData getDataFor(Player player) {
		return getDataFor(player.getName());
	}

	/**
	 * The PlayerData stored for the given player by name.
	 * 
	 * @param player
	 *            The player's name in question.
	 * @return The PlayerData stored or null if no data is stored.
	 */
	public static PlayerData getDataFor(String player) {
		if (!Configuration.usePlayerData())
			return null;
		if (!playerDataExists(player))
			return null;
		ConfigurationSection section = MyZ.instance.getPlayerDataConfig().getConfigurationSection(player);
		return new PlayerData(player, section.getInt("player_kills"), section.getInt("zombie.kills"), section.getInt("pigman.kills"),
				section.getInt("giant.kills"), section.getInt("player.kills_life"), section.getInt("zombie.kills_life"),
				section.getInt("pigman.kills_life"), section.getInt("giant.kills_life"), section.getInt("plays"), section.getInt("deaths"),
				section.getInt("rank"), section.getBoolean("isBleeding"), section.getBoolean("isPoisoned"),
				section.getBoolean("wasKilledNPC"), section.getLong("timeOfKickban"), section.getStringList("friends"),
				section.getInt("heals_life"), section.getInt("thirst"), section.getString("clan"), section.getInt("minutes.played"),
				section.getInt("minutes.played_life"), section.getInt("minutes.played_life_record"),
				section.getInt("player.kills_life_record"), section.getInt("zombie.kills_life_record"),
				section.getInt("pigman.kills_life_record"), section.getInt("giant.kills_life_record"));
	}

	/**
	 * Create a data entry for the given player if one does not already exist.
	 * 
	 * @param player
	 *            The player.
	 * @param player_kills
	 *            How many player kills to begin with.
	 * @param zombie_kills
	 *            How many zombie kills to begin with.
	 * @param pigman_kills
	 *            How many pigman kills to begin with.
	 * @param giant_kills
	 *            How many giant kills to begin with.
	 * @param player_kills_life
	 *            How many player kills (life) to begin with.
	 * @param zombie_kills_life
	 *            How many zombie kills (life) to begin with.
	 * @param pigman_kills_life
	 *            How many pigman kills (life) to begin with.
	 * @param giant_kills_life
	 *            How many giant kills (life) to begin with.
	 * @param plays
	 *            How many times the player has played (default 1).
	 * @param deaths
	 *            How many times the player has died.
	 * @param rank
	 *            The rank (default 0).
	 * @param isBleeding
	 *            Whether or not the player is bleeding.
	 * @param isPoisoned
	 *            Whether or not the player is poisoned.
	 * @param wasKilledNPC
	 *            Whether or not the player's NPC was killed.
	 * @param friends
	 *            The player's friends.
	 * @param heals_life
	 *            The player's heals in the current life.
	 * @param thirst
	 *            The player's thirst level.
	 * @param clan
	 *            The player's clan or an empty string.
	 * @param minutes_alive
	 *            The number of minutes alive total.
	 * @param minutes_alive_life
	 *            The number of minutes alive this life.
	 * @param minutes_alive_life_record
	 *            The highest number of minutes survived in one life.
	 * @param player_kills_life_record
	 *            The highest number of players killed in one life.
	 * @param zombie_kills_life_record
	 *            The highest number of zombies killed in one life.
	 * @param pigman_kills_life_record
	 *            The highest number of pigmen killed in one life.
	 * @param giant_kills_life_record
	 *            The highest number of giants killed in one life.
	 * @return The PlayerData object created.
	 */
	public static PlayerData createDataFor(Player player, int player_kills, int zombie_kills, int pigman_kills, int giant_kills,
			int player_kills_life, int zombie_kills_life, int pigman_kills_life, int giant_kills_life, int plays, int deaths, int rank,
			boolean isBleeding, boolean isPoisoned, boolean wasKilledNPC, long timeOfKickban, List<String> friends, int heals_life,
			int thirst, String clan, long minutes_alive, int minutes_alive_life, int minutes_alive_life_record,
			int player_kills_life_record, int zombie_kills_life_record, int pigman_kills_life_record, int giant_kills_life_record) {
		if (!Configuration.usePlayerData())
			return null;
		if (!playerDataExists(player)) {
			ConfigurationSection section = MyZ.instance.getPlayerDataConfig().createSection(player.getName());
			section.set("player.kills", player_kills);
			section.set("zombie.kills", zombie_kills);
			section.set("pigman.kills", pigman_kills);
			section.set("giant.kills", giant_kills);
			section.set("player.kills_life", player_kills_life);
			section.set("zombie.kills_life", zombie_kills_life);
			section.set("pigman.kills_life", pigman_kills_life);
			section.set("giant.kills_life", giant_kills_life);
			section.set("player.kills_life_record", player_kills_life_record);
			section.set("zombie.kills_life_record", zombie_kills_life_record);
			section.set("pigman.kills_life_record", pigman_kills_life_record);
			section.set("giant.kills_life_record", giant_kills_life_record);
			section.set("plays", plays);
			section.set("deaths", deaths);
			section.set("rank", rank);
			section.set("isBleeding", isBleeding);
			section.set("isPoisoned", isPoisoned);
			section.set("wasKilledNPC", wasKilledNPC);
			section.set("timeOfKickban", timeOfKickban);
			section.set("friends", friends);
			section.set("heals_life", heals_life);
			section.set("thirst", thirst);
			section.set("clan", clan);
			section.set("minutes.played", minutes_alive);
			section.set("minutes.played_life", minutes_alive_life);
			section.set("minutes.played_life_record", minutes_alive_life_record);
			try {
				MyZ.instance.getPlayerDataConfig().save(new File(MyZ.instance.getDataFolder() + File.separator + "playerdata.yml"));
			} catch (IOException e) {
				MyZ.instance.getLogger().warning("Unable to save a new PlayerData for " + player.getName() + ": " + e.getMessage());
			}
		}
		return getDataFor(player);
	}

	/**
	 * @see playerDataExists(String player)
	 */
	private static boolean playerDataExists(Player player) {
		return playerDataExists(player.getName());
	}

	/**
	 * Whether or not the data entry exists for the given player.
	 * 
	 * @param player
	 *            The player in question.
	 * @return True if the entry exists, false otherwise.
	 */
	private static boolean playerDataExists(String player) {
		if (!Configuration.usePlayerData())
			return false;
		FileConfiguration config = MyZ.instance.getPlayerDataConfig();
		return config != null && config.contains(player);
	}

	/**
	 * @see save(boolean bypass_autosave)
	 */
	private void save() {
		save(false);
	}

	/**
	 * Save the PlayerData object into the YAML. Only continues if
	 * playerDataExists() resolves to true.
	 * 
	 * @param bypass_autosave
	 *            Whether or not to bypass the value of autosave.
	 */
	public void save(boolean bypass_autosave) {
		if (!Configuration.usePlayerData())
			return;
		if (bypass_autosave || autosave)
			if (playerDataExists(name)) {
				ConfigurationSection section = MyZ.instance.getPlayerDataConfig().getConfigurationSection(name);
				section.set("player.kills", player_kills);
				section.set("zombie.kills", zombie_kills);
				section.set("pigman.kills", pigman_kills);
				section.set("giant.kills", giant_kills);
				section.set("player.kills_life", player_kills_life);
				section.set("zombie.kills_life", zombie_kills_life);
				section.set("pigman.kills_life", pigman_kills_life);
				section.set("giant.kills_life", giant_kills_life);
				section.set("player.kills_life_record", player_kills_life_record);
				section.set("zombie.kills_life_record", zombie_kills_life_record);
				section.set("pigman.kills_life_record", pigman_kills_life_record);
				section.set("giant.kills_life_record", giant_kills_life_record);
				section.set("plays", plays);
				section.set("deaths", deaths);
				section.set("rank", rank);
				section.set("isBleeding", isBleeding);
				section.set("isPoisoned", isPoisoned);
				section.set("wasKilledNPC", wasKilledNPC);
				section.set("timeOfKickban", timeOfKickban);
				section.set("friends", friends);
				section.set("heals_life", heals_life);
				section.set("thirst", thirst);
				section.set("clan", clan);
				section.set("minutes.played", minutes_alive);
				section.set("minutes.played_life", minutes_alive_life);
				section.set("minutes.played_life_record", minutes_alive_life_record);
				try {
					MyZ.instance.getPlayerDataConfig().save(new File(MyZ.instance.getDataFolder() + File.separator + "playerdata.yml"));
				} catch (IOException e) {
					MyZ.instance.getLogger().warning("Unable to save a PlayerData for " + name + ": " + e.getMessage());
				}
			}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the clan
	 */
	public String getClan() {
		return clan;
	}

	/**
	 * @return True if this player is in a clan, false otherwise.
	 */
	public boolean inClan() {
		return clan != null && !clan.isEmpty();
	}

	/**
	 * @return The number of players in the same clan as this player.
	 */
	public int getNumberInClan() {
		if (!inClan()) { return 0; }
		List<String> playersInClan = new ArrayList<String>();
		playersInClan.add(name);
		PlayerData data;
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (playersInClan.contains(player.getName())) {
				continue;
			}
			data = getDataFor(player.getName());
			if (data != null) {
				if (data.inClan() && data.getClan().equals(getClan())) {
					playersInClan.add(player.getName());
				}
			}
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (playersInClan.contains(player.getName())) {
				continue;
			}
			data = getDataFor(player.getName());
			if (data != null) {
				if (data.inClan() && data.getClan().equals(getClan())) {
					playersInClan.add(player.getName());
				}
			}
		}
		return playersInClan.size();
	}

	/**
	 * @return All the online players in the same clan as this player.
	 */
	public List<Player> getOnlinePlayersInClan() {
		List<Player> playersInClan = new ArrayList<Player>();
		if (!inClan()) { return playersInClan; }
		playersInClan.add(Bukkit.getPlayerExact(name));
		PlayerData data;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (playersInClan.contains(player.getName())) {
				continue;
			}
			data = getDataFor(player.getName());
			if (data != null) {
				if (data.inClan() && data.getClan().equals(getClan())) {
					playersInClan.add(player);
				}
			}
		}
		return playersInClan;
	}

	/**
	 * @return the player_kills
	 */
	public int getPlayerKills() {
		return player_kills;
	}

	/**
	 * @param player_kills
	 *            the player_kills to set
	 */
	public void setPlayerKills(int player_kills) {
		this.player_kills = player_kills;
		save();
	}

	/**
	 * @return the zombie_kills
	 */
	public int getZombieKills() {
		return zombie_kills;
	}

	/**
	 * @param zombie_kills
	 *            the zombie_kills to set
	 */
	public void setZombieKills(int zombie_kills) {
		this.zombie_kills = zombie_kills;
		save();
	}

	/**
	 * @return the pigman_kills
	 */
	public int getPigmanKills() {
		return pigman_kills;
	}

	/**
	 * @param pigman_kills
	 *            the pigman_kills to set
	 */
	public void setPigmanKills(int pigman_kills) {
		this.pigman_kills = pigman_kills;
		save();
	}

	/**
	 * @return the giant_kills
	 */
	public int getGiantKills() {
		return giant_kills;
	}

	/**
	 * @param giant_kills
	 *            the giant_kills to set
	 */
	public void setGiantKills(int giant_kills) {
		this.giant_kills = giant_kills;
		save();
	}

	/**
	 * @return the plays
	 */
	public int getPlays() {
		return plays;
	}

	/**
	 * @param plays
	 *            the plays to set
	 */
	public void setPlays(int plays) {
		this.plays = plays;
		save();
	}

	/**
	 * @return the deaths
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * @param deaths
	 *            the deaths to set
	 */
	public void setDeaths(int deaths) {
		this.deaths = deaths;
		save();
	}

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
		save();
	}

	/**
	 * @return the isBleeding
	 */
	public boolean isBleeding() {
		return isBleeding;
	}

	/**
	 * @param isBleeding
	 *            the isBleeding to set
	 */
	public void setBleeding(boolean isBleeding) {
		this.isBleeding = isBleeding;
		save();
	}

	/**
	 * @return the isPoisoned
	 */
	public boolean isPoisoned() {
		return isPoisoned;
	}

	/**
	 * @param isPoisoned
	 *            the isPoisoned to set
	 */
	public void setPoisoned(boolean isPoisoned) {
		this.isPoisoned = isPoisoned;
		save();
	}

	/**
	 * @return the wasKilledNPC
	 */
	public boolean wasKilledNPC() {
		return wasKilledNPC;
	}

	/**
	 * @param wasKilledNPC
	 *            the wasKilledNPC to set
	 */
	public void setWasKilledNPC(boolean wasKilledNPC) {
		isPoisoned = wasKilledNPC;
		save();
	}

	/**
	 * @return the timeOfKickban
	 */
	public long getTimeOfKickban() {
		return timeOfKickban;
	}

	/**
	 * @param timeOfKickban
	 *            the timeOfKickban to set
	 */
	public void setTimeOfKickban(long timeOfKickban) {
		this.timeOfKickban = timeOfKickban;
		save();
	}

	/**
	 * @return The player's friends.
	 */
	public List<String> getFriends() {
		return new ArrayList<String>(friends);
	}

	/**
	 * Whether or not the given player is a friend of the player.
	 * 
	 * @see isFriend(String name)
	 * 
	 * @param player
	 *            The player.
	 * @return True if the friends list contains the player's name.
	 */
	public boolean isFriend(Player player) {
		return isFriend(player.getName());
	}

	/**
	 * Whether or not the given player is a friend of our player.
	 * 
	 * @param name
	 * @return True if friends contains @param name.
	 */
	public boolean isFriend(String name) {
		return friends.contains(name);
	}

	/**
	 * Add a friend to the friend list.
	 * 
	 * @param name
	 *            The friend's name.
	 */
	public void addFriend(String name) {
		friends.add(name);
		save();
	}

	/**
	 * Remove a friend from the friend list.
	 * 
	 * @param name
	 *            The friend's name.
	 */
	public void removeFriend(String name) {
		friends.remove(name);
		save();
	}

	/**
	 * @return the player_kills_life
	 */
	public int getPlayerKillsLife() {
		return player_kills_life;
	}

	/**
	 * @param player_kills_life
	 *            the player_kills_life to set
	 */
	public void setPlayerKillsLife(int player_kills_life) {
		this.player_kills_life = player_kills_life;
		save();
	}

	/**
	 * @return the zombie_kills_life
	 */
	public int getZombieKillsLife() {
		return zombie_kills_life;
	}

	/**
	 * @param thirst
	 *            the thirst to set
	 */
	public void setThirst(int thirst) {
		this.thirst = thirst;
		save();
	}

	/**
	 * @return the thirst
	 */
	public int getThirst() {
		return thirst;
	}

	/**
	 * @param zombie_kills_life
	 *            the zombie_kills_life to set
	 */
	public void setZombieKillsLife(int zombie_kills_life) {
		this.zombie_kills_life = zombie_kills_life;
		save();
	}

	/**
	 * @return the player_kills_life_record
	 */
	public int getPlayerKillsLifeRecord() {
		return player_kills_life_record;
	}

	/**
	 * @param player_kills_life_record
	 *            the player_kills_life_record to set
	 */
	public void setPlayerKillsLifeRecord(int player_kills_life_record) {
		this.player_kills_life_record = player_kills_life_record;
		save();
	}

	/**
	 * @return the zombie_kills_life_record
	 */
	public int getZombieKillsLifeRecord() {
		return zombie_kills_life_record;
	}

	/**
	 * @param zombie_kills_life_record
	 *            the zombie_kills_life_record to set
	 */
	public void setZombieKillsLifeRecord(int zombie_kills_life_record) {
		this.zombie_kills_life_record = zombie_kills_life_record;
		save();
	}

	/**
	 * @return the pigman_kills_life_record
	 */
	public int getPigmanKillsLifeRecord() {
		return pigman_kills_life_record;
	}

	/**
	 * @param pigman_kills_life_record
	 *            the pigman_kills_life_record to set
	 */
	public void setPigmanKillsLifeRecord(int pigman_kills_life_record) {
		this.pigman_kills_life_record = pigman_kills_life_record;
		save();
	}

	/**
	 * @return the giant_kills_life_record
	 */
	public int getGiantKillsLifeRecord() {
		return giant_kills_life_record;
	}

	/**
	 * @param giant_kills_life_record
	 *            the pigman_kills_life_record to set
	 */
	public void setGiantKillsLifeRecord(int giant_kills_life_record) {
		this.giant_kills_life_record = giant_kills_life_record;
		save();
	}

	/**
	 * @return the pigman_kills_life
	 */
	public int getPigmanKillsLife() {
		return pigman_kills_life;
	}

	/**
	 * @param pigman_kills_life
	 *            the pigman_kills_life to set
	 */
	public void setPigmanKillsLife(int pigman_kills_life) {
		this.pigman_kills_life = pigman_kills_life;
		save();
	}

	/**
	 * @return the giant_kills_life
	 */
	public int getGiantKillsLife() {
		return giant_kills_life;
	}

	/**
	 * @param giant_kills_life
	 *            the giant_kills_life to set
	 */
	public void setGiantKillsLife(int giant_kills_life) {
		this.giant_kills_life = giant_kills_life;
		save();
	}

	/**
	 * Toggle the autosaving of the playerdata file when this PlayerData is
	 * changed.
	 * 
	 * @param state
	 *            The state to set autosave to.
	 * @param save_immediately
	 *            Whether or not to save when this command is called.
	 */
	public void setAutosave(boolean state, boolean save_immediately) {
		autosave = state;
		if (save_immediately)
			save(true);
	}

	/**
	 * @return the heals_life
	 */
	public int getHealsLife() {
		return heals_life;
	}

	/**
	 * @param heals_life
	 *            the heals_life to set
	 */
	public void setHealsLife(int heals_life) {
		this.heals_life = heals_life;
		save();
	}

	/**
	 * @return the minutes_alive
	 */
	public long getMinutesAlive() {
		return minutes_alive;
	}

	/**
	 * @param minutes_alive
	 *            the minutes_alive to set.
	 */
	public void setMinutesAlive(long minutes_alive) {
		this.minutes_alive = minutes_alive;
		save();
	}

	/**
	 * @return the minutes_alive_life
	 */
	public int getMinutesAliveLife() {
		return minutes_alive_life;
	}

	/**
	 * @param minutes_alive_life
	 *            the minutes_alive_life to set.
	 */
	public void setMinutesAliveLife(int minutes_alive_life) {
		this.minutes_alive_life = minutes_alive_life;
		save();
	}

	/**
	 * @return the minutes_alive_life_record
	 */
	public int getMinutesAliveLifeRecord() {
		return minutes_alive_life_record;
	}

	/**
	 * @param minutes_alive_life_record
	 *            the minutes_alive_life_record to set.
	 */
	public void setMinutesAliveLifeRecord(int minutes_alive_life_record) {
		this.minutes_alive_life_record = minutes_alive_life_record;
		save();
	}

	/**
	 * @param clan
	 *            the clan to set
	 */
	public void setClan(String clan) {
		this.clan = clan;
		save();
	}

	/**
	 * Whether or not this player is a bandit.
	 * 
	 * @return True if the player is a bandit, false otherwise.
	 */
	public boolean isBandit() {
		return player_kills_life >= Configuration.getBanditKills();
	}

	/**
	 * Whether or not this player is a healer.
	 * 
	 * @return True if the player is a healer, false otherwise.
	 */
	public boolean isHealer() {
		return player_kills_life >= Configuration.getHealerHeals();
	}
}
