/**
 * 
 */
package myz.Listeners;

import java.util.Random;

import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * @author Jordan
 * 
 */
public class PlayerHurtEntity implements Listener {

	private Random random = new Random();

	@EventHandler(priority = EventPriority.LOWEST)
	private void onDamage(EntityDamageByEntityEvent e) {
		// Cancel damage inside spawn room.
		if (Configuration.isInLobby(e.getDamager().getLocation()))
			e.setCancelled(true);

		// Do widespread hits with axes when crouched.
		if (e.getDamager() instanceof Player && isAxe(((Player) e.getDamager()).getItemInHand().getType())
				&& ((Player) e.getDamager()).isSneaking())
			if (random.nextDouble() <= 0.33)
				for (Entity nearby : e.getDamager().getNearbyEntities(1.5, 2, 1.5))
					if (nearby instanceof Zombie || nearby instanceof PigZombie || nearby instanceof Horse || nearby instanceof Player
							&& nearby != e.getDamager()) {
						LivingEntity living = (LivingEntity) nearby;
						living.damage(e.getDamage(), e.getDamager());
					}
		// Do headshots and pulling
		if (e.getCause() == DamageCause.PROJECTILE) {
			Projectile projectile = (Projectile) e.getDamager();
			if (wasHeadshot(projectile.getShooter(), projectile))
				e.setDamage(e.getDamage() * 2);
		} else if (e.getCause() == DamageCause.ENTITY_ATTACK && e.getDamager() instanceof Player) {
			Location otherLocation = e.getEntity().getLocation();
			Location playerLocation = e.getDamager().getLocation();

			// Pull a player off a ledge.
			if (playerLocation.distance(otherLocation) >= 1 && playerLocation.getY() > otherLocation.getY())
				e.getDamager().setVelocity(otherLocation.toVector().subtract(playerLocation.toVector()).normalize().multiply(0.15));
		}
	}

	private boolean wasHeadshot(Entity entity, Projectile arrow) {
		if (!(arrow instanceof Arrow) || !(arrow.getShooter() instanceof Player))
			return false;

		double projectileY = arrow.getLocation().getY();
		double entityY = entity.getLocation().getY();
		boolean headshot = projectileY - entityY > 1.75d;

		if (headshot)
			Messenger.sendConfigMessage((Player) arrow.getShooter(), "damage.headshot");
		return headshot;
	}

	/**
	 * Whether or not a material is an axe material.
	 * 
	 * @param material
	 *            The material to compare.
	 * @return True if the material is a wooden, stone, gold, iron or diamond
	 *         axe.
	 */
	private boolean isAxe(Material material) {
		return material == Material.WOOD_AXE || material == Material.STONE_AXE || material == Material.GOLD_AXE
				|| material == Material.IRON_AXE || material == Material.DIAMOND_AXE;
	}
}
