/**
 * 
 */
package myz.mobs;

import java.lang.reflect.Field;

import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import myz.Support.Configuration;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityPigZombie;
import net.minecraft.server.v1_6_R3.EntityVillager;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_6_R3.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_6_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_6_R3.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R3.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_6_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.util.UnsafeList;

/**
 * @author Jordan
 * 
 */
public class CustomEntityPigZombie extends EntityPigZombie implements SmartEntity {

	public CustomEntityPigZombie(World world) {
		super(world);

		try {
			Field field = PathfinderGoalSelector.class.getDeclaredField("a");
			field.setAccessible(true);

			field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		getNavigation().b(true);
		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(2, new PathfinderGoalZombieAttack(this, EntityHuman.class, Configuration.getPigmanSpeed(), false));
		goalSelector.a(3, new PathfinderGoalZombieAttack(this, EntityVillager.class, Configuration.getPigmanSpeed(), true));
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
	}

	@Override
	protected Entity findTarget() {
		EntityHuman entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}

	@Override
	public boolean m(Entity entity) {
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getPigmanDamage() * (isBaby() ? 0.5f : 1f));
	}

	@Override
	protected void bw() {
		setEquipment(0, new ItemStack(Item.STONE_SWORD));
	}

	@Override
	public boolean canSpawn() {
		// int i = MathHelper.floor(locX);
		// int j = MathHelper.floor(boundingBox.b);
		// int k = MathHelper.floor(locZ);

		return world.difficulty > 0 && world.b(boundingBox) && world.getCubes(this, boundingBox).isEmpty()
				&& !world.containsLiquid(boundingBox);// && this.a(i, j, k) >=
														// 0.0F;
	}

	/* (non-Javadoc)
	 * @see myz.mobs.SmartEntity#see(org.bukkit.Location, int)
	 */
	@Override
	public void see(Location location, int priority) {
		if (random.nextInt(priority + 1) >= 1) {
			setGoalTarget(null);
			target = null;
			PathingSupport.setTarget(this, location, Configuration.getZombieSpeed());
		}
	}
}
