/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.API.PlayerHealSelfEvent;
import myz.Support.Configuration;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class BandageSelf implements Listener {

	@EventHandler
	private void onRightClickWithBandage(final PlayerInteractEvent e) {
		if (e.getPlayer().getGameMode() != GameMode.CREATIVE
				&& e.getItem() != null
				&& e.getItem().isSimilar(
						Configuration.getBandageItem() != null ? Configuration.getBandageItem() : new ItemStack(Material.PAPER))) {
			PlayerHealSelfEvent event = new PlayerHealSelfEvent(e.getPlayer());
			MyZ.instance.getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				MyZ.instance.stopBleeding(e.getPlayer());
				if (e.getPlayer().getHealth() + Configuration.getBandageHealAmount() <= e.getPlayer().getMaxHealth()) {
					EntityRegainHealthEvent regainEvent = new EntityRegainHealthEvent(e.getPlayer(), Configuration.getBandageHealAmount(),
							RegainReason.CUSTOM);
					MyZ.instance.getServer().getPluginManager().callEvent(regainEvent);
					if (!regainEvent.isCancelled())
						e.getPlayer().setHealth(e.getPlayer().getHealth() + Configuration.getBandageHealAmount());
				}

				if (e.getItem().getAmount() != 1)
					e.getItem().setAmount(e.getItem().getAmount() - 1);
				else
					MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
						@Override
						public void run() {
							e.getPlayer().setItemInHand(null);
						}
					}, 0L);
			}
		}
	}
}
