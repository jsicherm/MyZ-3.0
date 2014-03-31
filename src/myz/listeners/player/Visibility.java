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
import myz.utilities.Validate;
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onChestOpen(PlayerInteractEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST
				|| e.getClickedBlock().getType() == Material.TRAP_DOOR || e.getClickedBlock().getType() == Material.WOOD_DOOR)
			PathingSupport.elevatePlayer(e.getPlayer(), (Integer) Configuration.getConfig("projectile.doors.visibility_range"));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onExplodeBlocks(EntityExplodeEvent e) {
		if (!Validate.inWorld(e.getLocation()))
			return;
		List<Block> explodedBlocks = new ArrayList<Block>();

		for (Block block : e.blockList())
			if (block.getType() == Material.WEB)
				explodedBlocks.add(block);

		e.blockList().clear();
		e.blockList().addAll(explodedBlocks);
		e.setYield(0f);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onGrenadeLand(PlayerTeleportEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (e.getCause() == TeleportCause.ENDER_PEARL && (Boolean) Configuration.getConfig(Configuration.ENDERNADE)) {
			e.setCancelled(true);
			e.getTo().getWorld().createExplosion(e.getTo(), 4f);

			// Minor issue being chunk entities rather than nearby but no real
			// problem.
			for (Entity nearby : e.getTo().getChunk().getEntities())
				if (nearby.getType() == EntityType.ZOMBIE || nearby.getType() == EntityType.PIG_ZOMBIE
						|| nearby.getType() == EntityType.SKELETON)
					see((EntityInsentient) ((CraftLivingEntity) nearby).getHandle(), e.getTo(),
							(Integer) Configuration.getConfig("projectile.enderpearl.visibility_priority"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onProjectileLand(ProjectileHitEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;
		for (Entity nearby : e.getEntity().getNearbyEntities((Integer) Configuration.getConfig("projectile.snowball.visibility_range"), 5,
				(Integer) Configuration.getConfig("projectile.snowball.visibility_range")))
			if (nearby.getType() == EntityType.ZOMBIE || nearby.getType() == EntityType.PIG_ZOMBIE
					|| nearby.getType() == EntityType.SKELETON)
				see((EntityInsentient) ((CraftLivingEntity) nearby).getHandle(), e.getEntity().getLocation(),
						e.getEntity() instanceof Snowball ? (Integer) Configuration.getConfig("projectile.snowball.visibility_priority")
								: (Integer) Configuration.getConfig("projectile.other.visibility_priority"));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onShootArrow(ProjectileLaunchEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;
		if (e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof Arrow)
			PathingSupport.elevatePlayer((Player) e.getEntity().getShooter(),
					(Integer) Configuration.getConfig("projectile.arrow.shoot.visibility_range"));
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
