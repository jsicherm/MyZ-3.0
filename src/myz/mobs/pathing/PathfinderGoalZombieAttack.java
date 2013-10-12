/**
 * 
 */
package myz.mobs.pathing;

import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityCreature;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.PathEntity;
import net.minecraft.server.v1_6_R3.PathfinderGoal;
import net.minecraft.server.v1_6_R3.World;

/**
 * @author Jordan *
 */
public class PathfinderGoalZombieAttack extends PathfinderGoal {

	World a;
	EntityCreature b;
	int c;
	double d;
	boolean e;
	PathEntity f;
	Class<? extends EntityLiving> g;
	private int h;

	public PathfinderGoalZombieAttack(EntityCreature entitycreature, Class<? extends EntityLiving> oclass, double d0, boolean flag) {
		this(entitycreature, d0, flag);
		this.g = oclass;
	}

	public PathfinderGoalZombieAttack(EntityCreature entitycreature, double d0, boolean flag) {
		this.b = entitycreature;
		this.a = entitycreature.world;
		this.d = d0;
		this.e = flag;
		this.a(3);
	}

	public boolean a() {
		EntityLiving entityliving = this.b.getGoalTarget();

		if (entityliving == null) {
			return false;
		} else if (!entityliving.isAlive()) {
			return false;
		} else if (this.g != null && !this.g.isAssignableFrom(entityliving.getClass())) {
			return false;
		} else {
			this.f = this.b.getNavigation().a(entityliving);
			return this.f != null;
		}
	}

	public boolean b() {
		EntityLiving entityliving = this.b.getGoalTarget();

		return entityliving == null ? false : (!entityliving.isAlive() ? false : (!this.e ? !this.b.getNavigation().g() : this.b.b(
				MathHelper.floor(entityliving.locX), MathHelper.floor(entityliving.locY), MathHelper.floor(entityliving.locZ))));
	}

	public void c() {
		this.b.getNavigation().a(this.f, this.d);
		this.h = 0;
	}

	public void d() {
		this.b.getNavigation().h();
	}

	public void e() {
		EntityLiving entityliving = this.b.getGoalTarget();

		this.b.getControllerLook().a(entityliving, 30.0F, 30.0F);
		if ((this.e || this.b.getEntitySenses().canSee(entityliving)) && --this.h <= 0) {
			this.h = 4 + this.b.aD().nextInt(7);
			this.b.getNavigation().a((Entity) entityliving, this.d);
		}

		this.c = Math.max(this.c - 1, 0);
		double d0 = (double) (this.b.width * 2.0F * this.b.width * 2.0F + entityliving.width);

		if (this.b.e(entityliving.locX, entityliving.boundingBox.b, entityliving.locZ) <= d0) {
			if (this.c <= 0) {
				this.c = 20;
				if (this.b.aZ() != null) {
					this.b.aV();
				}

				this.b.m(entityliving);
			}
		}
	}
}