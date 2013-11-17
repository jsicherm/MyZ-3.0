/**
 * 
 */
package myz.Listeners;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Utilities.Utilities;
import myz.Utilities.WorldGuardManager;
import myz.mobs.CustomEntityGiantZombie;
import myz.mobs.CustomEntityNPC;
import myz.mobs.CustomEntityPigZombie;
import myz.mobs.NPCType;
import net.minecraft.server.v1_6_R3.DataWatcher;
import net.minecraft.server.v1_6_R3.EntityVillager;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MerchantRecipe;
import net.minecraft.server.v1_6_R3.MerchantRecipeList;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * @author Jordan
 * 
 */
public class EntitySpawn implements Listener {

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSpawn(CreatureSpawnEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getLocation().getWorld().getName()))
			return;

		// Cancel spawning inside spawn room.
		if (Configuration.isInLobby(e.getEntity().getLocation())) {
			e.setCancelled(true);
			return;
		}

		EntityType type = e.getEntityType();

		// Override mooshroom spawns with giant spawns.
		if (e.getSpawnReason() == SpawnReason.SPAWNER_EGG && e.getEntityType() == EntityType.MUSHROOM_COW) {
			World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();
			CustomEntityGiantZombie giant = new CustomEntityGiantZombie(world);
			giant.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
			world.addEntity(giant, SpawnReason.CUSTOM);
			e.setCancelled(true);
			return;
		}

		// Override villager trades.
		if (e.getEntityType() == EntityType.VILLAGER) {
			EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
			try {
				Field recipes = villager.getClass().getDeclaredField("bu");
				recipes.setAccessible(true);
				MerchantRecipeList list = new MerchantRecipeList();
				Potion health = new Potion(PotionType.INSTANT_HEAL);
				Potion strength = new Potion(PotionType.STRENGTH);
				Potion regen = new Potion(PotionType.REGEN);
				org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.STONE_SWORD);
				sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);

				switch (((Villager) e.getEntity()).getProfession()) {
				case BLACKSMITH:
					list.a(new MerchantRecipe(new ItemStack(Item.IRON_SWORD, 1), CraftItemStack.asNMSCopy(sword)));
					list.a(new MerchantRecipe(new ItemStack(Item.GOLD_NUGGET, 3), new ItemStack(Item.GOLD_INGOT, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.GOLD_INGOT, 5), new ItemStack(Item.IRON_INGOT, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.IRON_INGOT, 10), new ItemStack(Item.DIAMOND, 1)));
					break;
				case BUTCHER:
					list.a(new MerchantRecipe(new ItemStack(Item.ROTTEN_FLESH, 3), new ItemStack(Item.RAW_BEEF, 3)));
					list.a(new MerchantRecipe(new ItemStack(Item.RAW_BEEF, 5), new ItemStack(Item.LEATHER, 2)));
					list.a(new MerchantRecipe(new ItemStack(Item.SHEARS, 1), new ItemStack(Item.SADDLE, 1)));
					break;
				case FARMER:
					list.a(new MerchantRecipe(new ItemStack(Item.GOLD_NUGGET, 1), new ItemStack(Item.MILK_BUCKET, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.LEATHER, 3), new ItemStack(Item.LEASH, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.COOKED_BEEF, 3), new ItemStack(Item.EGG, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.MILK_BUCKET, 2), new ItemStack(Item.EGG, 1), new ItemStack(Item.CAKE, 2)));
					break;
				case LIBRARIAN:
					list.a(new MerchantRecipe(new ItemStack(Item.PAPER, 1), new ItemStack(Item.BOOK_AND_QUILL, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.PAPER, 3), new ItemStack(Item.LEATHER_CHESTPLATE, 1)));
					list.a(new MerchantRecipe(new ItemStack(Item.BOOK, 2), new ItemStack(Item.STICK, 3)));
					break;
				case PRIEST:
					list.a(new MerchantRecipe(new ItemStack(Item.NETHER_STAR, 1), CraftItemStack.asNMSCopy(health.toItemStack(1))));
					list.a(new MerchantRecipe(new ItemStack(Item.GOLDEN_APPLE, 2), CraftItemStack.asNMSCopy(strength.toItemStack(1))));
					list.a(new MerchantRecipe(CraftItemStack.asNMSCopy(health.toItemStack(1)), CraftItemStack.asNMSCopy(strength
							.toItemStack(1)), CraftItemStack.asNMSCopy(regen.toItemStack(1))));
					break;
				default:
					break;

				}
				recipes.set(villager, list);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		// Spawn NPCs
		if (type == EntityType.SKELETON && !Configuration.isNPC()) {
			e.setCancelled(true);
			return;
		}
		
		if (type == EntityType.SKELETON && e.getSpawnReason() != SpawnReason.CUSTOM) {
			e.setCancelled(true);
			if (random.nextDouble() <= 0.9) { return; }
			World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();
			NPCType npctype;
			CustomEntityNPC npc = new CustomEntityNPC(world, npctype = NPCType.getRandom());
			npc.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());

			if (world.addEntity(npc, SpawnReason.CUSTOM)) {
				Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn();
				packet.a = npc.getBukkitEntity().getEntityId();
				packet.b = getRandomName(npctype);
				packet.c = (int) e.getLocation().getX() * 32;
				packet.d = (int) e.getLocation().getY() * 32;
				packet.e = (int) e.getLocation().getZ() * 32;
				packet.f = 0;
				packet.g = 0;
				packet.h = npc.getEquipment(0) != null ? npc.getEquipment(0).id : 0;

				DataWatcher datawatcher = new DataWatcher();
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

				((Skeleton) npc.getBukkitEntity()).setRemoveWhenFarAway(true);
				((Skeleton) npc.getBukkitEntity()).getEquipment().setBootsDropChance(1);
				((Skeleton) npc.getBukkitEntity()).getEquipment().setLeggingsDropChance(1);
				((Skeleton) npc.getBukkitEntity()).getEquipment().setChestplateDropChance(1);
				((Skeleton) npc.getBukkitEntity()).getEquipment().setHelmetDropChance(1);
				((Skeleton) npc.getBukkitEntity()).getEquipment().setItemInHandDropChance(1);
			}
			return;
		} else if (type == EntityType.SKELETON && e.getSpawnReason() == SpawnReason.CUSTOM) { return; }

		if (e.getSpawnReason() != SpawnReason.DEFAULT && e.getSpawnReason() != SpawnReason.CHUNK_GEN
				&& e.getSpawnReason() != SpawnReason.NATURAL && e.getSpawnReason() != SpawnReason.VILLAGE_INVASION) { return; }

		if (MyZ.instance.getServer().getPluginManager().isPluginEnabled("WorldGuard"))
			if (WorldGuardManager.isAmplifiedRegion(e.getLocation())) {
				// Increase natural spawns inside towns.
				if (random.nextDouble() >= 0.5) {
					Location newLocation = e.getLocation().clone();
					newLocation.add(random.nextInt(8) * random.nextInt(2) == 0 ? -1 : 1, 0, random.nextInt(8) * random.nextInt(2) == 0 ? -1
							: 1);
					boolean doSpawn = true;
					while (newLocation.getBlock().getType() != Material.AIR) {
						newLocation.add(0, 1, 0);
						if (newLocation.getY() > newLocation.getWorld().getMaxHeight()) {
							doSpawn = false;
							break;
						}
					}
					if (doSpawn)
						e.getLocation().getWorld().spawnEntity(newLocation, e.getEntityType());
				}
			} else // Decrease natural spawns outside of towns.
			if (random.nextDouble() <= 0.45) {
				e.setCancelled(true);
				return;
			}
		// Make sure we only spawn our desired mobs.
		if (type != EntityType.ZOMBIE && type != EntityType.GIANT && type != EntityType.HORSE && type != EntityType.PLAYER
				&& type != EntityType.PIG_ZOMBIE && type != EntityType.SKELETON && type != EntityType.VILLAGER) {
			e.setCancelled(true);
			return;
		}

		if (e.getEntityType() == EntityType.ZOMBIE) {
			((Zombie) e.getEntity()).setBaby(random.nextInt(20) < 3);
		}

		// Make some natural pigmen spawn.
		if (e.getLocation().getZ() >= 2000 && type == EntityType.ZOMBIE && random.nextInt(30) == 1) {
			World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();
			CustomEntityPigZombie pigman = new CustomEntityPigZombie(world);
			pigman.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
			pigman.setBaby(random.nextInt(20) < 3);
			world.addEntity(pigman, SpawnReason.NATURAL);
			e.setCancelled(true);
			return;
		}

		// Undead and skeletal horses.
		if (type == EntityType.HORSE) {
			Horse horse = (Horse) e.getEntity();
			switch (random.nextInt(10)) {
			case 0:
			case 1:
				horse.setVariant(Variant.UNDEAD_HORSE);
				break;
			case 2:
			case 3:
			case 4:
				horse.setVariant(Variant.SKELETON_HORSE);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Get a random name for an NPC.
	 * 
	 * @return The name.
	 */
	private String getRandomName(NPCType type) {
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
}
