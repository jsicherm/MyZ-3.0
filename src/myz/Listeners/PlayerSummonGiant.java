/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.API.PlayerSummonGiantEvent;
import myz.Support.Messenger;
import myz.mobs.CustomEntityGiantZombie;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class PlayerSummonGiant implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSummon(final BlockPlaceEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName())) { return; }
		if (!e.getPlayer().hasPermission("MyZ.spawn_giant")) {
			Messenger.sendConfigMessage(e.getPlayer(), "special.giant_summon_permission");
			e.setCancelled(true);
			return;
		}

		if (e.getItemInHand().isSimilar(new ItemStack(Material.SKULL_ITEM, 1, (byte) 2))) {
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
						World world = ((CraftWorld) e.getBlockPlaced().getWorld()).getHandle();
						CustomEntityGiantZombie zombie = new CustomEntityGiantZombie(world);
						zombie.setPosition(e.getBlockPlaced().getX(), e.getBlockPlaced().getY(), e.getBlockPlaced().getZ());
						world.addEntity(zombie);
					}
				}, 10 * 20L);
			}
		}
	}
}
