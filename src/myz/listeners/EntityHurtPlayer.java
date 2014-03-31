/**
 * 
 */
package myz.listeners;

import java.util.Random;

import myz.MyZ;
import myz.support.interfacing.Configuration;
import myz.utilities.Validate;

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

	private Random random = new Random();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onZombification(EntityDamageByEntityEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;
		if ((e.getDamager() instanceof Horse || e.getDamager() instanceof Zombie) && e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Horse && ((Horse) e.getDamager()).getVariant() != Variant.UNDEAD_HORSE)
				return;
			if (random.nextDouble() <= (Double) Configuration.getConfig("damage.chance_of_poison_from_zombie")
					&& (Double) Configuration.getConfig("damage.chance_of_poison_from_zombie") != 0.0)
				MyZ.instance.startPoison((Player) e.getEntity());
		}

		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player)
			if (MyZ.instance.isFriend(((Player) e.getDamager()).getUniqueId(), ((Player) e.getEntity()).getUniqueId()))
				e.setCancelled(true);
	}
}
