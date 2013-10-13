/**
 * 
 */
package myz.Listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * @author Jordan
 * 
 */
public class EntitySpawn implements Listener {

	@EventHandler
	private void onSpawn(CreatureSpawnEvent e) {
		EntityType type = e.getEntityType();
		// Make sure we only spawn our desired mobs.
		if (type != EntityType.ZOMBIE && type != EntityType.GIANT && type != EntityType.HORSE && type != EntityType.PLAYER
				&& type != EntityType.PIG_ZOMBIE) {
			e.setCancelled(true);
			return;
		}
	}
}
