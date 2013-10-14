/**
 * 
 */
package myz.Listeners;

import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * @author Jordan
 * 
 */
public class EntitySpawn implements Listener {

	private static final Random random = new Random();

	@EventHandler
	private void onSpawn(CreatureSpawnEvent e) {
		EntityType type = e.getEntityType();
		// Make sure we only spawn our desired mobs.
		if (type != EntityType.ZOMBIE && type != EntityType.GIANT && type != EntityType.HORSE && type != EntityType.PLAYER
				&& type != EntityType.PIG_ZOMBIE) {
			e.setCancelled(true);
			return;
		}
		if (type == EntityType.HORSE) {
			Horse horse = ((Horse) e.getEntity());
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
