/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.Commands.BlockCommand;
import myz.Support.Configuration;
import myz.chests.ChestManager;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Jordan
 * 
 */
public class BlockEvent implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlace(BlockPlaceEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getBlockPlaced().getType() == Material.ENDER_CHEST)
			return;
		if (BlockCommand.blockChangers.containsKey(e.getPlayer().getName())) {
			BlockCommand.blockChangers.get(e.getPlayer().getName()).doOnPlace(e.getBlockPlaced(), e.getPlayer());
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onClick(PlayerInteractEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && BlockCommand.blockChangers.containsKey(e.getPlayer().getName())) {
			BlockCommand.blockChangers.get(e.getPlayer().getName()).doOnHit(e.getItem(), e.getClickedBlock(), e.getPlayer());
			e.setCancelled(true);
		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST
				&& ChestManager.isMyZChest(e.getClickedBlock().getLocation())) {
			e.setCancelled(true);
			e.getClickedBlock().setType(Material.AIR);
		}
	}

	/*
	 *  Above are the methods for creating the links.
	 *  Below are the methods for reacting to the links.
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlacement(BlockPlaceEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getBlockPlaced().getType() == Material.ENDER_CHEST)
			return;
		boolean state = !Configuration.canPlace(e.getBlock());
		if (state && e.getPlayer().hasPermission("MyZ.world_admin"))
			return;
		state = Configuration.doPlace(e.getBlock());
		e.setCancelled(state);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onDestroy(BlockBreakEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		boolean state = !Configuration.canBreak(e.getBlock(), e.getPlayer().getItemInHand());
		if (state && e.getPlayer().hasPermission("MyZ.world_admin"))
			return;
		state = Configuration.doBreak(e.getBlock(), e.getPlayer().getItemInHand());
		e.setCancelled(state);
	}
}
