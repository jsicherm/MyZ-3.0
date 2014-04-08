/**
 * 
 */
package myz.nmscode.v1_7_R2.pathfinders;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_7_R2.EntityCreature;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.PathfinderGoal;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalFollow extends PathfinderGoal {

	EntityCreature a;
	EntityHuman b;
	double c;
	private int d;

	public PathfinderGoalFollow(EntityCreature entity, double d0) {
		a = entity;
		c = d0;
	}

	@Override
	public boolean a() {
		List list = a.world.a(b.getClass(), a.boundingBox.grow(8.0D, 4.0D, 8.0D));
		EntityHuman entityanimal = null;
		double d0 = Double.MAX_VALUE;
		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			EntityHuman entityanimal1 = (EntityHuman) iterator.next();

			double d1 = a.f(entityanimal1);

			if (d1 <= d0) {
				d0 = d1;
				entityanimal = entityanimal1;
			}
		}

		if (entityanimal == null)
			return false;
		else if (d0 < 9.0D)
			return false;
		else {
			b = entityanimal;
			return true;
		}
	}

	@Override
	public boolean b() {
		if (!b.isAlive())
			return false;
		else {
			double d0 = a.f(b);

			return d0 >= 9.0D && d0 <= 256.0D;
		}
	}

	@Override
	public void c() {
		d = 0;
	}

	@Override
	public void d() {
		b = null;
	}

	@Override
	public void e() {
		if (--d <= 0) {
			d = 10;
			a.getNavigation().a(b, c);
		}
	}
}
