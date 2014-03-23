/**
 * 
 */
package myz.listeners.player;

import java.util.List;

import myz.chests.ChestManager;
import myz.commands.BlockCommand;
import myz.support.Teleport;
import myz.support.interfacing.Configuration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Jordan
 * 
 */
public class BlockEvent implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlace(BlockPlaceEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getBlockPlaced().getType() == Material.ENDER_CHEST)
			return;
		if (BlockCommand.blockChangers.containsKey(e.getPlayer().getUniqueId())) {
			BlockCommand.blockChangers.get(e.getPlayer().getUniqueId()).doOnPlace(e.getBlockPlaced(), e.getPlayer());
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onClick(PlayerInteractEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		Location grave;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && BlockCommand.blockChangers.containsKey(e.getPlayer().getUniqueId())) {
			BlockCommand.blockChangers.get(e.getPlayer().getUniqueId()).doOnHit(e.getItem(), e.getClickedBlock(), e.getPlayer());
			e.setCancelled(true);
		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST
				&& ChestManager.isMyZChest(e.getClickedBlock().getLocation())) {
			e.setCancelled(true);
			ChestManager.breakChest(e.getClickedBlock());
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.BONE
				&& (grave = didClickGrave(e.getClickedBlock())) != null) {
			e.setCancelled(true);
			Teleport.teleport(e.getPlayer(), grave.subtract(0, 1, 0));
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.BONE
				&& e.getBlockFace() == BlockFace.DOWN && (grave = didClickOutGrave(e.getClickedBlock())) != null) {
			e.setCancelled(true);
			e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
			Teleport.teleport(e.getPlayer(), grave.add(0, 1, 0));
		}
	}

	/**
	 * Whether or not the Player clicked the bottom face of the soul sand below
	 * a grave block. A grave block is identified by a cobble wall material
	 * block clicked in the pillar of a cross shaped layout as a 4 tall pillar
	 * with 1-block wings on opposing sides of the third block up. The base
	 * should be soul sand and the two blocks beneath that should be air.
	 * 
	 * @param clicked
	 *            The block the player clicked.
	 * @return The location of the block one above the soul sand under this
	 *         grave or null if a grave base wasn't clicked.
	 */
	private Location didClickOutGrave(Block clicked) {
		Material g = Material.COBBLE_WALL;
		if (clicked.getType() != Material.SOUL_SAND)
			return null;
		Location click = clicked.getLocation();
		// There is a grave above where we clicked.
		if (click.clone().add(0, 1, 0).getBlock().getType() == g && click.clone().add(0, 2, 0).getBlock().getType() == g
				&& click.clone().add(0, 3, 0).getBlock().getType() == g && click.clone().add(0, 4, 0).getBlock().getType() == g)
			if (click.clone().add(1, 3, 0).getBlock().getType() == g && click.clone().add(-1, 3, 0).getBlock().getType() == g
					|| click.clone().add(0, 3, 1).getBlock().getType() == g && click.clone().add(0, 3, 1).getBlock().getType() == g)
				return click.clone().add(0, 1, 0);
		return null;
	}

	/**
	 * Whether or not the Player clicked a grave block. A grave block is
	 * identified by a cobble wall material block clicked in the pillar of a
	 * cross shaped layout as a 4 tall pillar with 1-block wings on opposing
	 * sides of the third block up. The base should be soul sand and the two
	 * blocks beneath that should be air.
	 * 
	 * @param clicked
	 *            The block the player clicked.
	 * @return The location of the block one below the soul sand under this
	 *         grave or null if a grave wasn't clicked.
	 */
	private Location didClickGrave(Block clicked) {
		Material g = Material.COBBLE_WALL;
		if (clicked.getType() != g)
			return null;
		Location click = clicked.getLocation();
		// Click the base block.
		if (click.clone().subtract(0, 1, 0).getBlock().getType() == Material.SOUL_SAND
				&& click.clone().subtract(0, 2, 0).getBlock().getType() == Material.AIR
				&& click.clone().subtract(0, 3, 0).getBlock().getType() == Material.AIR) {
			// There is a pillar of three more above.
			if (click.clone().add(0, 1, 0).getBlock().getType() == g && click.clone().add(0, 2, 0).getBlock().getType() == g
					&& click.clone().add(0, 3, 0).getBlock().getType() == g)
				// There are two branches in either direction so we have a
				// grave.
				if (click.clone().add(1, 2, 0).getBlock().getType() == g && click.clone().add(-1, 2, 0).getBlock().getType() == g
						|| click.clone().add(0, 2, 1).getBlock().getType() == g && click.clone().add(0, 2, -1).getBlock().getType() == g)
					return click.clone().subtract(0, 2, 0);
		} else if (click.clone().subtract(0, 2, 0).getBlock().getType() == Material.SOUL_SAND
				&& click.clone().subtract(0, 3, 0).getBlock().getType() == Material.AIR
				&& click.clone().subtract(0, 4, 0).getBlock().getType() == Material.AIR) {
			// There is a pillar of three more above.
			if (click.clone().subtract(0, 1, 0).getBlock().getType() == g && click.clone().add(0, 1, 0).getBlock().getType() == g
					&& click.clone().add(0, 2, 0).getBlock().getType() == g)
				// There are two branches in either direction so we have a
				// grave.
				if (click.clone().add(1, 1, 0).getBlock().getType() == g && click.clone().add(-1, 1, 0).getBlock().getType() == g
						|| click.clone().add(0, 1, 1).getBlock().getType() == g && click.clone().add(0, 1, -1).getBlock().getType() == g)
					return click.clone().subtract(0, 3, 0);
		} else if (click.clone().subtract(0, 3, 0).getBlock().getType() == Material.SOUL_SAND
				&& click.clone().subtract(0, 4, 0).getBlock().getType() == Material.AIR
				&& click.clone().subtract(0, 5, 0).getBlock().getType() == Material.AIR) {
			// There is a pillar of three more above.
			if (click.clone().subtract(0, 2, 0).getBlock().getType() == g && click.clone().subtract(0, 1, 0).getBlock().getType() == g
					&& click.clone().add(0, 1, 0).getBlock().getType() == g)
				// There are two branches in either direction so we have a
				// grave.
				if (click.clone().add(1, 0, 0).getBlock().getType() == g && click.clone().add(-1, 0, 0).getBlock().getType() == g
						|| click.clone().add(0, 0, 1).getBlock().getType() == g && click.clone().add(0, 0, -1).getBlock().getType() == g)
					return click.clone().subtract(0, 4, 0);
		} else if (click.clone().subtract(0, 4, 0).getBlock().getType() == Material.SOUL_SAND
				&& click.clone().subtract(0, 5, 0).getBlock().getType() == Material.AIR
				&& click.clone().subtract(0, 6, 0).getBlock().getType() == Material.AIR)
			// There is a pillar of three more above.
			if (click.clone().subtract(0, 3, 0).getBlock().getType() == g && click.clone().subtract(0, 2, 0).getBlock().getType() == g
					&& click.clone().subtract(0, 1, 0).getBlock().getType() == g)
				// There are two branches in either direction so we have a
				// grave.
				if (click.clone().subtract(-1, 1, 0).getBlock().getType() == g && click.clone().subtract(1, 1, 0).getBlock().getType() == g
						|| click.clone().subtract(0, 1, -1).getBlock().getType() == g
						&& click.clone().subtract(0, 1, 1).getBlock().getType() == g)
					return click.clone().subtract(0, 5, 0);
		return null;
	}

	/*
	 *  Above are the methods for creating the links.
	 *  Below are the methods for reacting to the links.
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlacement(BlockPlaceEvent e) {
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
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
		if (!((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(e.getPlayer().getWorld().getName()))
			return;
		boolean state = !Configuration.canBreak(e.getBlock(), e.getPlayer().getItemInHand());
		if (state && e.getPlayer().hasPermission("MyZ.world_admin"))
			return;
		state = Configuration.doBreak(e.getBlock(), e.getPlayer().getItemInHand());
		e.setCancelled(state);
	}
}
