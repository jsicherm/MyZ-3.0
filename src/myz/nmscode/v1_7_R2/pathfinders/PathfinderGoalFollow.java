/**
 * 
 */
package myz.nmscode.v1_7_R2.pathfinders;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_7_R2.Entity;
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
		this.a = entity;
		this.c = d0;
	}

	public boolean a() {
		List list = this.a.world.a(this.b.getClass(), this.a.boundingBox.grow(8.0D, 4.0D, 8.0D));
		EntityHuman entityanimal = null;
		double d0 = Double.MAX_VALUE;
		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			EntityHuman entityanimal1 = (EntityHuman) iterator.next();

			double d1 = this.a.f(entityanimal1);

			if (d1 <= d0) {
				d0 = d1;
				entityanimal = entityanimal1;
			}
		}

		if (entityanimal == null) {
			return false;
		} else if (d0 < 9.0D) {
			return false;
		} else {
			this.b = entityanimal;
			return true;
		}
	}

	public boolean b() {
		if (!this.b.isAlive()) {
			return false;
		} else {
			double d0 = this.a.f(this.b);

			return d0 >= 9.0D && d0 <= 256.0D;
		}
	}

	public void c() {
		this.d = 0;
	}

	public void d() {
		this.b = null;
	}

	public void e() {
		if (--this.d <= 0) {
			this.d = 10;
			this.a.getNavigation().a((Entity) this.b, this.c);
		}
	}
}
