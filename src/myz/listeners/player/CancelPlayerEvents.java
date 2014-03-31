/**
 * 
 */
package myz.listeners.player;

import myz.support.interfacing.Configuration;
import myz.utilities.Utils;
import myz.utilities.Validate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class CancelPlayerEvents implements Listener {

	private void grapple(Player puller, Entity pulled, Location to) {
		if (puller.equals(pulled))
			Utils.pullTo(puller, to, false);
		else
			Utils.pullTo(pulled, to, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onFish(PlayerFishEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;

		Player p = e.getPlayer();
		if (p.getItemInHand().getType() != Material.FISHING_ROD)
			return;
		if (e.getState() == State.IN_GROUND) {
			for (Entity entity : e.getHook().getNearbyEntities(1.5, 1, 1.5))
				if (entity instanceof Item) {
					p.getItemInHand().setDurability((short) -12);
					grapple(p, entity, p.getLocation());
					return;
				}
			grapple(p, p, e.getHook().getLocation());
		} else if (e.getState() == State.CAUGHT_ENTITY) {
			e.setCancelled(true);
			grapple(p, e.getCaught(), p.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onRegen(EntityRegainHealthEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;
		if (e.getEntity() instanceof Player && e.getRegainReason() == RegainReason.SATIATED)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onSafeLogout(PlayerInteractEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null
				&& e.getItem().isSimilar((ItemStack) Configuration.getConfig(Configuration.LOGOUT_ITEM))) {
			e.setCancelled(true);
			Utils.startSafeLogout(e.getPlayer());
		}
	}
}
