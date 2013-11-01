/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Utilities.Utilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Jordan
 * 
 */
public class CancelPlayerEvents implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	private void onRegen(EntityRegainHealthEvent e) {
		if(!MyZ.instance.getWorlds().contains(e.getEntity().getWorld().getName())) { return; }
		if (e.getEntity() instanceof Player && e.getRegainReason() == RegainReason.SATIATED)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSafeLogout(PlayerInteractEvent e) {
		if(!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName())) { return; }
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null
				&& e.getItem().isSimilar(Configuration.getSafeLogoutItem())) {
			e.setCancelled(true);
			Utilities.startSafeLogout(e.getPlayer());
		}
	}
}
