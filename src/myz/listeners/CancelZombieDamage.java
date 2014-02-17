/**
 * 
 */
package myz.listeners;

import java.util.List;

import myz.support.interfacing.Configuration;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * @author Jordan
 * 
 */
public class CancelZombieDamage implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	private void onBurnInSun(EntityCombustEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		if (e.getEntityType() == EntityType.ZOMBIE || e.getEntityType() == EntityType.PIG_ZOMBIE || e.getEntityType() == EntityType.GIANT
				|| e.getEntityType() == EntityType.SKELETON)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onTakeFallDamage(EntityDamageEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		if (e.getCause() == DamageCause.FALL
				&& (e.getEntityType() == EntityType.ZOMBIE || e.getEntityType() == EntityType.PIG_ZOMBIE || e.getEntityType() == EntityType.GIANT))
			e.setCancelled(true);
	}
}
