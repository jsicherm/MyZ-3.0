/**
 * 
 */
package myz.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

/**
 * @author Jordan
 * 
 */
public class CancelPlayerEvents implements Listener {

	@EventHandler
	private void onRegen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player && e.getRegainReason() == RegainReason.SATIATED)
			e.setCancelled(true);
	}
}
