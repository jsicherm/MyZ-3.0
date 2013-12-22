/**
 * 
 */
package myz.mobs;

import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import myz.Support.Configuration;
import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityGiantZombie;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalLeapAtTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;

/**
 * @author Jordan
 * 
 */
public class CustomEntityGiantZombie extends EntityGiantZombie {

	public CustomEntityGiantZombie(World world) {
		super(world);

		try {
			PathingSupport.getField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getSecondField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getSecondField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		getNavigation().b(true);
		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(2, new PathfinderGoalZombieAttack(this, EntityHuman.class, Configuration.getGiantSpeed(), false));
		goalSelector.a(3, new PathfinderGoalZombieAttack(this, EntityVillager.class, Configuration.getGiantSpeed(), true));
		goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 8.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		goalSelector.a(1, new PathfinderGoalLeapAtTarget(this, 1.3F));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityHuman.class, 0, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityVillager.class, 0, false));
	}

	@Override
	protected Entity findTarget() {
		if (getGoalTarget() != null) { return getGoalTarget(); }
		EntityHuman entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		if (entityhuman != null && this.o(entityhuman)) {
			setGoalTarget(entityhuman);
			return entityhuman;
		}
		return null;
	}

	@Override
	protected String t() {
		return "mob.zombie.say";
	}

	@Override
	protected String aT() {
		return "mob.zombie.hurt";
	}

	@Override
	protected String aU() {
		return "mob.zombie.death";
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
		this.makeSound("mob.zombie.step", 0.15F, 1.0F);
	}

	/*@Override
	protected void a(Entity entity, float f) {
		if (this.attackTicks <= 0 && f < 2.0F && entity.boundingBox.e > this.boundingBox.b && entity.boundingBox.b < this.boundingBox.e) {
			this.attackTicks = 20;
			this.m(entity);
		}
	}*/

	/*@Override
	public boolean m(Entity entity) {
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getGiantDamage());
	}*/
}
