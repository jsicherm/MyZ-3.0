/*
 * Updater for Bukkit.
 *
 * This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
 */

package myz;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import myz.support.interfacing.Messenger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.comphenix.protocol.metrics.Updater.UpdateType;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the
 * updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding
 * auto-update toggles in your plugin's config, this system provides NO CHECK
 * WITH YOUR CONFIG to make sure the user has allowed auto-updating. <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config
 * that prevents the auto-updater from running <b>AT ALL</b>. <br>
 * If you fail to include this option in your config, your plugin will be
 * <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to
 * 'auto-update: true' - if this value is set to false you may NOT run the
 * auto-updater. <br>
 * If you are unsure about these rules, please read the plugin submission
 * guidelines: http://goo.gl/8iU5l
 * 
 * @author Gravity
 * @version 2.0
 */

public class Updater implements CommandExecutor, CommandSender {

	private Plugin plugin;
	private String versionName, newVersionName;
	private String versionLink;
	private String versionType;
	private String versionGameVersion;

	private boolean announce; // Whether to announce file downloads

	private URL url; // Connecting to RSS
	private File file; // The plugin's file
	private Thread thread; // Updater thread

	public static final String TITLE_VALUE = "name";
	public static final String LINK_VALUE = "downloadUrl";
	public static final String TYPE_VALUE = "releaseType";
	public static final String VERSION_VALUE = "gameVersion";
	private static final String QUERY = "/servermods/files?projectIds=";
	private static final String HOST = "https://api.curseforge.com";
	private boolean hasUpdate; // Whether or not an update was found

	private static final int BYTE_SIZE = 1024; // Used for downloading files
	private String updateFolder;// The folder that downloads will be placed in

	/**
	 * Initialize the updater
	 * 
	 * @param plugin
	 *            The plugin that is checking for an update.
	 * @param id
	 *            The dev.bukkit.org id of the project
	 * @param file
	 *            The file that the plugin is running from, get this by doing
	 *            this.getFile() from within your main class.
	 * @param type
	 *            Specify the type of update this will be. See
	 *            {@link UpdateType}
	 * @param announce
	 *            True if the program should announce the progress of new
	 *            updates in console
	 */
	public Updater(Plugin plugin, int id, File file, boolean announce) {
		this.plugin = plugin;
		this.announce = announce;
		this.file = file;
		updateFolder = plugin.getServer().getUpdateFolder();

		try {
			url = new URL(Updater.HOST + Updater.QUERY + id);
		} catch (final MalformedURLException e) {
			Messenger.sendConsoleMessage("&4Unable to find MyZ on dev.bukkit");
			e.printStackTrace();
		}

		thread = new Thread(new UpdateRunnable());
		thread.start();
		registerCommand();
	}

	/**
	 * Get the latest version's release type (release, beta, or alpha).
	 */
	public String getLatestType() {
		waitForThread();
		return versionType;
	}

	/**
	 * Get the latest version's game version.
	 */
	public String getLatestGameVersion() {
		waitForThread();
		return versionGameVersion;
	}

	/**
	 * Get the latest version's name.
	 */
	public String getLatestName() {
		waitForThread();
		return versionName;
	}

	/**
	 * Get the latest version's file link.
	 */
	public String getLatestFileLink() {
		waitForThread();
		return versionLink;
	}

	/**
	 * As the result of Updater output depends on the thread's completion, it is
	 * necessary to wait for the thread to finish before allowing anyone to
	 * check the result.
	 */
	private void waitForThread() {
		if (thread != null && thread.isAlive())
			try {
				thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void registerCommand() {
		try {
			SimplePluginManager pm = (SimplePluginManager) plugin.getServer().getPluginManager();
			Field f = SimplePluginManager.class.getDeclaredField("commandMap");
			f.setAccessible(true);
			SimpleCommandMap cm = (SimpleCommandMap) f.get(pm);
			f.setAccessible(false);
			if (cm.getCommand("update") == null) // First!
			{
				Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				c.setAccessible(true);
				PluginCommand cmd = c.newInstance("update", plugin);
				c.setAccessible(false);
				cmd.setExecutor(this);
				cm.register("update", cmd);
			}
		} catch (Throwable t) {
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if (!hasUpdate) {
			sender.sendMessage("No update was found.");
			return true;
		}
		if (sender.hasPermission("MyZ.update"))
			if (args.length != 0)
				if (args[0].equalsIgnoreCase("MyZ"))
					new Thread(new Runnable() {
						@Override
						public void run() {
							String name = file.getName();
							// If it's a zip file, it shouldn't be downloaded as
							// the plugin's name
							if (versionLink.endsWith(".zip")) {
								final String[] split = versionLink.split("/");
								name = split[split.length - 1];
							}
							Updater.this.saveFile(new File(plugin.getDataFolder().getParent(), updateFolder), name, versionLink, sender);
						}
					}).run();
		return true;
	}

	/**
	 * Save an update from dev.bukkit.org into the server's update folder.
	 */
	private void saveFile(File folder, String file, String u, CommandSender updater) {
		if (!folder.exists())
			folder.mkdir();
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			// Download the file
			final URL url = new URL(u);
			final int fileLength = url.openConnection().getContentLength();
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);

			final byte[] data = new byte[Updater.BYTE_SIZE];
			int count;
			long downloaded = 0;
			while ((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1) {
				downloaded += count;
				fout.write(data, 0, count);
				final int percent = (int) (downloaded * 100 / fileLength);
				if (announce && percent % 10 == 0) {
					Messenger.sendConsoleMessage("&eDownloading update: " + percent + "% of " + fileLength + " bytes.");
					if (!(updater instanceof ConsoleCommandSender))
						updater.sendMessage("Downloading update: " + percent + "% of " + fileLength + " bytes.");
				}
			}
			// Just a quick check to make sure we didn't leave any files from
			// last time...
			for (final File xFile : new File(plugin.getDataFolder().getParent(), updateFolder).listFiles())
				if (xFile.getName().endsWith(".zip"))
					xFile.delete();
			// Check to see if it's a zip file, if it is, unzip it.
			final File dFile = new File(folder.getAbsolutePath() + "/" + file);
			if (dFile.getName().endsWith(".zip"))
				// Unzip
				unzip(dFile.getCanonicalPath());
			if (announce) {
				Messenger.sendConsoleMessage("&eRestart your server to complete the update.");
				if (!(updater instanceof ConsoleCommandSender))
					updater.sendMessage("Restart your server to complete the update.");
			}
		} catch (final Exception ex) {
			Messenger.sendConsoleMessage("&4The auto-updater tried to download a new update, but was unsuccessful.");
		} finally {
			try {
				if (in != null)
					in.close();
				if (fout != null)
					fout.close();
			} catch (final Exception ex) {
			}
		}
	}

	/**
	 * Part of Zip-File-Extractor, modified by Gravity for use with Bukkit
	 */
	private void unzip(String file) {
		try {
			final File fSourceZip = new File(file);
			final String zipPath = file.substring(0, file.length() - 4);
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());
				destinationFilePath.getParentFile().mkdirs();
				if (entry.isDirectory())
					continue;
				else {
					final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
					int b;
					final byte buffer[] = new byte[Updater.BYTE_SIZE];
					final FileOutputStream fos = new FileOutputStream(destinationFilePath);
					final BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE);
					while ((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1)
						bos.write(buffer, 0, b);
					bos.flush();
					bos.close();
					bis.close();
					final String name = destinationFilePath.getName();
					if (name.endsWith(".jar") && pluginFile(name))
						destinationFilePath.renameTo(new File(plugin.getDataFolder().getParent(), updateFolder + "/" + name));
				}
				entry = null;
				destinationFilePath = null;
			}
			e = null;
			zipFile.close();
			zipFile = null;

			// Move any plugin data folders that were included to the right
			// place, Bukkit won't do this for us.
			for (final File dFile : new File(zipPath).listFiles()) {
				if (dFile.isDirectory())
					if (pluginFile(dFile.getName())) {
						final File oFile = new File(plugin.getDataFolder().getParent(), dFile.getName());
						final File[] contents = oFile.listFiles();
						for (final File cFile : dFile.listFiles()) {
							boolean found = false;
							for (final File xFile : contents)
								if (xFile.getName().equals(cFile.getName())) {
									found = true;
									break;
								}
							if (!found)
								// Move the new file into the current dir
								cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
							else
								// This file already exists.
								cFile.delete();
						}
					}
				dFile.delete();
			}
			new File(zipPath).delete();
			fSourceZip.delete();
		} catch (final IOException ex) {
			Messenger.sendConsoleMessage("&4The auto-updater tried to unzip a new update file, but was unsuccessful.");
			ex.printStackTrace();
		}
		new File(file).delete();
	}

	/**
	 * Check if the name of a jar is one of the plugins currently installed,
	 * used for extracting the correct files out of a zip.
	 */
	private boolean pluginFile(String name) {
		for (final File file : new File("plugins").listFiles())
			if (file.getName().equals(name))
				return true;
		return false;
	}

	/**
	 * Check to see if the program should continue by evaluation whether the
	 * plugin is already updated, or shouldn't be updated
	 */
	private boolean versionCheck(String title) {
		final String version = plugin.getDescription().getVersion();
		if (version.equalsIgnoreCase(newVersionName = title.replaceAll("[^\\d.]", "").replaceAll(" ", "")))
			return false;
		return !newVersionName.startsWith("2");
	}

	private boolean read() {
		try {
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);

			conn.addRequestProperty("User-Agent", "Updater (by Gravity)");

			conn.setDoOutput(true);

			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();

			final JSONArray array = (JSONArray) JSONValue.parse(response);

			if (array.size() == 0) {
				Messenger.sendConsoleMessage("&4The updater could not find any files for MyZ");
				return false;
			}

			versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TITLE_VALUE);
			versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.LINK_VALUE);
			versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TYPE_VALUE);
			versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.VERSION_VALUE);

			return true;
		} catch (final IOException e) {
			if (e.getMessage().contains("HTTP response code: 403"))
				plugin.getLogger().warning("dev.bukkit.org rejected the API key for downloading plugins.");
			else {
				Messenger.sendConsoleMessage("&4The updater could not contact dev.bukkit.org for updating.");
				Messenger.sendConsoleMessage("&4The site may be experiencing temporary downtime.");
			}
			e.printStackTrace();
			return false;
		}
	}

	private class UpdateRunnable implements Runnable {

		@Override
		public void run() {
			if (url != null)
				// Obtain the results of the project's file feed
				if (read())
					if (versionCheck(versionName))
						if (versionLink != null) {
							hasUpdate = true;
							Messenger.sendConsoleMessage("&aAn update was found for MyZ 3.0. If you wish to update from &e"
									+ plugin.getDescription().getVersion() + "&a to &e" + newVersionName + "&a, use &e/update MyZ&a.");
						}
		}
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#addAttachment(org.bukkit.plugin.Plugin)
	 */
	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#addAttachment(org.bukkit.plugin.Plugin, int)
	 */
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#addAttachment(org.bukkit.plugin.Plugin, java.lang.String, boolean)
	 */
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#addAttachment(org.bukkit.plugin.Plugin, java.lang.String, boolean, int)
	 */
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#getEffectivePermissions()
	 */
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#hasPermission(java.lang.String)
	 */
	@Override
	public boolean hasPermission(String arg0) {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#hasPermission(org.bukkit.permissions.Permission)
	 */
	@Override
	public boolean hasPermission(Permission arg0) {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#isPermissionSet(java.lang.String)
	 */
	@Override
	public boolean isPermissionSet(String arg0) {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#isPermissionSet(org.bukkit.permissions.Permission)
	 */
	@Override
	public boolean isPermissionSet(Permission arg0) {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#recalculatePermissions()
	 */
	@Override
	public void recalculatePermissions() {

	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.Permissible#removeAttachment(org.bukkit.permissions.PermissionAttachment)
	 */
	@Override
	public void removeAttachment(PermissionAttachment arg0) {

	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.ServerOperator#isOp()
	 */
	@Override
	public boolean isOp() {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.permissions.ServerOperator#setOp(boolean)
	 */
	@Override
	public void setOp(boolean arg0) {

	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandSender#getName()
	 */
	@Override
	public String getName() {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandSender#getServer()
	 */
	@Override
	public Server getServer() {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandSender#sendMessage(java.lang.String)
	 */
	@Override
	public void sendMessage(String arg0) {

	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandSender#sendMessage(java.lang.String[])
	 */
	@Override
	public void sendMessage(String[] arg0) {

	}
}
