/**
 * 
 */
package myz.mobs.pathing;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_6_R3.DistanceComparator;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityCreature;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.IEntitySelector;
import net.minecraft.server.v1_6_R3.PathfinderGoalTarget;

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
	private EntityLiving g;

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
		if (b > 0 && c.aD().nextInt(b) != 0)
			return false;
		else {
			double d0 = f();
			List<EntityLiving> list = c.world.a(a, c.boundingBox.grow(d0, 4.0D, d0), f);

			Collections.sort(list, e);
			if (list.isEmpty())
				return false;
			else {
				g = list.get(0);
				return true;
			}
		}
	}

	@Override
	public void c() {
		c.setGoalTarget(g);
		super.c();
	}

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
			Entity target = PathingSupport.findNearbyVulnerablePlayer(d.cc);
			if (target != null && target.equals(entity))
				return true;
			return c != null && !c.a(entity) ? false : d.a((EntityLiving) entity, false);
		}
	}
}
