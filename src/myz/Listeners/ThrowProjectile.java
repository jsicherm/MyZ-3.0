/**
 * 
 */
package myz.Listeners;

import java.util.ArrayList;
import java.util.List;

import myz.Support.Configuration;
import myz.mobs.SmartEntity;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * @author Jordan
 * 
 */
public class ThrowProjectile implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onShootArrow(ProjectileLaunchEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			for (Entity nearby : e.getEntity().getNearbyEntities(10, 5, 10)) {
				if (nearby instanceof SmartEntity) {
					((SmartEntity) nearby).see(e.getEntity().getLocation(), 2);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onProjectileLand(ProjectileHitEvent e) {
		for (Entity nearby : e.getEntity().getNearbyEntities(10, 5, 10)) {
			if (nearby instanceof SmartEntity) {
				((SmartEntity) nearby).see(e.getEntity().getLocation(), e.getEntity() instanceof Snowball ? 3 : 1);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onGrenadeLand(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.ENDER_PEARL && Configuration.isUsingGrenades()) {
			e.setCancelled(true);
			e.getTo().getWorld().createExplosion(e.getTo(), 4f);
			
			// Minor issue being chunk entities rather than nearby but no real problem.
			for (Entity nearby : e.getTo().getChunk().getEntities()) {
				if (nearby instanceof SmartEntity) {
					((SmartEntity) nearby).see(e.getTo(), 4);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onExplodeBlocks(EntityExplodeEvent e) {
		List<Block> explodedBlocks = new ArrayList<Block>();

		for (Block block : e.blockList())
			if (block.getType() == Material.WEB)
				explodedBlocks.add(block);

		e.blockList().clear();
		e.blockList().addAll(explodedBlocks);
		e.setYield(0f);
	}
}
