/**
 * 
 */
package myz.listeners.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import myz.mobs.CustomEntityNPC;
import myz.mobs.CustomEntityPigZombie;
import myz.mobs.CustomEntityZombie;
import myz.mobs.pathing.PathingSupport;
import myz.support.interfacing.Configuration;
import net.minecraft.server.v1_7_R1.EntityInsentient;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onShootArrow(ProjectileLaunchEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		if (e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof Arrow)
			PathingSupport.elevatePlayer((Player) e.getEntity().getShooter(), 10);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onProjectileLand(ProjectileHitEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getEntity().getWorld().getName()))
			return;
		for (Entity nearby : e.getEntity().getNearbyEntities(10, 5, 10))
			if (nearby.getType() == EntityType.ZOMBIE || nearby.getType() == EntityType.PIG_ZOMBIE
					|| nearby.getType() == EntityType.SKELETON) {
				see((EntityInsentient) ((CraftLivingEntity) nearby).getHandle(), e.getEntity().getLocation(),
						e.getEntity() instanceof Snowball ? 3 : 1);
			}
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
				if (nearby.getType() == EntityType.ZOMBIE || nearby.getType() == EntityType.PIG_ZOMBIE
						|| nearby.getType() == EntityType.SKELETON)
					see((EntityInsentient) ((CraftLivingEntity) nearby).getHandle(), e.getTo(), 4);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onChestOpen(PlayerInteractEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST
				|| e.getClickedBlock().getType() == Material.TRAP_DOOR || e.getClickedBlock().getType() == Material.WOOD_DOOR)
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

	private void see(net.minecraft.server.v1_7_R1.EntityInsentient entity, Location location, int priority) {
		if (random.nextInt(priority + 1) >= 1 && entity.getGoalTarget() == null || priority > 1) {
			entity.setGoalTarget(null);
			if (entity.getBukkitEntity().getType() == EntityType.ZOMBIE)
				((CustomEntityZombie) entity).see(location, priority);
			else if (entity.getBukkitEntity().getType() == EntityType.PIG_ZOMBIE)
				((CustomEntityPigZombie) entity).see(location, priority);
			else if (entity.getBukkitEntity().getType() == EntityType.SKELETON)
				((CustomEntityNPC) entity).see(location, priority);
		}
	}
}
