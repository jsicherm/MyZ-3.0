/**
 * 
 */
package myz.nmscode.v1_7_R3.pathfinders;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_7_R3.DistanceComparator;
import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.IEntitySelector;
import net.minecraft.server.v1_7_R3.PathfinderGoalTarget;

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
			e = pathfindergoalnearestattackabletarget;
			d = ientityselector;
		}

		@Override
		public boolean a(Entity entity) {
			if (!(entity instanceof EntityLiving))
				return false;
			if (e.creature.getGoalTarget() != null && e.creature.getGoalTarget().equals(entity))
				return true;

			if (!(entity instanceof EntityLiving) ? false : d != null && !d.a(entity) ? false : entity.isInvulnerable() ? false : e.a(
					(EntityLiving) entity, false)) {
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
		creature = entitycreature;
		a = oclass;
		b = i;
		e = new DistanceComparator(entitycreature);
		this.a(1);
		f = new EntitySelectorNearestAttackableZombieTarget(this, ientityselector);
	}

	@Override
	public boolean a() {
		if (b > 0 && c.aH().nextInt(b) != 0)
			return false;
		else {
			double d0 = f();
			List list = c.world.a(a, c.boundingBox.grow(d0, 4.0D, d0), f);

			Collections.sort(list, e);
			if (list.isEmpty())
				return false;
			else {
				g = (EntityLiving) list.get(0);
				return true;
			}
		}
	}

	@Override
	public void c() {
		c.setGoalTarget(g);
		super.c();
	}
}
