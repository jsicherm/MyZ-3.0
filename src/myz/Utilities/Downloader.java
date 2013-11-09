/**
 * 
 */
package myz.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import myz.Updater;
import myz.Support.Messenger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * 
 * @author DsTroyed
 * 
 *         Uses code from @author V10later's AutoUpdate class.
 * 
 */
public class Downloader {

	private static final String bukkitDevSlug = "55132";
	private static final String downloadedFileName = "MineZ-chests";
	private static String updateURL;
	private final File DataFolder;

	public Downloader(File DataFolder) {
		this.DataFolder = DataFolder;
		read();
		download();
	}

	private void download() {
		// Now try to download the file.
		try {
			URL url = new URL(updateURL);
			InputStream is = url.openStream();

			FileOutputStream fos = new FileOutputStream(DataFolder.getParentFile() + "/" + downloadedFileName + ".jar");

			byte[] buffer = new byte[4096];
			int bytesRead = 0;

			while ((bytesRead = is.read(buffer)) != -1)
				fos.write(buffer, 0, bytesRead);
			Messenger.sendConsoleMessage("&aCompleted download!");

			fos.close();
			is.close();
		} catch (IOException exc) {
			Messenger.sendConsoleMessage("&4Unable to download MineZ-chests: " + exc.getMessage());
		}
	}

	private void read() {
		Messenger.sendConsoleMessage("&eDownloading MineZ-chests.");
		try {
			URL url = new URL("https://api.curseforge.com/servermods/files?projectIds=" + bukkitDevSlug);
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);

			conn.addRequestProperty("User-Agent", "Updater (by Gravity)");

			conn.setDoOutput(true);

			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();

			final JSONArray array = (JSONArray) JSONValue.parse(response);

			if (array.size() == 0) {
				Messenger.sendConsoleMessage("&4The updater could not find any files for MineZ-chests");
				return;
			}

			updateURL = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.LINK_VALUE);
		} catch (final IOException e) {
			if (e.getMessage().contains("HTTP response code: 403"))
				Messenger.sendConsoleMessage("&4dev.bukkit.org rejected the API key for downloading plugins.");
			else {
				Messenger.sendConsoleMessage("&4The updater could not contact dev.bukkit.org for updating.");
				Messenger.sendConsoleMessage("&4The site may be experiencing temporary downtime.");
			}
			e.printStackTrace();
		}
	}
}
