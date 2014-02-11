/**
 * 
 */
package myz.utilities;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class LibsDisguiseUtils {

	/**
	 * Make sure players can see disguises, including their own.
	 */
	public static void setup() {
		// DisguiseAPI.setViewDisguises(true);
		// DisguiseAPI.setHearSelfDisguise(true);
	}

	/**
	 * Whether or not the player is a zombie (disguised).
	 * 
	 * @param player
	 *            The player.
	 * @return True if the player has a DisguiseCraft disguise on.
	 */
	public static boolean isZombie(Player player) {
		return DisguiseAPI.isDisguised(player);
	}

	/**
	 * Become a zombie.
	 * 
	 * @param player
	 *            The player to zombify.
	 */
	public static void becomeZombie(Player player) {
		DisguiseAPI.disguiseToAll(player, new MobDisguise(DisguiseType.ZOMBIE));
	}

	/**
	 * Remove this entity's disguise.
	 * 
	 * @param entity
	 *            The entity.
	 */
	public static void undisguise(LivingEntity entity) {
		DisguiseAPI.undisguiseToAll(entity);
	}

	/**
	 * Make an entity look like a player.
	 * 
	 * @param entity
	 *            The entity to turn into an NPC.
	 * @param name
	 *            The entity name.
	 */
	public static void becomeNPC(LivingEntity entity, String name) {
		if (entity != null)
			DisguiseAPI.disguiseToAll(entity, new PlayerDisguise(name + "")); // Null-buster
																				// 3000
																				// (patent
																				// pending)
	}
}
