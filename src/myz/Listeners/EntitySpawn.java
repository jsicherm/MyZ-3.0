/**
 * 
 */
package myz.Listeners;

import java.util.Random;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Utilities.WorldGuardManager;
import myz.mobs.CustomEntityGiantZombie;
import myz.mobs.CustomEntityPigZombie;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * @author Jordan
 * 
 */
public class EntitySpawn implements Listener {

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSpawn(CreatureSpawnEvent e) {
		if(!MyZ.instance.getWorlds().contains(e.getLocation().getWorld().getName())) { return; }
		
		EntityType type = e.getEntityType();

		// Override mooshroom spawns with giant spawns.
		if (e.getSpawnReason() == SpawnReason.SPAWNER_EGG && e.getEntityType() == EntityType.MUSHROOM_COW) {
			World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();
			CustomEntityGiantZombie giant = new CustomEntityGiantZombie(world);
			giant.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
			world.addEntity(giant, SpawnReason.NATURAL);
			e.setCancelled(true);
			return;
		}

		if (e.getSpawnReason() != SpawnReason.DEFAULT && e.getSpawnReason() != SpawnReason.CHUNK_GEN
			&& e.getSpawnReason() != SpawnReason.NATURAL && e.getSpawnReason() != SpawnReason.VILLAGE_INVASION) { return; }

		if (MyZ.instance.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
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
			} else {
				// Decrease natural spawns outside of towns.
				if (random.nextDouble() <= 0.3) {
					e.setCancelled(true);
					return;
				}
			}
		}
		// Make sure we only spawn our desired mobs.
		if (type != EntityType.ZOMBIE && type != EntityType.GIANT && type != EntityType.HORSE && type != EntityType.PLAYER
				&& type != EntityType.PIG_ZOMBIE) {
			e.setCancelled(true);
			return;
		}

		// Cancel spawning inside spawn room.
		if (Configuration.isInLobby(e.getEntity().getLocation())) {
			e.setCancelled(true);
			return;
		}

		// Make some natural pigmen spawn.
		if (e.getLocation().getZ() >= 2000 && type == EntityType.ZOMBIE && random.nextInt(30) == 1) {
			World world = ((CraftWorld) e.getLocation().getWorld()).getHandle();
			CustomEntityPigZombie pigman = new CustomEntityPigZombie(world);
			pigman.setPosition(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
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
}
