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
		case v1_7_R1:
			Hologram.removeAll();
			break;
		}
	}

	public static void holographic(String message, Player player, Location location, Player[] array) {
		Hologram hologram = new Hologram(message);
		hologram.setFollow(player);
		hologram.show(location, array);
		hologram.follow();
	}

	public static void holographicDisplay(Location location, Player playerFor, String... msg) {
		Hologram hologram = new Hologram(msg);
		hologram.show(location, playerFor);
	}

	public static double getHologramDistance() {
		switch (MyZ.version) {
		case v1_7_R1:
			return Hologram.distance;
		}
		return 0;
	}
}
