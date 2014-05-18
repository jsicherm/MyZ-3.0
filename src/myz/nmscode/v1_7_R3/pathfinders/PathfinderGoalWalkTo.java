/**
 * 
 */
package myz.nmscode.v1_7_R3.pathfinders;

import myz.nmscode.v1_7_R3.mobs.CustomEntityNPC;
import myz.nmscode.v1_7_R3.mobs.CustomEntityPigZombie;
import myz.nmscode.v1_7_R3.mobs.CustomEntityZombie;
import net.minecraft.server.v1_7_R3.EntityInsentient;
import net.minecraft.server.v1_7_R3.PathfinderGoal;

import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalWalkTo extends PathfinderGoal {

	private final float speed;
	private final EntityInsentient insentient;
	private final Location to;

	public PathfinderGoalWalkTo(EntityInsentient entityInsentient, Location to, float speed) {
		insentient = entityInsentient;
		this.speed = speed;
		this.to = to;
	}

	@Override
	public boolean a() {
		if (insentient.getBukkitEntity().getLocation().distanceSquared(to) <= 2) {
			if (insentient instanceof CustomEntityNPC)
				((CustomEntityNPC) insentient).cleanPather(this);
			else if (insentient instanceof CustomEntityPigZombie)
				((CustomEntityPigZombie) insentient).cleanPather(this);
			else if (insentient instanceof CustomEntityZombie)
				((CustomEntityZombie) insentient).cleanPather(this);
			return false;
		}
		return true;
	}

	@Override
	public void c() {
		insentient.getNavigation().a(to.getX(), to.getY(), to.getZ(), speed);
	}
}
