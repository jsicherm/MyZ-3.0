/**
 * 
 */
package myz.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author Jordan
 * 
 */
public class PlayerKillEntity implements Listener {

	@EventHandler
	private void onEntityDeath(EntityDeathEvent e) {
		e.setDroppedExp(0);
	}
}
