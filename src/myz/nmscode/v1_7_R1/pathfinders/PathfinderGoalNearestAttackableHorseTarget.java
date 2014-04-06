/**
 * 
 */
package myz.nmscode.v1_7_R1.pathfinders;

import java.util.Collections;
import java.util.List;

import myz.MyZ;
import net.minecraft.server.v1_7_R1.DistanceComparator;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityHorse;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.IEntitySelector;
import net.minecraft.server.v1_7_R1.PathfinderGoalTarget;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalNearestAttackableHorseTarget extends PathfinderGoalTarget {

	private final Class<? extends EntityLiving> a;
	private final int b;
	private final EntityHorse cc;
	private final DistanceComparator e;
	private final IEntitySelector f;

	// private EntityLiving g;

	class EntitySelectorNearestAttackableHorseTarget implements IEntitySelector {

		final IEntitySelector c;

		final PathfinderGoalNearestAttackableHorseTarget d;

		EntitySelectorNearestAttackableHorseTarget(PathfinderGoalNearestAttackableHorseTarget pathfindergoalnearestattackabletarget,
				IEntitySelector ientityselector) {
			d = pathfindergoalnearestattackabletarget;
			c = ientityselector;
		}

		@Override
		public boolean a(Entity entity) {
			if (!(entity instanceof EntityLiving) || entity == d.cc)
				return false;

			// Attack if we are an undead horse.
			if (((Horse) d.cc.getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
					|| ((Horse) d.cc.getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
				return c != null && !c.a(entity) ? false : d.a((EntityLiving) entity, false);

			if (d.cc.getOwnerName() != null && !d.cc.getOwnerName().isEmpty()) {
				if (!MyZ.instance.isBandit(MyZ.instance.getUID(d.cc.getOwnerName())))
					return false;
				if (entity instanceof EntityHuman)
					if (d.cc.getOwnerName().equals(((EntityHuman) entity).getName())
							|| MyZ.instance.isFriend(MyZ.instance.getUID(d.cc.getOwnerName()),
									MyZ.instance.getUID(((EntityHuman) entity).getName())))
						return false;
			}
			return c != null && !c.a(entity) ? false : d.a((EntityLiving) entity, false);
		}
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityHorse EntityHorse, Class<? extends EntityLiving> oclass, int i, boolean flag) {
		this(EntityHorse, oclass, i, flag, false);
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityHorse EntityHorse, Class<? extends EntityLiving> oclass, int i, boolean flag,
			boolean flag1) {
		this(EntityHorse, oclass, i, flag, flag1, (IEntitySelector) null);
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityHorse EntityHorse, Class<? extends EntityLiving> oclass, int i, boolean flag,
			boolean flag1, IEntitySelector ientityselector) {
		super(EntityHorse, flag, flag1);
		a = oclass;
		b = i;
		cc = EntityHorse;
		e = new DistanceComparator(EntityHorse);
		this.a(1);
		f = new EntitySelectorNearestAttackableHorseTarget(this, ientityselector);
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
