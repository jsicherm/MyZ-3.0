/**
 * 
 */
package myz.mobs.pathing;

import org.bukkit.entity.Player;

import myz.MyZ;
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
		g = oclass;
	}

	public PathfinderGoalZombieAttack(EntityCreature entitycreature, double d0, boolean flag) {
		b = entitycreature;
		a = entitycreature.world;
		d = d0;
		e = flag;
		this.a(3);
	}

	@Override
	public boolean a() {
		EntityLiving entityliving = b.getGoalTarget();

		if (entityliving == null)
			return false;
		else if (!entityliving.isAlive())
			return false;
		else if (g != null && !g.isAssignableFrom(entityliving.getClass()))
			return false;
		else {
			f = b.getNavigation().a(entityliving);
			return f != null;
		}
	}

	@Override
	public boolean b() {
		EntityLiving entityliving = b.getGoalTarget();

		return entityliving == null ? false : !entityliving.isAlive() ? false : !e ? !b.getNavigation().g() : b.b(
				MathHelper.floor(entityliving.locX), MathHelper.floor(entityliving.locY), MathHelper.floor(entityliving.locZ));
	}

	@Override
	public void c() {
		b.getNavigation().a(f, d);
		h = 0;
	}

	@Override
	public void d() {
		b.getNavigation().h();
	}

	@Override
	public void e() {
		boolean disguise = MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled();
		if (b.getGoalTarget().getBukkitEntity() instanceof Player && disguise
				&& myz.Utilities.DisguiseUtilities.isZombie((Player) b.getGoalTarget().getBukkitEntity())) {
			b.setGoalTarget(null);
			return;
		}

		EntityLiving entityliving = b.getGoalTarget();

		b.getControllerLook().a(entityliving, 30.0F, 30.0F);
		if ((e || b.getEntitySenses().canSee(entityliving)) && --h <= 0) {
			h = 4 + b.aD().nextInt(7);
			b.getNavigation().a(entityliving, d);
		} else if (entityliving != null && !entityliving.isAlive() || !b.getEntitySenses().canSee(entityliving))
			b.setGoalTarget(null);

		c = Math.max(c - 1, 0);
		double d0 = b.width * 2.0F * b.width * 2.0F + entityliving.width;

		if (b.e(entityliving.locX, entityliving.boundingBox.b, entityliving.locZ) <= d0)
			if (c <= 0) {
				c = 20;
				if (b.aZ() != null)
					b.aV();

				b.m(entityliving);
			}
	}
}