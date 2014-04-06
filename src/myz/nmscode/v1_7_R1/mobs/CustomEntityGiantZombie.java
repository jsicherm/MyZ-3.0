/**
 * 
 */
package myz.nmscode.v1_7_R1.mobs;

import java.util.List;
import java.util.UUID;

import myz.nmscode.compat.CustomMob;
import myz.nmscode.v1_7_R1.pathfinders.PathfinderGoalLookAtTarget;
import myz.nmscode.v1_7_R1.pathfinders.PathfinderGoalNearestAttackableZombieTarget;
import myz.nmscode.v1_7_R1.pathfinders.PathfinderGoalZombieAttack;
import myz.nmscode.v1_7_R1.pathfinders.Support;
import myz.support.interfacing.Configuration;
import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityGiantZombie;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.GenericAttributes;
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
	
	public LivingEntity getEntity() {
		return (LivingEntity) getBukkitEntity();
	}
	
	public UUID getUID() {
		return getUniqueID();
	}
	
	public Object getWorld() {
		return world;
	}

	public void setInventory(List<ItemStack> inventory) {
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
		makeSound("mob.zombie.step", 0.15F, 1.0F);
	}

	@Override
	protected void aD() {
		super.aD();
		getAttributeInstance(GenericAttributes.e).setValue((Double) Configuration.getConfig("mobs.giant.damage"));
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
	protected Entity findTarget() {
		if (getGoalTarget() != null)
			return getGoalTarget();
		EntityHuman entityhuman = Support.findNearbyVulnerablePlayer(this);

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
}
