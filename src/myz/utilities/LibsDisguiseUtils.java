/**
 * 
 */
package myz.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class LibsDisguiseUtils {

	private static final Map<LivingEntity, String> packets = new HashMap<LivingEntity, String>();

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
			packets.put(entity, name);
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
	 * Update all the stashed entities to become NPCs.
	 */
	public static void beNPCs() {
		if (packets.isEmpty() || Bukkit.getOnlinePlayers() == null)
			return;
		Set<LivingEntity> useless = new HashSet<LivingEntity>();
		for (LivingEntity entity : packets.keySet()) {
			if (entity == null || entity.isDead()) {
				useless.add(entity);
				continue;
			}
			DisguiseAPI.disguiseToAll(entity, new PlayerDisguise(packets.get(entity) + ""));
		}
		for (LivingEntity entity : useless)
			packets.remove(entity);
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
	 * Make sure players can see disguises, including their own.
	 */
	public static void setup() {
		// DisguiseAPI.setViewDisguises(true);
		// DisguiseAPI.setHearSelfDisguise(true);
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
}
