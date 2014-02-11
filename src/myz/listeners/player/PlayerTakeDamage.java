/**
 * 
 */
package myz.listeners.player;

import java.util.Random;

import myz.MyZ;
import myz.mobs.pathing.PathingSupport;
import myz.support.interfacing.Configuration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Jordan
 * 
 */
public class PlayerTakeDamage implements Listener {

	private Random random = new Random();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onDamage(EntityDamageEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getEntity().getWorld().getName()))
			return;
		if (e.getEntity() instanceof Player)
			if (random.nextDouble() <= Configuration.getBleedChance() && Configuration.getBleedChance() != 0.0)
				switch (e.getCause()) {
				case FALL:
					PathingSupport.elevatePlayer((Player) e.getEntity(), 10);
				case BLOCK_EXPLOSION:
				case CONTACT:
				case CUSTOM:
				case ENTITY_ATTACK:
				case ENTITY_EXPLOSION:
				case FALLING_BLOCK:
				case PROJECTILE:
				case THORNS:
					MyZ.instance.startBleeding((Player) e.getEntity());
					break;
				default:
					break;

				}
	}
}
