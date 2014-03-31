/**
 * 
 */
package myz.mobs;

import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathfinderGoalWalkTo;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import myz.support.interfacing.Configuration;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityPigZombie;
import net.minecraft.server.v1_7_R1.EntitySkeleton;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.EnumDifficulty;
import net.minecraft.server.v1_7_R1.GenericAttributes;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.Items;
import net.minecraft.server.v1_7_R1.PathfinderGoal;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;

/**
 * @author Jordan
 * 
 */
public class CustomEntityPigZombie extends EntityPigZombie {

	private int priority = 0;

	public CustomEntityPigZombie(World world) {
		super(world);

		populateGoals();
	}

	public void addPather(Location to, float speed) {
		goalSelector.a(4, new PathfinderGoalWalkTo(this, to, speed));
	}

	@Override
	public boolean canSpawn() {
		return world.difficulty != EnumDifficulty.PEACEFUL && world.b(boundingBox) && world.getCubes(this, boundingBox).isEmpty()
				&& !world.containsLiquid(boundingBox);
	}

	public void cleanPather(PathfinderGoal goal) {
		populateGoals();
		priority = 0;
	}

	public void populateGoals() {
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
		goalSelector.a(2, new PathfinderGoalZombieAttack(this, EntityHuman.class, (Double) Configuration.getConfig("mobs.pigman.speed")
				* (isBaby() ? 0.4 : 1), false));
		goalSelector.a(3, new PathfinderGoalZombieAttack(this, EntityVillager.class, (Double) Configuration.getConfig("mobs.pigman.speed")
				* (isBaby() ? 0.4 : 1), true));
		goalSelector.a(3, new PathfinderGoalZombieAttack(this, EntitySkeleton.class, (Double) Configuration.getConfig("mobs.pigman.speed")
				* (isBaby() ? 0.4 : 1), true));
		goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
		goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
		goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 8.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityHuman.class, 0, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityVillager.class, 0, false));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntitySkeleton.class, 0, false));

		bA();
	}

	public void see(Location location, int priority) {
		if (priority < this.priority)
			return;
		if (random.nextInt(priority + 1) >= 1 && getGoalTarget() == null || priority > 1) {
			setGoalTarget(null);
			target = null;
			double dub = (Double) Configuration.getConfig("mobs.pigman.speed");
			addPather(location, (float) dub * (isBaby() ? 0.4f : 1f));
		}
	}

	@Override
	protected void aD() {
		super.aD();
		getAttributeInstance(GenericAttributes.e).setValue((Double) Configuration.getConfig("mobs.pigman.damage") * (isBaby() ? 0.75 : 1));
	}

	@Override
	protected void bA() {
		setEquipment(0, new ItemStack(Items.STONE_SWORD));
	}

	@Override
	protected Entity findTarget() {
		EntityHuman entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}
}
