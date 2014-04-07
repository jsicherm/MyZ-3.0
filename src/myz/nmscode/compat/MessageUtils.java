/**
 * 
 */
package myz.nmscode.compat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import myz.MyZ;
import myz.nmscode.v1_7_R1.messages.Hologram;

/**
 * @author Jordan
 * 
 */
public class MessageUtils {

	public static void removeAllHolograms() {
		switch (MyZ.version) {
		case v1_7_2:
			Hologram.removeAll();
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.messages.Hologram.removeAll();
			break;
		}
	}

	public static void holographic(String message, Player player, Location location, Player[] array) {
		switch (MyZ.version) {
		case v1_7_2:
			Hologram hologram = new Hologram(message);
			hologram.setFollow(player);
			hologram.show(location, array);
			hologram.follow();
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.messages.Hologram holo = new myz.nmscode.v1_7_R2.messages.Hologram(message);
			holo.setFollow(player);
			holo.show(location, array);
			holo.follow();
			break;
		}
	}

	public static void holographicDisplay(Location location, Player playerFor, String... msg) {
		switch (MyZ.version) {
		case v1_7_2:
			Hologram hologram = new Hologram(msg);
			hologram.show(location, playerFor);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.messages.Hologram holo = new myz.nmscode.v1_7_R2.messages.Hologram(msg);
			holo.show(location, playerFor);
			break;
		}
	}

	public static double getHologramDistance() {
		switch (MyZ.version) {
		case v1_7_2:
			return Hologram.distance;
		case v1_7_5:
			return myz.nmscode.v1_7_R2.messages.Hologram.distance;
		}
		return 0;
	}
}
