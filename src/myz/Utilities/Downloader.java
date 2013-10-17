/**
 * 
 */
package myz.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 * @author DsTroyed
 * 
 *         Uses code from @author V10later's AutoUpdate class.
 * 
 */
public class Downloader {

	private static final String bukkitDevSlug = "minez-chests";
	private static final String downloadedFileName = "MineZ-chests";
	private static String updateURL;

	public Downloader(File DataFolder) {
		InputStreamReader stream;

		// Connect to BukGet to find the latest version of MineZ-Chests
		try {
			URL bukgetURL = new URL("http://api.bukget.org/api2/bukkit/plugin/" + bukkitDevSlug + "/latest");
			HttpURLConnection bukgetConnect = (HttpURLConnection) bukgetURL.openConnection();
			bukgetConnect.connect();

			int response = bukgetConnect.getResponseCode();
			if (response != 200)
				return;

			stream = new InputStreamReader(bukgetConnect.getInputStream());

			JSONParser jp = new JSONParser();
			Object o = jp.parse(stream);

			if (!(o instanceof JSONObject)) {
				stream.close();
				return;
			}

			JSONObject jo = (JSONObject) o;
			jo = (JSONObject) jo.get("versions");
			updateURL = (String) jo.get("download");
			stream.close();
		} catch (Exception exc) {
			return;
		}

		// Unable to fetch the updateURL, cancel.
		if (updateURL == null)
			return;

		// Now try to download the file.
		try {
			URL url = new URL(updateURL);
			InputStream is = url.openStream();

			FileOutputStream fos = new FileOutputStream(DataFolder.getParentFile() + "/" + downloadedFileName + ".jar");

			byte[] buffer = new byte[4096];
			int bytesRead = 0;

			Messenger.sendConsoleMessage(ChatColor.YELLOW + "Downloading MineZ-chests!");
			while ((bytesRead = is.read(buffer)) != -1)
				fos.write(buffer, 0, bytesRead);
			Messenger.sendConsoleMessage(ChatColor.GREEN + "Completed download!");

			fos.close();
			is.close();
		} catch (IOException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to download MineZ-chests: " + exc.getMessage());
		}
	}
}
