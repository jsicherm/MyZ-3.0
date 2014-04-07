/**
 * 
 */
package myz.nmscode.v1_7_R2.pathfinders;

import java.util.Collections;
import java.util.List;

import myz.nmscode.v1_7_R2.pathfinders.Support;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.DistanceComparator;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityCreature;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.IEntitySelector;
import net.minecraft.server.v1_7_R2.PathfinderGoalTarget;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalNearestAttackableZombieTarget extends PathfinderGoalTarget {

	private final Class a;
	private final int b;
	private final DistanceComparator e;
	private final IEntitySelector f;
	private EntityLiving g;

	private EntityCreature creature;

	class EntitySelectorNearestAttackableZombieTarget implements IEntitySelector {

		final IEntitySelector d;
		final PathfinderGoalNearestAttackableZombieTarget e;

		EntitySelectorNearestAttackableZombieTarget(PathfinderGoalNearestAttackableZombieTarget pathfindergoalnearestattackabletarget,
				IEntitySelector ientityselector) {
			this.e = pathfindergoalnearestattackabletarget;
			this.d = ientityselector;
		}

		public boolean a(Entity entity) {
			if (!(entity instanceof EntityLiving))
				return false;
			if (e.creature.getGoalTarget() != null && e.creature.getGoalTarget().equals(entity)) { return true; }

			if (!(entity instanceof EntityLiving) ? false : (this.d != null && !this.d.a(entity) ? false : entity.isInvulnerable() ? false
					: this.e.a((EntityLiving) entity, false))) {
				Entity target = Support.findNearbyVulnerablePlayer(e.creature);
				if (target != null && target instanceof EntityLiving && !target.isInvulnerable() && target.equals(entity)
						|| !(target instanceof EntityHuman)) {
					e.creature.setGoalTarget((EntityLiving) target);
					return true;
				}
			}

			return false;
		}
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag) {
		this(entitycreature, oclass, i, flag, false);
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1) {
		this(entitycreature, oclass, i, flag, flag1, (IEntitySelector) null);
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1,
			IEntitySelector ientityselector) {
		super(entitycreature, flag, flag1);
		this.creature = entitycreature;
		this.a = oclass;
		this.b = i;
		this.e = new DistanceComparator(entitycreature);
		this.a(1);
		this.f = new EntitySelectorNearestAttackableZombieTarget(this, ientityselector);
	}

	public boolean a() {
		if (this.b > 0 && this.c.aH().nextInt(this.b) != 0) {
			return false;
		} else {
			double d0 = this.f();
			List list = this.c.world.a(this.a, this.c.boundingBox.grow(d0, 4.0D, d0), this.f);

			Collections.sort(list, this.e);
			if (list.isEmpty()) {
				return false;
			} else {
				this.g = (EntityLiving) list.get(0);
				return true;
			}
		}
	}

	public void c() {
		this.c.setGoalTarget(this.g);
		super.c();
	}
}
