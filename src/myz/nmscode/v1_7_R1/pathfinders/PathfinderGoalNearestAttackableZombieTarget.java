/**
 * 
 */
package myz.nmscode.v1_7_R1.pathfinders;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_7_R1.DistanceComparator;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.IEntitySelector;
import net.minecraft.server.v1_7_R1.PathfinderGoalTarget;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalNearestAttackableZombieTarget extends PathfinderGoalTarget {

	private final Class<? extends EntityLiving> a;
	private final int b;
	private final EntityCreature cc;
	private final DistanceComparator e;
	private final IEntitySelector f;

	// private EntityLiving g;

	class EntitySelectorNearestAttackableZombieTarget implements IEntitySelector {

		final IEntitySelector c;

		final PathfinderGoalNearestAttackableZombieTarget d;

		EntitySelectorNearestAttackableZombieTarget(PathfinderGoalNearestAttackableZombieTarget pathfindergoalnearestattackabletarget,
				IEntitySelector ientityselector) {
			d = pathfindergoalnearestattackabletarget;
			c = ientityselector;
		}

		@Override
		public boolean a(Entity entity) {
			if (!(entity instanceof EntityLiving))
				return false;
			Entity target = Support.findNearbyVulnerablePlayer(d.cc);
			if (target != null && target.equals(entity) || !(target instanceof EntityHuman))
				return true;
			return c != null && !c.a(entity) ? false : d.a((EntityLiving) entity, false);
		}
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature creature, Class<? extends EntityLiving> oclass, int i, boolean flag) {
		this(creature, oclass, i, flag, false);
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature creature, Class<? extends EntityLiving> oclass, int i, boolean flag,
			boolean flag1) {
		this(creature, oclass, i, flag, flag1, (IEntitySelector) null);
	}

	public PathfinderGoalNearestAttackableZombieTarget(EntityCreature creature, Class<? extends EntityLiving> oclass, int i, boolean flag,
			boolean flag1, IEntitySelector ientityselector) {
		super(creature, flag, flag1);
		a = oclass;
		b = i;
		cc = creature;
		e = new DistanceComparator(creature);
		this.a(1);
		f = new EntitySelectorNearestAttackableZombieTarget(this, ientityselector);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean a() {
		if (b > 0 && c.aI().nextInt(b) != 0)
			return false;
		else {
			double d0 = f();
			List<EntityLiving> list = c.world.a(a, c.boundingBox.grow(d0, 4.0D, d0), f);

			Collections.sort(list, e);
			if (list.isEmpty())
				return false;
			else
				// g = list.get(0);
				return true;
		}
	}

	@Override
	public void c() {
		super.c();
	}
}
