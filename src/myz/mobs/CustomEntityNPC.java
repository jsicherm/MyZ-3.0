/**
 * 
 */
package myz.mobs;

import java.util.UUID;

import myz.MyZ;
import myz.mobs.pathing.PathfinderGoalFollow;
import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableZombieTarget;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.Utils;
import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.Enchantment;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntitySkeleton;
import net.minecraft.server.v1_7_R1.EnumDifficulty;
import net.minecraft.server.v1_7_R1.GenericAttributes;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.Items;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_7_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveIndoors;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R1.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R1.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_7_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R1.util.UnsafeList;
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
			PathingSupport.getField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getSecondField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			PathingSupport.getSecondField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
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

		bA();
	}

	@Override
	protected void aD() {
		super.aD();
		getAttributeInstance(GenericAttributes.e).setValue(Configuration.getNPCDamage());
	}

	public NPCType getType() {
		return type;
	}

	@Override
	protected String t() {
		return "mob.villager.idle";
	}

	@Override
	protected String aT() {
		return "mob.villager.hit"; // random.classic_hurt
	}

	@Override
	protected String aU() {
		return "random.classic_hurt";
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
		makeSound("step.grass", 0.15F, 1.0F);
	}

	@Override
	protected ItemStack getRareDrop(int i) {
		int r = random.nextInt(4);
		ItemStack item;
		Potion potion;
		switch (type) {
		case ENEMY_ARCHER:
		case FRIEND_ARCHER:
			switch (r) {
			case 0:
				return new ItemStack(Items.ARROW, random.nextInt(5) + 1);
			case 1:
				return new ItemStack(Items.CAKE, 1);
			case 2:
				return new ItemStack(Items.POTATO, 1);
			case 3:
				potion = new Potion(PotionType.INSTANT_HEAL);
				return CraftItemStack.asNMSCopy(potion.toItemStack(1));
			}
			break;
		case ENEMY_SWORDSMAN:
		case FRIEND_SWORDSMAN:
			switch (r) {
			case 0:
				return new ItemStack(Items.COOKIE, random.nextInt(5) + 1);
			case 1:
				item = new ItemStack(Items.STONE_SWORD, 1);
				if (random.nextBoolean())
					item.addEnchantment(Enchantment.DAMAGE_UNDEAD, 0);
				return item;
			case 2:
				item = new ItemStack(Items.WOOD_SWORD, 1);
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
				return new ItemStack(Items.POTION, random.nextInt(2) + 1);
			case 1:
				return new ItemStack(Items.GLASS_BOTTLE, 1);
			case 2:
				return new ItemStack(Items.APPLE, 1);
			case 3:
				return new ItemStack(Items.BOWL, 1);
			}
			break;
		}
		return new ItemStack(Items.POTION, 1);
	}

	@Override
	protected void bA() {
		int i = random.nextInt(5);

		switch (type) {
		case ENEMY_ARCHER:
		case FRIEND_ARCHER:
			setEquipment(0, new ItemStack(Items.BOW, 1));
			switch (i) {
			case 0:
				setEquipment(4, new ItemStack(Items.CHAINMAIL_HELMET, 1));
				break;
			case 1:
				setEquipment(2, new ItemStack(Items.IRON_LEGGINGS, 1));
				break;
			case 2:
				setEquipment(1, new ItemStack(Items.GOLD_BOOTS, 1));
				break;
			case 3:
				setEquipment(1, new ItemStack(Items.GOLD_BOOTS, 1));
				setEquipment(3, new ItemStack(Items.LEATHER_CHESTPLATE, 1));
				break;
			}
			break;
		case ENEMY_SWORDSMAN:
		case FRIEND_SWORDSMAN:
			switch (i) {
			case 0:
				setEquipment(0, new ItemStack(Items.STONE_SWORD, 1));
				setEquipment(4, new ItemStack(Items.CHAINMAIL_HELMET, 1));
				break;
			case 1:
				setEquipment(0, new ItemStack(Items.IRON_SWORD, 1));
				setEquipment(2, new ItemStack(Items.IRON_LEGGINGS, 1));
				break;
			case 2:
				setEquipment(0, new ItemStack(Items.STONE_SWORD, 1));
				setEquipment(3, new ItemStack(Items.GOLD_CHESTPLATE, 1));
				break;
			case 3:
				setEquipment(0, new ItemStack(Items.WOOD_SWORD, 1));
				setEquipment(3, new ItemStack(Items.CHAINMAIL_CHESTPLATE, 1));
				setEquipment(2, new ItemStack(Items.CHAINMAIL_LEGGINGS, 1));
				break;
			default:
				setEquipment(0, new ItemStack(Items.STONE_SWORD, 1));
				break;
			}
			break;
		case ENEMY_WANDERER:
		case FRIEND_WANDERER:
			switch (i) {
			case 0:
				setEquipment(0, new ItemStack(Items.POTION, 1));
				break;
			case 1:
				setEquipment(0, new ItemStack(Items.GLASS_BOTTLE, 1));
				break;
			case 2:
				setEquipment(0, new ItemStack(Items.WOOD_SWORD, 1));
				setEquipment(4, new ItemStack(Items.LEATHER_HELMET, 1));
				break;
			case 3:
				setEquipment(0, new ItemStack(Items.ROTTEN_FLESH, 1));
				break;
			case 4:
				setEquipment(0, new ItemStack(Items.STONE_SPADE, 1));
				break;
			}
			break;
		}
	}

	/*@Override
	public boolean m(Entity entity) {
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getNPCDamage() * (isBaby() ? 0.5f : 1f));
	}*/

	@Override
	protected Entity findTarget() {
		Entity entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}

	@Override
	public void die() {
		destroySelf();
	}

	@Override
	public void die(DamageSource source) {
		destroySelf();
	}

	/**
	 * Destroy our packet. Did someone say GARBAGE CLEANUP? Hopefully.
	 */
	private void destroySelf() {
		final UUID uid = getBukkitEntity().getUniqueId();
		int a = 0;

		if (Utils.packets != null)
			for (Object packet : Utils.packets.keySet())
				if (Utils.packets.get(packet).getUUID().equals(uid)) {
					Utils.packets.remove(packet);
					try {
						a = (Integer) Utils.getPrivateField(packet, "a");
					} catch (Exception exc) {
						Messenger.sendConsoleMessage("&4PacketPlayOutNamedEntitySpawn issue!");
					}
					break;
				}

		final int A = a;

		// Send a destroy packet 3 seconds later (give entity time to do death
		// animation).
		if (MyZ.instance.isEnabled())
			MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
				@Override
				public void run() {
					if (A == 0)
						return;
					PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(A);
					Utils.distributePacket(getBukkitEntity().getWorld(), packet);
				}
			}, 20 * 3);
	}

	@Override
	public Item getLoot() {
		return null;
	}

	@Override
	protected void dropDeathLoot(boolean flag, int i) {
	}

	@Override
	public boolean canSpawn() {
		return world.difficulty != EnumDifficulty.PEACEFUL && world.b(boundingBox) && world.getCubes(this, boundingBox).isEmpty()
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
