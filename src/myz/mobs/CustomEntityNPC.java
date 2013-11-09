/**
 * 
 */
package myz.mobs;

import java.lang.reflect.Field;

import myz.Support.Configuration;
import myz.mobs.pathing.PathfinderGoalFollow;
import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.Enchantment;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntitySkeleton;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.PathfinderGoalArrowAttack;
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

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_6_R3.util.UnsafeList;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * @author Jordan
 * 
 */
public class CustomEntityNPC extends EntitySkeleton implements SmartEntity {

	private final NPCType type;

	public CustomEntityNPC(World world) {
		this(world, NPCType.getRandom());
	}

	/**
	 * @param world
	 */
	public CustomEntityNPC(World world, NPCType type) {
		super(world);
		this.type = type;

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
		goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
		goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
		goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 8.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));

		switch (type) {
		case ENEMY_ARCHER:
		case ENEMY_SWORDSMAN:
		case ENEMY_WANDERER:
			if (type != NPCType.ENEMY_ARCHER) {
				goalSelector.a(2, new PathfinderGoalZombieAttack(this, EntityHuman.class, Configuration.getNPCSpeed(), false));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityGiantZombie.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityPigZombie.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityHorse.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityZombie.class, Configuration.getNPCSpeed(), true));
			} else
				goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0, 20, 60, 15f));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, EntityHuman.class, 0, true));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityGiantZombie.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityPigZombie.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityHorse.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityZombie.class, 0, false));
			break;
		case FRIEND_ARCHER:
		case FRIEND_SWORDSMAN:
		case FRIEND_WANDERER:
			if (type != NPCType.FRIEND_ARCHER) {
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityGiantZombie.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityPigZombie.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityHorse.class, Configuration.getNPCSpeed(), true));
				goalSelector.a(3, new PathfinderGoalZombieAttack(this, CustomEntityZombie.class, Configuration.getNPCSpeed(), true));
			} else
				goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0, 20, 60, 15f));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityGiantZombie.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityPigZombie.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityHorse.class, 0, false));
			targetSelector.a(2, new PathfinderGoalNearestAttackableZombieTarget(this, CustomEntityZombie.class, 0, false));
			goalSelector.a(5, new PathfinderGoalFollow(this, 1.2, false));
			break;
		}

		bw();
	}

	public NPCType getType() {
		return type;
	}

	@Override
	protected String r() {
		return "random.breathe";
	}

	@Override
	protected String aO() {
		return "damage.hit"; // random.classic_hurt
	}

	@Override
	protected String aP() {
		return "random.classic_hurt";
	}

	@Override
	protected void a(int i, int j, int k, int l) {
		makeSound("step.grass", 0.15F, 1.0F);
	}

	@Override
	protected ItemStack l(int i) {
		int r = random.nextInt(4);
		ItemStack item;
		Potion potion;
		switch (type) {
		case ENEMY_ARCHER:
		case FRIEND_ARCHER:
			switch (r) {
			case 0:
				return new ItemStack(Item.ARROW, random.nextInt(5) + 1);
			case 1:
				return new ItemStack(Item.CAKE, 1);
			case 2:
				return new ItemStack(Item.POTATO, 1);
			case 3:
				potion = new Potion(PotionType.INSTANT_HEAL);
				return CraftItemStack.asNMSCopy(potion.toItemStack(1));
			}
			break;
		case ENEMY_SWORDSMAN:
		case FRIEND_SWORDSMAN:
			switch (r) {
			case 0:
				return new ItemStack(Item.COOKIE, random.nextInt(5) + 1);
			case 1:
				item = new ItemStack(Item.STONE_SWORD, 1);
				if (random.nextBoolean())
					item.addEnchantment(Enchantment.DAMAGE_UNDEAD, 0);
				return item;
			case 2:
				item = new ItemStack(Item.WOOD_SWORD, 1);
				if (random.nextBoolean())
					item.addEnchantment(Enchantment.DAMAGE_UNDEAD, 0);
				return item;
			case 3:
				potion = new Potion(PotionType.INSTANT_HEAL);
				return CraftItemStack.asNMSCopy(potion.toItemStack(1));
			}
			break;
		case ENEMY_WANDERER:
		case FRIEND_WANDERER:
			switch (r) {
			case 0:
				return new ItemStack(Item.POTION, random.nextInt(2) + 1);
			case 1:
				return new ItemStack(Item.GLASS_BOTTLE, 1);
			case 2:
				return new ItemStack(Item.APPLE, 1);
			case 3:
				return new ItemStack(Item.BOWL, 1);
			}
			break;
		}
		return new ItemStack(Item.POTION, 1);
	}

	@Override
	protected void bw() {
		int i = random.nextInt(5);

		switch (type) {
		case ENEMY_ARCHER:
		case FRIEND_ARCHER:
			setEquipment(0, new ItemStack(Item.BOW, 1));
			switch (i) {
			case 0:
				setEquipment(4, new ItemStack(Item.CHAINMAIL_HELMET, 1));
				break;
			case 1:
				setEquipment(2, new ItemStack(Item.IRON_LEGGINGS, 1));
				break;
			case 2:
				setEquipment(1, new ItemStack(Item.GOLD_BOOTS, 1));
				break;
			case 3:
				setEquipment(1, new ItemStack(Item.GOLD_BOOTS, 1));
				setEquipment(3, new ItemStack(Item.LEATHER_CHESTPLATE, 1));
				break;
			}
			break;
		case ENEMY_SWORDSMAN:
		case FRIEND_SWORDSMAN:
			switch (i) {
			case 0:
				setEquipment(0, new ItemStack(Item.STONE_SWORD, 1));
				setEquipment(4, new ItemStack(Item.CHAINMAIL_HELMET, 1));
				break;
			case 1:
				setEquipment(0, new ItemStack(Item.IRON_SWORD, 1));
				setEquipment(2, new ItemStack(Item.IRON_LEGGINGS, 1));
				break;
			case 2:
				setEquipment(0, new ItemStack(Item.STONE_SWORD, 1));
				setEquipment(3, new ItemStack(Item.GOLD_CHESTPLATE, 1));
				break;
			case 3:
				setEquipment(0, new ItemStack(Item.WOOD_SWORD, 1));
				setEquipment(3, new ItemStack(Item.CHAINMAIL_CHESTPLATE, 1));
				setEquipment(2, new ItemStack(Item.CHAINMAIL_LEGGINGS, 1));
				break;
			default:
				setEquipment(0, new ItemStack(Item.STONE_SWORD, 1));
				break;
			}
			break;
		case ENEMY_WANDERER:
		case FRIEND_WANDERER:
			switch (i) {
			case 0:
				setEquipment(0, new ItemStack(Item.POTION, 1));
				break;
			case 1:
				setEquipment(0, new ItemStack(Item.GLASS_BOTTLE, 1));
				break;
			case 2:
				setEquipment(0, new ItemStack(Item.WOOD_SWORD, 1));
				setEquipment(4, new ItemStack(Item.LEATHER_HELMET, 1));
				break;
			case 3:
				setEquipment(0, new ItemStack(Item.ROTTEN_FLESH, 1));
				break;
			case 4:
				setEquipment(0, new ItemStack(Item.STONE_SPADE, 1));
				break;
			}
			break;
		}
	}

	@Override
	public boolean m(Entity entity) {
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getNPCDamage() * (isBaby() ? 0.5f : 1f));
	}

	@Override
	protected Entity findTarget() {
		Entity entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}

	@Override
	public int getLootId() {
		return l(3).id;
	}

	@Override
	protected void dropDeathLoot(boolean flag, int i) {
		int j = this.getLootId();

		if (j > 0) {
			int k = this.random.nextInt(3);

			if (i > 0) {
				k += this.random.nextInt(i + 1);
			}

			for (int l = 0; l < k; ++l) {
				this.b(j, 1);
			}
		}
	}

	@Override
	public boolean canSpawn() {
		return world.difficulty > 0 && world.b(boundingBox) && world.getCubes(this, boundingBox).isEmpty()
				&& !world.containsLiquid(boundingBox);
	}

	/* (non-Javadoc)
	 * @see myz.mobs.SmartEntity#see(org.bukkit.Location, int)
	 */
	@Override
	public void see(Location location, int priority) {
		if (random.nextInt(priority + 1) >= 1) {
			setGoalTarget(null);
			target = null;
			PathingSupport.setTarget(this, location, Configuration.getNPCSpeed());
		}
	}
}
