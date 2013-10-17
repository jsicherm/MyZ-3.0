/**
 * 
 */
package myz.Listeners;

import java.util.Random;

import myz.MyZ;
import myz.Support.Configuration;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author Jordan
 * 
 */
public class EntityHurtPlayer implements Listener {

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onZombification(EntityDamageByEntityEvent e) {
		if ((e.getDamager() instanceof Horse || e.getDamager() instanceof Zombie) && e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Horse && ((Horse) e.getDamager()).getVariant() != Variant.UNDEAD_HORSE)
				return;
			if (random.nextDouble() <= Configuration.getPoisonChanceZombie() && Configuration.getPoisonChanceZombie() != 0.0)
				MyZ.instance.startPoison((Player) e.getEntity());
		}
	}
}
