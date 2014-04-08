/**
 * 
 */
package myz.nmscode.v1_7_R2.pathfinders;

import java.util.Collections;
import java.util.List;

import myz.MyZ;
import net.minecraft.server.v1_7_R2.DistanceComparator;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityCreature;
import net.minecraft.server.v1_7_R2.EntityHorse;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.IEntitySelector;
import net.minecraft.server.v1_7_R2.PathfinderGoalTarget;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;

/**
 * @author Jordan
 * 
 */
public class PathfinderGoalNearestAttackableHorseTarget extends PathfinderGoalTarget {

	private final Class a;
	private final int b;
	private final DistanceComparator e;
	private final IEntitySelector f;
	private EntityLiving g;

	private EntityHorse creature;

	class EntitySelectorNearestAttackableHorseTarget implements IEntitySelector {

		final IEntitySelector d;
		final PathfinderGoalNearestAttackableHorseTarget e;

		EntitySelectorNearestAttackableHorseTarget(PathfinderGoalNearestAttackableHorseTarget pathfindergoalnearestattackabletarget,
				IEntitySelector ientityselector) {
			e = pathfindergoalnearestattackabletarget;
			d = ientityselector;
		}

		@Override
		public boolean a(Entity entity) {
			// Attack if we are an undead horse.
			if (((Horse) e.creature.getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
					|| ((Horse) e.creature.getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
				return d != null && !d.a(entity) ? false : e.a((EntityLiving) entity, false);

			if (e.creature.getOwnerName() != null && !e.creature.getOwnerName().isEmpty()) {
				if (!MyZ.instance.isBandit(MyZ.instance.getUID(e.creature.getOwnerName())))
					return false;
				if (entity instanceof EntityHuman)
					if (e.creature.getOwnerName().equals(((EntityHuman) entity).getName())
							|| MyZ.instance.isFriend(MyZ.instance.getUID(e.creature.getOwnerName()),
									MyZ.instance.getUID(((EntityHuman) entity).getName())))
						return false;
			}
			return !(entity instanceof EntityLiving) ? false : d != null && !d.a(entity) ? false : entity.isInvulnerable() ? false : e.a(
					(EntityLiving) entity, false);
		}
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag) {
		this(entitycreature, oclass, i, flag, false);
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1) {
		this(entitycreature, oclass, i, flag, flag1, (IEntitySelector) null);
	}

	public PathfinderGoalNearestAttackableHorseTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1,
			IEntitySelector ientityselector) {
		super(entitycreature, flag, flag1);
		creature = (EntityHorse) entitycreature;
		a = oclass;
		b = i;
		e = new DistanceComparator(entitycreature);
		this.a(1);
		f = new EntitySelectorNearestAttackableHorseTarget(this, ientityselector);
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
