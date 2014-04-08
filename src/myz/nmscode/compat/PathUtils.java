/**
 * 
 */
package myz.nmscode.compat;

import myz.MyZ;
import myz.nmscode.v1_7_R1.pathfinders.Support;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class PathUtils {

	public static void elevate(Player player, int priority) {
		switch (MyZ.version) {
		case v1_7_2:
			Support.elevatePlayer(player, priority);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.pathfinders.Support.elevatePlayer(player, priority);
			break;
		}
	}

	public static void see(LivingEntity entity, Location location, int priority) {
		switch (MyZ.version) {
		case v1_7_2:
			Support.see(entity, location, priority);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.pathfinders.Support.see(entity, location, priority);
			break;
		}
	}

	public static float expVisibility(Player player) {
		switch (MyZ.version) {
		case v1_7_2:
			return (float) Support.experienceBarVisibility(player) / 18;
		case v1_7_5:
			return (float) myz.nmscode.v1_7_R2.pathfinders.Support.experienceBarVisibility(player) / 18;
		}
		return 0.0f;
	}
}
