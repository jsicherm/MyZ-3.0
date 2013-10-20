/**
 * 
 */
package myz.Listeners;

import java.util.Random;

import myz.Support.Configuration;
import myz.mobs.CustomEntityPigZombie;
import net.minecraft.server.v1_6_R3.World;

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
		EntityType type = e.getEntityType();
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
