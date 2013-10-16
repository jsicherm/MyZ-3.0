/**
 * 
 */
package myz.Listeners;

import myz.MyZ;
import myz.API.PlayerHealSelfEvent;
import myz.Support.Configuration;
import myz.Support.MedKit;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Jordan
 * 
 */
public class Heal implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onRightClickWithMedKit(PlayerInteractEvent e) {
		final Player player = e.getPlayer();
		
		// Handle MedKits.
		if (player.getGameMode() != GameMode.CREATIVE && e.getItem() != null) {
			MedKit kit;
			if ((kit = MedKit.getMedKitFor(e.getItem())) != null) {
				MyZ.instance.stopBleeding(player);
				if (kit.getAntisepticRequired() == 0 && kit.getOintmentRequired() == 0) {
					if (player.getHealth() + 1 <= player.getMaxHealth()) {
						player.setHealth(player.getHealth() + 1);
					}
				} else {
					int antiLevel = kit.getAntisepticRequired(), regenLevel = kit.getOintmentRequired();
					if (regenLevel != 0) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenLevel * 40, regenLevel));
					}
					if (antiLevel != 0) {
						MyZ.instance.stopPoison(player);
						player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, antiLevel * 100, antiLevel));
					}
				}
				if (e.getItem().getAmount() != 1)
					e.getItem().setAmount(e.getItem().getAmount() - 1);
				else
					MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
						@Override
						public void run() {
							player.setItemInHand(null);
						}
					}, 0L);
			}
		}
		
		// Handle bandage healing.
		if (e.getPlayer().getGameMode() != GameMode.CREATIVE
				&& e.getItem() != null
				&& e.getItem().isSimilar(
						Configuration.getBandageItem() != null ? Configuration.getBandageItem() : new ItemStack(Material.PAPER))) {
			PlayerHealSelfEvent event = new PlayerHealSelfEvent(player);
			MyZ.instance.getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				MyZ.instance.stopBleeding(player);
				if (player.getHealth() + Configuration.getBandageHealAmount() <= player.getMaxHealth()) {
					EntityRegainHealthEvent regainEvent = new EntityRegainHealthEvent(player, Configuration.getBandageHealAmount(),
							RegainReason.CUSTOM);
					MyZ.instance.getServer().getPluginManager().callEvent(regainEvent);
					if (!regainEvent.isCancelled())
						player.setHealth(player.getHealth() + Configuration.getBandageHealAmount());
				}

				if (e.getItem().getAmount() != 1)
					e.getItem().setAmount(e.getItem().getAmount() - 1);
				else
					MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
						@Override
						public void run() {
							player.setItemInHand(null);
						}
					}, 0L);
			}
		}
	}
}
