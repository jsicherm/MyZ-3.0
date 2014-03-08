/**
 * 
 */
package myz.mobs.pathing;

import myz.mobs.CustomEntityNPC;
import myz.mobs.CustomEntityPigZombie;
import myz.mobs.CustomEntityZombie;
import net.minecraft.server.v1_7_R1.EntityInsentient;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalWalkTo extends PathfinderGoal {

	private final float speed;
	private final EntityInsentient insentient;
	private final Location to;

	public PathfinderGoalWalkTo(EntityInsentient entity, Location to, float speed) {
		insentient = entity;
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
