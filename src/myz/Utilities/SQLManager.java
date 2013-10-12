package myz.Utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SQLManager {

	private Connection sql;
	public final String hostname, database, username, password;
	public final int port;
	private boolean connected;

	/**
	 * A simple MySQL tool for ease of access.
	 * 
	 * @param hostname
	 *            Host
	 * @param port
	 *            Port
	 * @param database
	 *            Database
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 */
	public SQLManager(String hostname, int port, String database, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		Messenger.sendConsoleMessage(ChatColor.YELLOW + "Connecting to MySQL...");
		connect();
	}

	public void executeQuery(String query) throws SQLException {
		sql.createStatement().executeUpdate(query);
	}

	/**
	 * Establish connection with MySQL.
	 */
	public void connect() {
		String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database;

		// Attempt to connect
		try {
			// Connection succeeded
			sql = DriverManager.getConnection(url, username, password);
			connected = true;
			Messenger.sendConsoleMessage(ChatColor.GREEN + "Connection successful.");
			setup();
		} catch (Exception e) {
			// Couldn't connect to the database
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to connect.");
		}
	}

	/**
	 * Disconnect from MySQL.
	 */
	public void disconnect() {
		if (sql != null && connected) {
			Messenger.sendConsoleMessage(ChatColor.YELLOW + "Disconnected from MySQL.");
			try {
				sql.close();
			} catch (SQLException e) {
				Messenger.sendConsoleMessage(ChatColor.RED + "Unable to close MySQL connection: " + e.getMessage());
			}
		}
	}

	/**
	 * @return true if connected to MySQL.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Create tables.
	 */
	public void setup() {
		if (!isConnected())
			return;
		try {
			executeQuery("CREATE TABLE IF NOT EXISTS playerdata (username VARCHAR(17) PRIMARY KEY, player_kills SMALLINT UNSIGNED, zombie_kills SMALLINT UNSIGNED, pigman_kills SMALLINT UNSIGNED, giant_kills SMALLINT UNSIGNED, player_kills_life SMALLINT UNSIGNED, zombie_kills_life SMALLINT UNSIGNED, pigman_kills_life SMALLINT UNSIGNED, giant_kills_life SMALLINT UNSIGNED, plays SMALLINT UNSIGNED, deaths SMALLINT UNSIGNED, rank SMALLINT UNSIGNED, isBleeding TINYINT(1), isPoisoned TINYINT(1), wasNPCKilled TINYINT(1), timeOfKickban BIGINT(15), friends VARCHAR(255), heals_life SMALLINT UNSIGNED, thirst SMALLINT UNSIGNED)");
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL setup command: " + e.getMessage());
		}
	}

	/**
	 * Empty the given table.
	 * 
	 * @param table
	 *            The table name
	 * @return non null if deletion was successful
	 */
	public ResultSet emptyTable(String table) {
		return query("TRUNCATE TABLE " + table);
	}

	/**
	 * Add a player to the table if they're not currently in.
	 * 
	 * @param player
	 *            The user to add
	 */
	public void add(Player player) {
		if (!isIn(player.getName())) {
			try {
				executeQuery("INSERT INTO playerdata (username) VALUES ('" + player.getName() + "', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "
						+ MyZ.instance.getRankFor(player) + ", 0, 0, 0, 0, '', " + Configuration.getMaxThirstLevel() + ")");
			} catch (Exception e) {
				Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL add command: " + e.getMessage());
			}
		}
	}

	/**
	 * Query a MySQL command
	 * 
	 * @param cmd
	 *            The command
	 * @return If the command executed properly
	 */
	private ResultSet query(String cmd) {
		if (!isConnected())
			return null;
		try {
			return sql.createStatement().executeQuery(cmd);
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL query command '" + cmd + "': " + e.getMessage());
			return null;
		}
	}

	/**
	 * Determine whether or not a given username is in the table.
	 * 
	 * @param name
	 *            The username to check
	 * @return true if the user is in the table.
	 */
	public boolean isIn(String name) {
		if (!isConnected())
			return false;
		try {
			return query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1").next();
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL isin command: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Get a list of all primary keys in the table.
	 * 
	 * @return A list of all the primary keys, an empty list if none found or
	 *         null if not connected.
	 */
	public List<String> getKeys() {
		if (!isConnected())
			return null;
		List<String> list = new ArrayList<String>();
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username != 'null'");
			if (rs != null)
				while (rs.next())
					if (rs.getString("username") != null)
						list.add(rs.getString("username"));
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getkeys command: " + e.getMessage());
		}
		return list;
	}

	/**
	 * Run a query to set data in MySQL.
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @param value
	 *            The value
	 */
	public void set(String name, String field, Object value) {
		try {
			executeQuery("UPDATE playerdata SET " + field + " = " + value + " WHERE username = '" + name + "' LIMIT 1");
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL set command for " + name + "." + field + ": "
					+ e.getMessage());
		}
	}

	/**
	 * Get a piece of integer data out of the MySQL database.
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The int received or 0 if nothing found
	 */
	public int getInt(String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getInt(field);
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getint command for " + name + "." + field + ": "
					+ e.getMessage());
		}
		return 0;
	}

	/**
	 * Get a piece of boolean data out of the MySQL database.
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The boolean received or false if nothing found
	 */
	public boolean getBoolean(String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getBoolean(field);
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getboolean command for " + name + "." + field + ": "
					+ e.getMessage());
		}
		return false;
	}

	/**
	 * Get a piece of long data out of the MySQL database.
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The long received or 0 if nothing found
	 */
	public long getLong(String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getLong(field);
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getlong command for " + name + "." + field + ": "
					+ e.getMessage());
		}
		return 0;
	}

	/**
	 * Get a piece of string data out of the MySQL database.
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The string received or an empty string if nothing found
	 */
	public String getString(String name, String field) {
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1");
			if (rs.next())
				return rs.getString(field);
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getstring commandfor " + name + "." + field + ": "
					+ e.getMessage());
		}
		return "";
	}

	/**
	 * Get a piece of stringlist data out of the MySQL database. Every element
	 * is separated by a comma (,)
	 * 
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The stringlist received or an empty stringlist if nothing found
	 */
	public List<String> getStringList(String name, String field) {
		List<String> returnList = new ArrayList<String>();
		try {
			ResultSet rs = query("SELECT * FROM playerdata WHERE username = '" + name + "' LIMIT 1");
			if (rs.next()) {
				String string = rs.getString(field);
				for (String player : string.split(","))
					returnList.add(player);
			}
		} catch (Exception e) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to execute MySQL getstringlist command for" + name + "." + field + ": "
					+ e.getMessage());
		}
		return returnList;
	}
}
