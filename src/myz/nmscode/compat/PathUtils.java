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
		case v1_7_R1:
			Support.elevatePlayer(player, priority);
			break;
		}
	}

	public static void see(LivingEntity entity, Location location, int priority) {
		switch (MyZ.version) {
		case v1_7_R1:
			Support.see(entity, location, priority);
			break;
		}
	}
}
