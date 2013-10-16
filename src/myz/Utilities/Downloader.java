/**
 * 
 */
package myz.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.bukkit.ChatColor;

import myz.Support.Messenger;

/**
 * 
 * @author DsTroyed
 * 
 */
public class Downloader {

	private static final String file = "http://dev.bukkit.org/media/files/728/907/MineZ-chests_v0.3.3.jar";

	public Downloader(File DataFolder) {
		String downloadedFileName = file.substring(file.lastIndexOf("/") + 1);

		try {
			URL url = new URL(file);
			InputStream is = url.openStream();

			FileOutputStream fos = new FileOutputStream(DataFolder.getParentFile() + "/" + downloadedFileName);

			byte[] buffer = new byte[4096];
			int bytesRead = 0;

			Messenger.sendConsoleMessage(ChatColor.YELLOW + "Downloading MineZ-chests!");
			while ((bytesRead = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			Messenger.sendConsoleMessage(ChatColor.GREEN + "Completed download!");

			fos.close();
			is.close();
		} catch (IOException exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Unable to download MineZ-chests: " + exc.getMessage());
		}
	}
}
