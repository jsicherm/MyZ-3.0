/**
 * 
 */
package myz.Utilities;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

/**
 * @author Jordan
 * 
 */
public class DisguiseUtilities {

	private static DisguiseCraftAPI api;

	/**
	 * Whether or not the player is a zombie (disguised).
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player has a DisguiseCraft disguise on.
	 */
	public static boolean isZombie(Player player) {
		if (api == null) {
			api = DisguiseCraft.getAPI();
		}
		if (api == null) { return false; }
		return api.isDisguised(player);
	}

	/**
	 * Become a zombie.
	 * 
	 * @param player
	 *            The player to zombify.
	 */
	public static void becomeZombie(Player player) {
		if (api == null) {
			api = DisguiseCraft.getAPI();
		}
		if (api != null)
			api.disguisePlayer(player, new Disguise(DisguiseCraft.getAPI().newEntityID(), DisguiseType.Zombie));
	}

	/**
	 * Remove this players disguise.
	 * 
	 * @param player
	 *            The player.
	 */
	public static void becomeHuman(Player player) {
		if (api == null) {
			api = DisguiseCraft.getAPI();
		}
		if (api != null && api.isDisguised(player))
			api.undisguisePlayer(player);
	}
}
