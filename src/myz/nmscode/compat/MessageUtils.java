/**
 * 
 */
package myz.nmscode.compat;

import myz.MyZ;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class MessageUtils {

	public static void fancyDisplay(Player playerFor, Player player, Player typeFor, ItemStack pH, ItemStack tH) {
		switch (MyZ.version) {
		case v1_7_2:
			new myz.nmscode.v1_7_R1.messages.FancyMessage(Configuration.getPrefixForPlayerRank(playerFor)).itemTooltip(pH)
					.then(" " + Messenger.getConfigMessage(Localizer.getLocale(player), "murdered") + " ")
					.then(Configuration.getPrefixForPlayerRank(typeFor)).itemTooltip(tH);
			break;
		case v1_7_5:
			new myz.nmscode.v1_7_R2.messages.FancyMessage(Configuration.getPrefixForPlayerRank(playerFor)).itemTooltip(pH)
					.then(" " + Messenger.getConfigMessage(Localizer.getLocale(player), "murdered") + " ")
					.then(Configuration.getPrefixForPlayerRank(typeFor)).itemTooltip(tH);
			break;
		case v1_7_9:
			new myz.nmscode.v1_7_R3.messages.FancyMessage(Configuration.getPrefixForPlayerRank(playerFor)).itemTooltip(pH)
					.then(" " + Messenger.getConfigMessage(Localizer.getLocale(player), "murdered") + " ")
					.then(Configuration.getPrefixForPlayerRank(typeFor)).itemTooltip(tH);
			break;
		}
	}

	public static void removeAllHolograms() {
		switch (MyZ.version) {
		case v1_7_2:
			myz.nmscode.v1_7_R1.messages.Hologram.removeAll();
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.messages.Hologram.removeAll();
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.messages.Hologram.removeAll();
			break;
		}
	}

	public static void holographic(String message, Player player, Location location, Player[] array) {
		switch (MyZ.version) {
		case v1_7_2:
			myz.nmscode.v1_7_R1.messages.Hologram hologram = new myz.nmscode.v1_7_R1.messages.Hologram(message);
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
		case v1_7_9:
			myz.nmscode.v1_7_R3.messages.Hologram h = new myz.nmscode.v1_7_R3.messages.Hologram(message);
			h.setFollow(player);
			h.show(location, array);
			h.follow();
			break;
		}
	}

	public static void holographicDisplay(Location location, Player playerFor, String... msg) {
		switch (MyZ.version) {
		case v1_7_2:
			myz.nmscode.v1_7_R1.messages.Hologram hologram = new myz.nmscode.v1_7_R1.messages.Hologram(msg);
			hologram.show(location, playerFor);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.messages.Hologram holo = new myz.nmscode.v1_7_R2.messages.Hologram(msg);
			holo.show(location, playerFor);
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.messages.Hologram h = new myz.nmscode.v1_7_R3.messages.Hologram(msg);
			h.show(location, playerFor);
			break;
		}
	}

	public static double getHologramDistance() {
		switch (MyZ.version) {
		case v1_7_2:
			return myz.nmscode.v1_7_R1.messages.Hologram.distance;
		case v1_7_5:
			return myz.nmscode.v1_7_R2.messages.Hologram.distance;
		case v1_7_9:
			return myz.nmscode.v1_7_R3.messages.Hologram.distance;
		}
		return 0;
	}
}
