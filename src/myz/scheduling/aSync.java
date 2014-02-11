/**
 * 
 */
package myz.scheduling;

import java.util.Random;

import myz.MyZ;
import myz.api.PlayerDrinkWaterEvent;
import myz.api.PlayerTakeBleedingDamageEvent;
import myz.api.PlayerTakePoisonDamageEvent;
import myz.api.PlayerTakeWaterDamageEvent;
import myz.mobs.pathing.PathingSupport;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.NMSUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Jordan
 * 
 */
public class aSync implements Runnable {

	private static int ticks = 0;
	private Random random = new Random();
	private final boolean isDisguise;

	public aSync() {
		isDisguise = MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled();
	}

	@Override
	public void run() {
		ticks++;

		for (String world : MyZ.instance.getWorlds()) {
			if (Bukkit.getWorld(world) == null) {
				Messenger.sendConsoleMessage("&4Specified world (" + world + ") does not exist! Please update your config.yml");
				continue;
			}
			for (Player player : MyZ.instance.getServer().getWorld(world).getPlayers()) {
				if (player.getGameMode() == GameMode.CREATIVE || Configuration.isInLobby(player))
					continue;

				// Increment minutes alive.
				if (ticks % 60 == 0) {
					PlayerData data = PlayerData.getDataFor(player);
					int amount;
					if (data != null) {
						data.setMinutesAlive(data.getMinutesAlive() + 1);
						data.setMinutesAliveLife(amount = data.getMinutesAliveLife() + 1);
						if (amount > data.getMinutesAliveLifeRecord())
							data.setMinutesAliveLifeRecord(amount);
					}
					if (MyZ.instance.getSQLManager().isConnected()) {
						MyZ.instance.getSQLManager().set(player.getName(), "minutes_alive",
								MyZ.instance.getSQLManager().getLong(player.getName(), "minutes_alive") + 1, true);
						MyZ.instance.getSQLManager().set(player.getName(), "minutes_alive_life",
								amount = MyZ.instance.getSQLManager().getInt(player.getName(), "minutes_alive_life") + 1, true);
						if (amount > MyZ.instance.getSQLManager().getInt(player.getName(), "minutes_alive_record"))
							MyZ.instance.getSQLManager().set(player.getName(), "minutes_alive_record", amount, true);
					}
				}

				if (isDisguise)
					if (myz.utilities.DisguiseUtils.isZombie(player))
						continue;

				player.setExp((float) PathingSupport.experienceBarVisibility(player) / 18);

				// Increase thirst level by swimming or by standing in the rain.
				boolean isRaining = false;
				Object nmsplayer = NMSUtils.castToNMS(player);
				if (nmsplayer != null)
					try {
						isRaining = (Boolean) nmsplayer
								.getClass()
								.getMethod("isRainingAt", int.class, int.class, int.class)
								.invoke(nmsplayer, player.getLocation().getBlockX(), player.getLocation().getBlockY(),
										player.getLocation().getBlockZ());
					} catch (Exception exc) {
					}
				if (player.getLevel() < Configuration.getMaxThirstLevel()
						&& (player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid() || isRaining
								&& noBlocksAbove(player.getLocation())))
					if (ticks % (random.nextInt(2) + 1) == 0) {
						PlayerDrinkWaterEvent event = new PlayerDrinkWaterEvent(player);
						if (!event.isCancelled())
							MyZ.instance.setThirst(player, player.getLevel() + 1);
					}

				// Take bleeding damage.
				if (ticks % Configuration.getBleedDamageFrequency() == 0 && MyZ.instance.isBleeding(player)
						&& player.getHealth() > Configuration.getBleedDamage()) {
					PlayerTakeBleedingDamageEvent event = new PlayerTakeBleedingDamageEvent(player);
					if (!event.isCancelled()) {
						player.damage(Configuration.getBleedDamage());
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1, 20));
						Messenger.sendConfigMessage(player, "damage.bleed_begin");
					}
				}

				// Take poison damage.
				if ((ticks + 11) % Configuration.getPoisonDamageFrequency() == 0 && MyZ.instance.isPoisoned(player)
						&& player.getHealth() > Configuration.getPoisonDamage()) {
					PlayerTakePoisonDamageEvent event = new PlayerTakePoisonDamageEvent(player);
					if (!event.isCancelled()) {
						player.damage(Configuration.getPoisonDamage());
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1, 20));
						player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 2, 80));
						Messenger.sendConfigMessage(player, "damage.poison_begin");
					}
				}

				// Take thirst decay and damage.
				if ((ticks + 22) % Configuration.getWaterDecreaseTime() == 0)
					if (player.getLevel() > 0)
						MyZ.instance.setThirst(player, player.getLevel() - 1);
					else if (player.getHealth() > Configuration.getWaterDamage()) {
						PlayerTakeWaterDamageEvent event = new PlayerTakeWaterDamageEvent(player);
						if (!event.isCancelled())
							player.damage(Configuration.getWaterDamage());
					}
			}

			// Ensure we don't exceed the integer size.
			if (ticks >= Integer.MAX_VALUE)
				ticks = 0;
		}
	}

	/**
	 * Check if there are any blocks above the specified location. Searches from
	 * this location's y to max build height.
	 * 
	 * @param loc
	 *            The location.
	 * @return False if there are any blocks above the given location.
	 */
	private boolean noBlocksAbove(Location loc) {
		for (int y = loc.getBlockY(); y < loc.getWorld().getMaxHeight(); y++)
			if (loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() != Material.AIR)
				return false;
		return true;
	}
}
