/**
 * 
 */
package myz.listeners.player;

import myz.MyZ;
import myz.api.PlayerSummonGiantEvent;
import myz.nmscode.compat.MobUtils;
import myz.support.interfacing.Messenger;
import myz.utilities.Validate;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class PlayerSummonGiant implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSummon(final BlockPlaceEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		if (e.getItemInHand() != null && e.getItemInHand().isSimilar(new ItemStack(Material.SKULL_ITEM, 1, (byte) 2))) {
			if (!e.getPlayer().hasPermission("MyZ.spawn_giant")) {
				Messenger.sendConfigMessage(e.getPlayer(), "special.giant_summon_permission");
				e.setCancelled(true);
				return;
			}
			for (int y = e.getBlockPlaced().getY() + 1; y < e.getBlockPlaced().getY() + 16; y++)
				if (e.getBlockPlaced().getWorld().getBlockAt(e.getBlockPlaced().getX(), y, e.getBlockPlaced().getZ()).getType() != Material.AIR) {
					Messenger.sendConfigMessage(e.getPlayer(), "special.giant_could_not_summon");
					e.setCancelled(true);
					return;
				}
			PlayerSummonGiantEvent event = new PlayerSummonGiantEvent(e.getPlayer(), e.getBlockPlaced().getLocation());
			MyZ.instance.getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				Messenger.sendConfigMessage(e.getPlayer(), "special.giant_summoned");
				for (Entity player : e.getPlayer().getNearbyEntities(40, 10, 40))
					if (player instanceof Player)
						Messenger.sendConfigMessage((Player) player, "special.giant_summoned");
				MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
					@Override
					public void run() {
						e.getBlockPlaced().setType(Material.AIR);
						MobUtils.create(e.getBlockPlaced().getLocation(), EntityType.GIANT, SpawnReason.NATURAL);
					}
				}, 10 * 20L);
			}
		}
	}
}
