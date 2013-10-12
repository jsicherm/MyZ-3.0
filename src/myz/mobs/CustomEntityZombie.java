/**
 * 
 */
package myz.mobs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import myz.Support.Configuration;
import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathingSupport;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityVillager;
import net.minecraft.server.v1_6_R3.EntityZombie;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_6_R3.PathfinderGoalMoveIndoors;
import net.minecraft.server.v1_6_R3.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_6_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_6_R3.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R3.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_6_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.craftbukkit.v1_6_R3.util.UnsafeList;

/**
 * @author Jordan
 * 
 */
public class CustomEntityZombie extends EntityZombie {

	protected List<org.bukkit.inventory.ItemStack> inventory = new ArrayList<org.bukkit.inventory.ItemStack>();

	public CustomEntityZombie(World world) {
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
		// goalSelector.a(2, new PathfinderGoalZombieAttack(this,
		// EntityHuman.class, Configuration.getZombieSpeed(), false));
		// goalSelector.a(3, new PathfinderGoalZombieAttack(this,
		// EntityVillager.class, Configuration.getZombieSpeed(), true));
		goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
		goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
		goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 8.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityHuman.class, 0, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityVillager.class, 0, false));
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		if (!super.damageEntity(damagesource, f)) {
			return false;
		} else {
			EntityLiving entityliving = this.getGoalTarget();

			if (entityliving == null && this.bN() instanceof EntityLiving) {
				entityliving = (EntityLiving) this.bN();
			}

			if (entityliving == null && damagesource.getEntity() instanceof EntityLiving) {
				entityliving = (EntityLiving) damagesource.getEntity();
			}

			if (entityliving != null && this.world.difficulty >= 3
					&& (double) this.random.nextFloat() < this.getAttributeInstance(bp).getValue()) {
				int i = MathHelper.floor(this.locX);
				int j = MathHelper.floor(this.locY);
				int k = MathHelper.floor(this.locZ);
				EntityZombie entityzombie = new EntityZombie(this.world);

				for (int l = 0; l < 50; ++l) {
					int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
					int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
					int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);

					if (this.world.w(i1, j1 - 1, k1) && this.world.getLightLevel(i1, j1, k1) < 10) {
						entityzombie.setPosition((double) i1, (double) j1, (double) k1);
						if (this.world.b(entityzombie.boundingBox) && this.world.getCubes(entityzombie, entityzombie.boundingBox).isEmpty()
								&& !this.world.containsLiquid(entityzombie.boundingBox)) {
							this.world.addEntity(entityzombie);
							// Prevent crazy zombie mobs.
							entityzombie.setGoalTarget(entityliving);
							// entityzombie.a((GroupDataEntity) null);
							// this.getAttributeInstance(bp).a(new
							// AttributeModifier("Zombie reinforcement caller charge",
							// -0.05000000074505806D, 0));
							// entityzombie.getAttributeInstance(bp).a(new
							// AttributeModifier("Zombie reinforcement callee charge",
							// -0.05000000074505806D, 0));
							break;
						}
					}
				}
			}

			return true;
		}
	}

	@Override
	protected ItemStack l(int i) {
		switch (random.nextInt(3)) {
		case 0:
			return new ItemStack(Item.POTION);

		case 1:
			return new ItemStack(Item.BOWL);

		default:
			return new ItemStack(Item.LEASH);
		}
	}

	@Override
	protected void bw() {
		if (random.nextFloat() < (world.difficulty == 3 ? 0.05F : 0.01F)) {
			int i = random.nextInt(3);

			if (i == 0)
				setEquipment(0, new ItemStack(Item.STONE_SWORD));
			else
				setEquipment(0, new ItemStack(Item.STONE_SPADE));
		}
	}

	@Override
	public boolean m(Entity entity) {
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getZombieDamage() * (isBaby() ? 0.5f : 1f));
	}

	@Override
	protected Entity findTarget() {
		EntityHuman entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}

	/**
	 * Set this zombie's inventory contents. Generally contains a player's
	 * inventory plus their helmet and subtract their item in hand.
	 * 
	 * @param inventory
	 *            The list of items to set.
	 */
	public void setInventory(List<org.bukkit.inventory.ItemStack> inventory) {
		this.inventory = inventory;
	}

	private void emptyInventory() {
		for (org.bukkit.inventory.ItemStack item : inventory) {
			if (item != null)
				getBukkitEntity().getWorld().dropItemNaturally(getBukkitEntity().getLocation(), item);
		}
		inventory.clear();
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		emptyInventory();
	}

	@Override
	public void die() {
		super.die();
		emptyInventory();
	}
}
