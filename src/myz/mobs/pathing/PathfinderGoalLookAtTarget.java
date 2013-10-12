/**
 * 
 */
package myz.mobs.pathing;

import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.PathfinderGoal;

/**
 * @author Jordan
 * 
 */

public class PathfinderGoalLookAtTarget extends PathfinderGoal {

	private EntityInsentient b;
	protected Entity a;
	private float c;
	private int d;
	private float e;
	private Class<? extends EntityLiving> f;

	public PathfinderGoalLookAtTarget(EntityInsentient entityinsentient, Class<? extends EntityLiving> oclass, float f) {
		b = entityinsentient;
		this.f = oclass;
		c = f;
		e = 0.02F;
		this.a(2);
	}

	public PathfinderGoalLookAtTarget(EntityInsentient entityinsentient, Class<? extends EntityLiving> oclass, float f, float f1) {
		b = entityinsentient;
		this.f = oclass;
		c = f;
		e = f1;
		this.a(2);
	}

	@Override
	public boolean a() {
		if (b.aD().nextFloat() >= e)
			return false;
		else {
			if (b.getGoalTarget() != null)
				a = b.getGoalTarget();

			if (f == EntityHuman.class)
				a = PathingSupport.findNearbyVulnerablePlayer(b);
			else
				a = b.world.a(f, b.boundingBox.grow(c, 3.0D, c), b);

			return a != null;
		}
	}

	@Override
	public boolean b() {
		return !a.isAlive() ? false : b.e(a) > c * c ? false : d > 0;
	}

	@Override
	public void c() {
		d = 40 + b.aD().nextInt(40);
	}

	@Override
	public void d() {
		a = null;
	}

	@Override
	public void e() {
		b.getControllerLook().a(a.locX, a.locY + a.getHeadHeight(), a.locZ, 10.0F, b.bp());
		--d;
	}
}