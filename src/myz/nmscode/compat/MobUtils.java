/**
 * 
 */
package myz.nmscode.compat;

import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.nmscode.v1_7_R1.mobs.CustomEntityPlayer;
import myz.nmscode.v1_7_R1.mobs.CustomEntityType;
import myz.nmscode.v1_7_R1.mobs.CustomEntityZombie;
import myz.nmscode.v1_7_R1.utilities.EntityCreator;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * @author Jordan
 * 
 */
public class MobUtils {

	private static List<CustomMob> NPCs = new ArrayList<CustomMob>();

	/**
	 * Get the list of NPCs on the server.
	 * 
	 * @return The list of NPCs.
	 */
	public static List<CustomMob> getNPCs() {
		return NPCs;
	}

	public static void removeCustomPlayers() {
		for (CustomMob player : NPCs)
			player.getEntity().remove();
	}

	public static void clearCustomPlayers() {
		NPCs.clear();
	}

	public static void clearOurNPC(Player player) {
		CustomMob ourNPC = null;
		for (CustomMob npc : getNPCs())
			if (npc.getUID().equals(player.getUniqueId())) {
				ourNPC = npc;
				break;
			}
		if (ourNPC != null) {
			ourNPC.getEntity().remove();
			MobUtils.getNPCs().remove(ourNPC);
		}
	}

	public static void unregister() {
		switch (MyZ.version) {
		case v1_7_2:
			CustomEntityType.unregisterEntities();
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.mobs.CustomEntityType.unregisterEntities();
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.mobs.CustomEntityType.unregisterEntities();
			break;
		}
	}

	public static void register() {
		switch (MyZ.version) {
		case v1_7_2:
			CustomEntityType.registerEntities();
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.mobs.CustomEntityType.registerEntities();
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.mobs.CustomEntityType.registerEntities();
			break;
		}
	}

	public static void create(Location location, EntityType type, SpawnReason reason) {
		create(location, type, reason, false);
	}

	public static void create(Location location, EntityType type, SpawnReason reason, boolean a) {
		create(location, type, reason, a, false);
	}

	public static void create(Location location, EntityType type, SpawnReason reason, boolean a, boolean b) {
		switch (MyZ.version) {
		case v1_7_2:
			EntityCreator.create(location, type, reason, a, b);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.utilities.EntityCreator.create(location, type, reason, a, b);
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.utilities.EntityCreator.create(location, type, reason, a, b);
			break;
		}
	}

	public static void overrideVillager(LivingEntity entity) {
		switch (MyZ.version) {
		case v1_7_2:
			EntityCreator.overrideVillager(entity);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.utilities.EntityCreator.overrideVillager(entity);
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.utilities.EntityCreator.overrideVillager(entity);
			break;
		}
	}

	public static void disguiseNPC(Location location) {
		switch (MyZ.version) {
		case v1_7_2:
			EntityCreator.disguiseNPC(location);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.utilities.EntityCreator.disguiseNPC(location);
			break;
		case v1_7_9:
			myz.nmscode.v1_7_R3.utilities.EntityCreator.disguiseNPC(location);
			break;
		}
	}

	public static CustomMob newCustomPlayer(Player playerDuplicate) {
		switch (MyZ.version) {
		case v1_7_2:
			return CustomEntityPlayer.newInstance(playerDuplicate);
		case v1_7_5:
			return myz.nmscode.v1_7_R2.mobs.CustomEntityPlayer.newInstance(playerDuplicate);
		case v1_7_9:
			return myz.nmscode.v1_7_R3.mobs.CustomEntityPlayer.newInstance(playerDuplicate);
		}
		return null;
	}

	public static CustomMob newCustomZombie(Player player) {
		switch (MyZ.version) {
		case v1_7_2:
			return CustomEntityZombie.newInstance(player);
		case v1_7_5:
			return myz.nmscode.v1_7_R2.mobs.CustomEntityZombie.newInstance(player);
		case v1_7_9:
			return myz.nmscode.v1_7_R3.mobs.CustomEntityZombie.newInstance(player);
		}
		return null;
	}
}
