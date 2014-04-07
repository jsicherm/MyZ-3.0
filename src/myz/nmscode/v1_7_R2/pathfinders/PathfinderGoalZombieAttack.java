/**
 * 
 */
package myz.nmscode.v1_7_R2.pathfinders;

import myz.MyZ;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityCreature;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.MathHelper;
import net.minecraft.server.v1_7_R2.PathEntity;
import net.minecraft.server.v1_7_R2.PathfinderGoal;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.entity.Player;

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
	Class g;
	private int h;
	private double i;
	private double j;
	private double k;

	public PathfinderGoalZombieAttack(EntityCreature entitycreature, Class oclass, double d0, boolean flag) {
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
		} else if (!entityliving.isAlive() || entityliving.isInvulnerable()) {
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

		return entityliving == null ? false : (!entityliving.isAlive() || entityliving.isInvulnerable() ? false : (!this.e ? !this.b
				.getNavigation().g() : this.b.b(MathHelper.floor(entityliving.locX), MathHelper.floor(entityliving.locY),
				MathHelper.floor(entityliving.locZ))));
	}

	public void c() {
		this.b.getNavigation().a(this.f, this.d);
		this.h = 0;
	}

	public void d() {
		this.b.getNavigation().h();
	}

	public void e() {
		boolean disguise = MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled();

		if (b.getGoalTarget() == null) { return; }

		if (b.getGoalTarget().getBukkitEntity() instanceof Player && disguise
				&& myz.utilities.DisguiseUtils.isZombie((Player) b.getGoalTarget().getBukkitEntity())) {
			b.setGoalTarget(null);
			return;
		}

		disguise = MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled();

		if (b.getGoalTarget().getBukkitEntity() instanceof Player && disguise
				&& myz.utilities.LibsDisguiseUtils.isZombie((Player) b.getGoalTarget().getBukkitEntity())) {
			b.setGoalTarget(null);
			return;
		}

		EntityLiving entityliving = this.b.getGoalTarget();
		if (entityliving.isInvulnerable()) {
			b.setGoalTarget(null);
			return;
		}

		this.b.getControllerLook().a(entityliving, 30.0F, 30.0F);
		double d0 = this.b.e(entityliving.locX, entityliving.boundingBox.b, entityliving.locZ);
		double d1 = (double) (this.b.width * 2.0F * this.b.width * 2.0F + entityliving.width);

		--this.h;
		if ((this.e || this.b.getEntitySenses().canSee(entityliving))
				&& this.h <= 0
				&& (this.i == 0.0D && this.j == 0.0D && this.k == 0.0D || entityliving.e(this.i, this.j, this.k) >= 1.0D || this.b.aH()
						.nextFloat() < 0.05F)) {
			this.i = entityliving.locX;
			this.j = entityliving.boundingBox.b;
			this.k = entityliving.locZ;
			this.h = 4 + this.b.aH().nextInt(7);
			if (d0 > 1024.0D) {
				this.h += 10;
			} else if (d0 > 256.0D) {
				this.h += 5;
			}

			if (!this.b.getNavigation().a((Entity) entityliving, this.d)) {
				this.h += 15;
			}
		}

		this.c = Math.max(this.c - 1, 0);
		if (d0 <= d1 && this.c <= 20) {
			this.c = 20;
			if (this.b.bd() != null) {
				this.b.aZ();
			}

			this.b.n(entityliving);
		}
	}
}