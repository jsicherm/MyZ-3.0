/**
 * 
 */
package myz.Listeners;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Utilities.Utilities;
import myz.Utilities.WorldGuardManager;
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
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * @author Jordan
 * 
 */
public class EntitySpawn implements Listener {

	private Random random = new Random();

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSpawn(CreatureSpawnEvent e) {
		World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();

		// MultiWorld support, yay!
		if (!MyZ.instance.getWorlds().contains(e.getLocation().getWorld().getName())) {
			if (e.getEntity().getMetadata("MyZ.bypass") != null && !e.getEntity().getMetadata("MyZ.bypass").isEmpty())
				return;
			switch (e.getEntityType()) {
			case HORSE:
				EntityHorse horse = new EntityHorse(world);
				horse.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
				if (horse.canSpawn()) {
					horse.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
					world.addEntity(horse, e.getSpawnReason());
				}
				e.setCancelled(true);
				return;
			case PIG_ZOMBIE:
				EntityPigZombie pigman = new EntityPigZombie(world);
				pigman.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
				if (pigman.canSpawn()) {
					pigman.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
					world.addEntity(pigman, e.getSpawnReason());
				}
				e.setCancelled(true);
				return;
			case ZOMBIE:
				EntityZombie zombie = new EntityZombie(world);
				zombie.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
				if (zombie.canSpawn()) {
					zombie.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
					world.addEntity(zombie, e.getSpawnReason());
				}
				e.setCancelled(true);
				return;
			case SKELETON:
				EntitySkeleton skeleton = new EntitySkeleton(world);
				skeleton.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
				if (skeleton.canSpawn()) {
					skeleton.getBukkitEntity().setMetadata("MyZ.bypass", new FixedMetadataValue(MyZ.instance, true));
					skeleton.a((GroupDataEntity) null);
					skeleton.bT();
					world.addEntity(skeleton, e.getSpawnReason());
				}
				e.setCancelled(true);
				return;
			default:
				return;
			}
		}

		// Cancel spawning inside spawn room.
		if (Configuration.isInLobby(e.getEntity().getLocation())) {
			e.setCancelled(true);
			return;
		}

		EntityType type = e.getEntityType();

		// Override mooshroom spawns with giant spawns.
		if (e.getSpawnReason() == SpawnReason.SPAWNER_EGG && e.getEntityType() == EntityType.MUSHROOM_COW) {
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
					list.a(new MerchantRecipe(new ItemStack(Items.MILK_BUCKET, 2), new ItemStack(Items.EGG, 1),
							new ItemStack(Items.CAKE, 2)));
					break;
				case LIBRARIAN:
					list.a(new MerchantRecipe(new ItemStack(Items.PAPER, 1), new ItemStack(Items.BOOK_AND_QUILL, 1)));
					list.a(new MerchantRecipe(new ItemStack(Items.PAPER, 3), new ItemStack(Items.LEATHER_CHESTPLATE, 1)));
					list.a(new MerchantRecipe(new ItemStack(Items.BOOK, 2), new ItemStack(Items.STICK, 3)));
					break;
				case PRIEST:
					list.a(new MerchantRecipe(new ItemStack(Items.NETHER_STAR, 1), CraftItemStack.asNMSCopy(health.toItemStack(1))));
					list.a(new MerchantRecipe(new ItemStack(Items.GOLDEN_APPLE, 2), CraftItemStack.asNMSCopy(strength.toItemStack(1))));
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

		if (type == EntityType.ZOMBIE && random.nextDouble() <= 0.1 && e.getSpawnReason() != SpawnReason.CUSTOM && Configuration.isNPC()) {
			e.setCancelled(true);
			if (random.nextDouble() <= 0.9)
				return;
			NPCType npctype;
			CustomEntityNPC npc = new CustomEntityNPC(world, npctype = NPCType.getRandom());
			npc.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());

			if (world.addEntity(npc, SpawnReason.CUSTOM)) {
				if (MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") == null
						|| !MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled()) {
					PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
					try {
						Utilities.setPrivateField(packet, "a", npc.getBukkitEntity().getEntityId());
						// TODO ID, name
						String name = getRandomName(npctype);
						GameProfile profile = new GameProfile(name, name);
						Utilities.setPrivateField(packet, "b", profile);
						Utilities.setPrivateField(packet, "c", (int) e.getLocation().getX() * 32);
						Utilities.setPrivateField(packet, "d", (int) e.getLocation().getY() * 32);
						Utilities.setPrivateField(packet, "e", (int) e.getLocation().getZ() * 32);
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
			return;
		}

		if (e.getSpawnReason() != SpawnReason.DEFAULT && e.getSpawnReason() != SpawnReason.CHUNK_GEN
				&& e.getSpawnReason() != SpawnReason.NATURAL && e.getSpawnReason() != SpawnReason.VILLAGE_INVASION)
			return;

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
				&& type != EntityType.PIG_ZOMBIE && type != EntityType.VILLAGER) {
			e.setCancelled(true);
			return;
		}

		if (e.getEntityType() == EntityType.ZOMBIE)
			((Zombie) e.getEntity()).setBaby(random.nextInt(20) < 3);

		// Make some natural pigmen spawn.
		if (e.getLocation().getZ() >= 2000 && type == EntityType.ZOMBIE && random.nextInt(30) == 1) {
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
