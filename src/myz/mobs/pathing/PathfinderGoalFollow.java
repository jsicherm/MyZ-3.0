/**
 * 
 */
package myz.mobs.pathing;

import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.PathfinderGoal;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalFollow extends PathfinderGoal {

	private EntityCreature a;
	private double b;
	private double c;
	private double d;
	private double e;
	private double f;
	private double g;
	private EntityHuman h;
	private int i;
	private boolean j;
	private boolean l;
	private boolean m;

	public PathfinderGoalFollow(EntityCreature entitycreature, double d0, boolean flag) {
		a = entitycreature;
		b = d0;
		l = flag;
		this.a(3);
	}

	@Override
	public boolean a() {
		if (i > 0) {
			--i;
			return false;
		} else {
			h = a.world.findNearbyPlayer(a, 10.0D);
			if (h != null && h.abilities.isInvulnerable)
				h = null;
			if (h == null)
				return false;
			else
				return true;
		}
	}

	@Override
	public boolean b() {
		if (l) {
			if (a.e(h) < 36.0D) {
				if (h.e(c, d, e) > 0.010000000000000002D)
					return false;

				if (Math.abs(h.pitch - f) > 5.0D || Math.abs(h.yaw - g) > 5.0D)
					return false;
			} else {
				c = h.locX;
				d = h.locY;
				e = h.locZ;
			}

			f = h.pitch;
			g = h.yaw;
		}

		return this.a();
	}

	@Override
	public void c() {
		c = h.locX;
		d = h.locY;
		e = h.locZ;
		j = true;
		m = a.getNavigation().a();
		a.getNavigation().a(false);
	}

	@Override
	public void d() {
		h = null;
		a.getNavigation().h();
		i = 100;
		j = false;
		a.getNavigation().a(m);
	}

	@Override
	public void e() {
		a.getControllerLook().a(h, 30.0F, 40);
		if (a.e(h) < 6.25D)
			a.getNavigation().h();
		else
			a.getNavigation().a(h, b);
	}

	public boolean f() {
		return j;
	}
}
