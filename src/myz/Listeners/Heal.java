/**
 * 
 */
package myz.Listeners;

import java.util.HashMap;
import java.util.Map;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.MedKit;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.Utilities.Localizer;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Jordan
 * 
 */
public class Heal implements Listener {

	private Map<String, Long> lastHeals = new HashMap<String, Long>();

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onRightClick(PlayerInteractEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		final Player player = e.getPlayer();
		final ItemStack item = e.getItem();

		// Handle MedKits.
		if (player.getGameMode() != GameMode.CREATIVE && item != null) {
			MedKit kit;
			if ((kit = MedKit.getMedKitFor(item)) != null) {
				MyZ.instance.stopBleeding(player);
				if (kit.getAntisepticRequired() == 0 && kit.getOintmentRequired() == 0) {
					if (player.getHealth() + 1 <= player.getMaxHealth())
						player.setHealth(player.getHealth() + 1);
					else
						Messenger.sendConfigMessage(player, "heal.waste");
				} else {
					int antiLevel = kit.getAntisepticRequired(), regenLevel = kit.getOintmentRequired();
					if (regenLevel != 0)
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenLevel * 40, regenLevel));
					if (antiLevel != 0) {
						MyZ.instance.stopPoison(player);
						player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, antiLevel * 100, antiLevel));
					}
				}
				if (item.getAmount() != 1)
					item.setAmount(item.getAmount() - 1);
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
		if (player.getGameMode() != GameMode.CREATIVE && item != null
				&& item.isSimilar(Configuration.getBandageItem() != null ? Configuration.getBandageItem() : new ItemStack(Material.PAPER))) {

			MyZ.instance.stopBleeding(player);
			if (player.getHealth() + Configuration.getBandageHealAmount() <= player.getMaxHealth()) {
				EntityRegainHealthEvent regainEvent = new EntityRegainHealthEvent(player, Configuration.getBandageHealAmount(),
						RegainReason.CUSTOM);
				MyZ.instance.getServer().getPluginManager().callEvent(regainEvent);
				if (!regainEvent.isCancelled())
					player.setHealth(player.getHealth() + Configuration.getBandageHealAmount());
			} else
				Messenger.sendConfigMessage(player, "heal.waste");

			if (item.getAmount() != 1)
				item.setAmount(item.getAmount() - 1);
			else
				MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
					@Override
					public void run() {
						player.setItemInHand(null);
					}
				}, 0L);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onRightClickOther(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof Player))
			return;
		final Player player = (Player) e.getRightClicked();
		final Player healer = e.getPlayer();
		final ItemStack item = healer.getItemInHand();
		boolean flag = false;
		long now = System.currentTimeMillis();

		// Handle MedKits.
		if (player.getGameMode() != GameMode.CREATIVE && item != null) {
			MedKit kit;
			if ((kit = MedKit.getMedKitFor(item)) != null) {
				if (lastHeals.containsKey(healer.getName())
						&& (now - lastHeals.get(healer.getName())) / 1000 < Configuration.getHealDelay())
					Messenger.sendMessage(
							healer,
							Messenger.getConfigMessage(Localizer.getLocale(player), "heal.wait", Configuration.getHealDelay()
									- (now - lastHeals.get(healer.getName())) / 1000));
				else {
					MyZ.instance.stopBleeding(player);
					if (kit.getAntisepticRequired() == 0 && kit.getOintmentRequired() == 0) {
						if (player.getHealth() + 1 <= player.getMaxHealth())
							player.setHealth(player.getHealth() + 1);
						else
							Messenger.sendConfigMessage(player, "heal.waste");
					} else {
						int antiLevel = kit.getAntisepticRequired(), regenLevel = kit.getOintmentRequired();
						if (regenLevel != 0)
							player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenLevel * 40, regenLevel));
						if (antiLevel != 0) {
							MyZ.instance.stopPoison(player);
							player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, antiLevel * 100, antiLevel));
						}
					}
					if (item.getAmount() != 1)
						item.setAmount(item.getAmount() - 1);
					else
						MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
							@Override
							public void run() {
								player.setItemInHand(null);
							}
						}, 0L);
				}
				flag = true;
			}
		} else if (player.getGameMode() != GameMode.CREATIVE && item != null
				&& item.isSimilar(Configuration.getBandageItem() != null ? Configuration.getBandageItem() : new ItemStack(Material.PAPER))) {
			if (lastHeals.containsKey(healer.getName()) && (now - lastHeals.get(healer.getName())) / 1000 < Configuration.getHealDelay())
				Messenger.sendMessage(
						healer,
						Messenger.getConfigMessage(Localizer.getLocale(player), "heal.wait", Configuration.getHealDelay()
								- (now - lastHeals.get(healer.getName())) / 1000));
			else {
				MyZ.instance.stopBleeding(player);
				if (player.getHealth() + Configuration.getBandageHealAmount() <= player.getMaxHealth()) {
					EntityRegainHealthEvent regainEvent = new EntityRegainHealthEvent(player, Configuration.getBandageHealAmount(),
							RegainReason.CUSTOM);
					MyZ.instance.getServer().getPluginManager().callEvent(regainEvent);
					if (!regainEvent.isCancelled())
						player.setHealth(player.getHealth() + Configuration.getBandageHealAmount());
				} else
					Messenger.sendConfigMessage(player, "heal.waste");

				if (item.getAmount() != 1)
					item.setAmount(item.getAmount() - 1);
				else
					MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
						@Override
						public void run() {
							player.setItemInHand(null);
						}
					}, 0L);
			}
			flag = true;
		}

		if (flag) {
			lastHeals.put(healer.getName(), now);

			PlayerData data = PlayerData.getDataFor(healer);
			int amount = 0;
			if (data != null)
				data.setHealsLife(amount = data.getHealsLife() + 1);
			if (MyZ.instance.getSQLManager().isConnected())
				MyZ.instance.getSQLManager().set(healer.getName(), "heals_life",
						amount = MyZ.instance.getSQLManager().getInt(healer.getName(), "heals_life") + 1, true);
			Messenger.sendMessage(healer, Messenger.getConfigMessage(Localizer.getLocale(player), "heal.amount", amount));
			if (MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI") != null
					&& MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI").isEnabled())
				KittehTag.colorName(healer);
		}
	}
}
