/**
 * 
 */
package myz.Listeners;

import java.util.ArrayList;
import java.util.List;

import myz.Support.Configuration;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

	@EventHandler
	private void onShootArrow(ProjectileLaunchEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			// TODO attract zombies to location of fired projectile with medium
			// priority.
		}
	}

	@EventHandler
	private void onProjectileLand(ProjectileHitEvent e) {
		// TODO attract zombies to location of landed projectile with low
		// priority unless was snowball, in which case high priority.
	}

	@EventHandler
	private void onGrenadeLand(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.ENDER_PEARL && Configuration.isUsingGrenades()) {
			e.setCancelled(true);
			e.getTo().getWorld().createExplosion(e.getTo(), 4f);
			// TODO attract zombies to location of landed grenade with very high
			// priority.
		}
	}

	@EventHandler
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
