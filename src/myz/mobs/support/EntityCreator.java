/**
 * 
 */
package myz.mobs.support;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import myz.MyZ;
import myz.Support.Messenger;
import myz.Utilities.Utilities;
import myz.mobs.CustomEntityGiantZombie;
import myz.mobs.CustomEntityNPC;
import myz.mobs.CustomEntityPigZombie;
import myz.mobs.NPCType;
import net.minecraft.server.v1_7_R1.DataWatcher;
import net.minecraft.server.v1_7_R1.EntityHorse;
import net.minecraft.server.v1_7_R1.EntityPigZombie;
import net.minecraft.server.v1_7_R1.EntitySkeleton;
import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.EntityZombie;
import net.minecraft.server.v1_7_R1.GroupDataEntity;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.Items;
import net.minecraft.server.v1_7_R1.MerchantRecipe;
import net.minecraft.server.v1_7_R1.MerchantRecipeList;
import net.minecraft.server.v1_7_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R1.World;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * @author Jordan
 * 
 */
public class EntityCreator {

	private static final Random random = new Random();

	/**
	 * Create an entity using NMS in order to bypass custom entity creation.
	 * 
	 * @param inLocation
	 *            The location to spawn at.
	 * @param type
	 *            The EntityType.
	 * @param reason
	 *            The spawning reason.
	 */
	public static void create(Location inLocation, EntityType type, SpawnReason reason) {
		create(inLocation, type, reason, false);
	}

	/**
	 * Create an entity using NMS in order to either bypass custom entity
	 * creation or promote it.
	 * 
	 * @param inLocation
	 *            The location to spawn at.
	 * @param type
	 *            The EntityType.
	 * @param reason
	 *            The spawning reason.
	 * @param toggle
	 *            Whether or not to spawn a custom entity (ONLY FOR PIGMEN)
	 */
	public static void create(Location inLocation, EntityType type, SpawnReason reason, boolean toggle) {
		World world = ((CraftWorld) inLocation.getWorld()).getHandle();
		switch (type) {
		case HORSE:
			EntityHorse horse = new EntityHorse(world);
			horse.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
			if (horse.canSpawn()) {
				horse.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
				world.addEntity(horse, reason);
			}
			break;
		case PIG_ZOMBIE:
			if (!toggle) {
				EntityPigZombie pigman = new EntityPigZombie(world);
				pigman.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
				if (pigman.canSpawn()) {
					pigman.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
					world.addEntity(pigman, reason);
				}
			} else {
				CustomEntityPigZombie pigman = new CustomEntityPigZombie(world);
				pigman.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
				pigman.setBaby(random.nextInt(20) < 3);
				world.addEntity(pigman, SpawnReason.NATURAL);
				((PigZombie) pigman.getBukkitEntity()).setAngry(true);
			}
			break;
		case ZOMBIE:
			EntityZombie zombie = new EntityZombie(world);
			zombie.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
			if (zombie.canSpawn()) {
				zombie.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
				world.addEntity(zombie, reason);
			}
			break;
		case SKELETON:
			EntitySkeleton skeleton = new EntitySkeleton(world);
			skeleton.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
			if (skeleton.canSpawn()) {
				skeleton.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
				skeleton.a((GroupDataEntity) null);
				skeleton.bT();
				world.addEntity(skeleton, reason);
			}
			break;
		case GIANT:
			CustomEntityGiantZombie giant = new CustomEntityGiantZombie(world);
			giant.setPosition(inLocation.getX(), inLocation.getY(), inLocation.getZ());
			world.addEntity(giant, SpawnReason.CUSTOM);
			break;
		default:
			break;
		}
	}

	/**
	 * Spawn an NPC and disguise it.
	 * 
	 * @param location
	 *            The location to spawn at.
	 */
	public static void disguiseNPC(Location location) {
		World world = ((CraftWorld) location.getWorld()).getHandle();
		NPCType npctype;
		CustomEntityNPC npc = new CustomEntityNPC(world, npctype = NPCType.getRandom());
		npc.setPosition(location.getX(), location.getY(), location.getZ());

		if (world.addEntity(npc, SpawnReason.CUSTOM)) {
			if (MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") == null
					|| !MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled()) {
				PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
				try {
					Utilities.setPrivateField(packet, "a", npc.getBukkitEntity().getEntityId());
					String name = getRandomName(npctype);
					GameProfile profile = new GameProfile(name, name);
					Utilities.setPrivateField(packet, "b", profile);
					Utilities.setPrivateField(packet, "c", (int) location.getX() * 32);
					Utilities.setPrivateField(packet, "d", (int) location.getY() * 32);
					Utilities.setPrivateField(packet, "e", (int) location.getZ() * 32);
					Utilities.setPrivateField(packet, "f", (byte) 0);
					Utilities.setPrivateField(packet, "g", (byte) 0);
					Utilities.setPrivateField(packet, "h", npc.getEquipment(0) != null ? Item.b(npc.getEquipment(0).getItem()) : 0);
				} catch (Exception exc) {
					Messenger.sendConsoleMessage("&4PacketPlayOutEntitySpawn issue!");
					exc.printStackTrace();
					return;
				}

				DataWatcher datawatcher = new DataWatcher(npc);
				datawatcher.a(0, (Object) (byte) 0);
				datawatcher.a(1, (Object) (short) 0);
				datawatcher.a(8, (Object) (byte) 0);

				try {
					Field f = packet.getClass().getDeclaredField("i");
					f.setAccessible(true);
					f.set(packet, datawatcher);
					Utilities.saveAndDistributePacket(packet, npc.getBukkitEntity());
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			} else if (MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") != null
					&& MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled())
				myz.Utilities.LibsDisguiseUtilities.becomeNPC((LivingEntity) npc.getBukkitEntity(), getRandomName(npctype));

			((Skeleton) npc.getBukkitEntity()).setRemoveWhenFarAway(true);
			((Skeleton) npc.getBukkitEntity()).getEquipment().setBootsDropChance(1);
			((Skeleton) npc.getBukkitEntity()).getEquipment().setLeggingsDropChance(1);
			((Skeleton) npc.getBukkitEntity()).getEquipment().setChestplateDropChance(1);
			((Skeleton) npc.getBukkitEntity()).getEquipment().setHelmetDropChance(1);
			((Skeleton) npc.getBukkitEntity()).getEquipment().setItemInHandDropChance(1);
		}
	}

	/**
	 * Get a random name for an NPC.
	 * 
	 * @return The name.
	 */
	private static String getRandomName(NPCType type) {
		List<String> possibilities = null;
		switch (type) {
		case ENEMY_ARCHER:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.archer.enemy");
			break;
		case ENEMY_SWORDSMAN:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.swordsman.enemy");
			break;
		case ENEMY_WANDERER:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.wanderer.enemy");
			break;
		case FRIEND_ARCHER:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.archer.friendly");
			break;
		case FRIEND_SWORDSMAN:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.swordsman.friendly");
			break;
		default:
			possibilities = MyZ.instance.getLocalizableConfig().getStringList("localizable.npc_names.wanderer.friendly");
			break;
		}
		return possibilities == null ? "Notch" : possibilities.get(random.nextInt(possibilities.size() == 0 ? 1 : possibilities.size()));
	}

	/**
	 * Override the trades of a villager. Assumes the provided entity is a
	 * villager.
	 * 
	 * @param entity
	 *            The villager to override.
	 */
	public static void overrideVillager(LivingEntity entity) {
		EntityVillager villager = ((CraftVillager) entity).getHandle();
		try {
			Field recipes = villager.getClass().getDeclaredField("bu");
			recipes.setAccessible(true);
			MerchantRecipeList list = new MerchantRecipeList();
			Potion health = new Potion(PotionType.INSTANT_HEAL);
			Potion strength = new Potion(PotionType.STRENGTH);
			Potion regen = new Potion(PotionType.REGEN);
			org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.STONE_SWORD);
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);

			switch (((Villager) entity).getProfession()) {
			case BLACKSMITH:
				list.a(new MerchantRecipe(new ItemStack(Items.IRON_SWORD, 1), CraftItemStack.asNMSCopy(sword)));
				list.a(new MerchantRecipe(new ItemStack(Items.GOLD_NUGGET, 3), new ItemStack(Items.GOLD_INGOT, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(Items.IRON_INGOT, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.IRON_INGOT, 10), new ItemStack(Items.DIAMOND, 1)));
				break;
			case BUTCHER:
				list.a(new MerchantRecipe(new ItemStack(Items.ROTTEN_FLESH, 3), new ItemStack(Items.RAW_BEEF, 3)));
				list.a(new MerchantRecipe(new ItemStack(Items.RAW_BEEF, 5), new ItemStack(Items.LEATHER, 2)));
				list.a(new MerchantRecipe(new ItemStack(Items.SHEARS, 1), new ItemStack(Items.SADDLE, 1)));
				break;
			case FARMER:
				list.a(new MerchantRecipe(new ItemStack(Items.GOLD_NUGGET, 1), new ItemStack(Items.MILK_BUCKET, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.LEATHER, 3), new ItemStack(Items.LEASH, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.COOKED_BEEF, 3), new ItemStack(Items.EGG, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.MILK_BUCKET, 2), new ItemStack(Items.EGG, 1), new ItemStack(Items.CAKE, 2)));
				break;
			case LIBRARIAN:
				list.a(new MerchantRecipe(new ItemStack(Items.PAPER, 1), new ItemStack(Items.BOOK_AND_QUILL, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.PAPER, 3), new ItemStack(Items.LEATHER_CHESTPLATE, 1)));
				list.a(new MerchantRecipe(new ItemStack(Items.BOOK, 2), new ItemStack(Items.STICK, 3)));
				break;
			case PRIEST:
				list.a(new MerchantRecipe(new ItemStack(Items.NETHER_STAR, 1), CraftItemStack.asNMSCopy(health.toItemStack(1))));
				list.a(new MerchantRecipe(new ItemStack(Items.GOLDEN_APPLE, 2), CraftItemStack.asNMSCopy(strength.toItemStack(1))));
				list.a(new MerchantRecipe(CraftItemStack.asNMSCopy(health.toItemStack(1)),
						CraftItemStack.asNMSCopy(strength.toItemStack(1)), CraftItemStack.asNMSCopy(regen.toItemStack(1))));
				break;
			default:
				break;

			}
			recipes.set(villager, list);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
