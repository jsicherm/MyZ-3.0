/**
 * 
 */
package myz.nmscode.v1_7_R2.mobs;

import java.util.List;
import java.util.UUID;

import myz.nmscode.compat.CustomMob;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalLookAtTarget;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalNearestAttackableZombieTarget;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalZombieAttack;
import myz.nmscode.v1_7_R2.pathfinders.Support;
import myz.support.interfacing.Configuration;
import net.minecraft.server.v1_7_R2.Block;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityGiantZombie;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.EntityVillager;
import net.minecraft.server.v1_7_R2.GenericAttributes;
import net.minecraft.server.v1_7_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R2.PathfinderGoalLeapAtTarget;
import net.minecraft.server.v1_7_R2.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R2.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.craftbukkit.v1_7_R2.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class CustomEntityGiantZombie extends EntityGiantZombie implements CustomMob {

	public CustomEntityGiantZombie(World world) {
		super(world);

		try {
			Support.getField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getSecondField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getSecondField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		getNavigation().b(true);
		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(2, new PathfinderGoalZombieAttack(this, EntityHuman.class, (Double) Configuration.getConfig("mobs.giant.speed"),
				false));
		goalSelector.a(3, new PathfinderGoalZombieAttack(this, EntityVillager.class, (Double) Configuration.getConfig("mobs.giant.speed"),
				true));
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
	public LivingEntity getEntity() {
		return (LivingEntity) getBukkitEntity();
	}

	@Override
	public UUID getUID() {
		return getUniqueID();
	}

	@Override
	public Object getWorld() {
		return world;
	}

	@Override
	public void setInventory(List<ItemStack> inventory) {
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
		makeSound("mob.zombie.step", 0.15F, 1.0F);
	}

	@Override
	protected void aC() {
		super.aC();
		getAttributeInstance(GenericAttributes.e).setValue((Double) Configuration.getConfig("mobs.giant.damage"));
	}

	@Override
	protected String aS() {
		return "mob.zombie.hurt";
	}

	@Override
	protected String aT() {
		return "mob.zombie.death";
	}

	@Override
	protected Entity findTarget() {
		EntityHuman entityhuman = Support.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.p(entityhuman) ? entityhuman : null;
	}

	@Override
	protected String t() {
		return "mob.zombie.say";
	}
}
