/**
 * 
 */
package myz.listeners.player;

import java.util.ArrayList;
import java.util.List;

import myz.mobs.SmartEntity;
import myz.mobs.pathing.PathingSupport;
import myz.support.interfacing.Configuration;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * @author Jordan
 * 
 */
public class Visibility implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onShootArrow(ProjectileLaunchEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		if (e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof Arrow)
			PathingSupport.elevatePlayer((Player) e.getEntity().getShooter(), 20);
		/*
		for (Entity nearby : e.getEntity().getNearbyEntities(10, 5, 10)) {
			if (nearby instanceof SmartEntity) {
				((SmartEntity) nearby).see(e.getEntity().getLocation(), 2);
			}
		}*/
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onProjectileLand(ProjectileHitEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		for (Entity nearby : e.getEntity().getNearbyEntities(10, 5, 10))
			if (nearby instanceof SmartEntity)
				((SmartEntity) nearby).see(e.getEntity().getLocation(), e.getEntity() instanceof Snowball ? 3 : 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onGrenadeLand(PlayerTeleportEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getCause() == TeleportCause.ENDER_PEARL && (Boolean) Configuration.getConfig(Configuration.ENDERNADE)) {
			e.setCancelled(true);
			e.getTo().getWorld().createExplosion(e.getTo(), 4f);

			// Minor issue being chunk entities rather than nearby but no real
			// problem.
			for (Entity nearby : e.getTo().getChunk().getEntities())
				if (nearby instanceof SmartEntity)
					((SmartEntity) nearby).see(e.getTo(), 4);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onChestOpen(PlayerInteractEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST)
			PathingSupport.elevatePlayer(e.getPlayer(), 10);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onExplodeBlocks(EntityExplodeEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getLocation().getWorld().getName()))
			return;
		List<Block> explodedBlocks = new ArrayList<Block>();

		for (Block block : e.blockList())
			if (block.getType() == Material.WEB)
				explodedBlocks.add(block);

		e.blockList().clear();
		e.blockList().addAll(explodedBlocks);
		e.setYield(0f);
	}
}
