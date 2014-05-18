/**
 * 
 */
package myz.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import myz.MyZ;
import myz.api.PlayerDrinkWaterEvent;
import myz.api.PlayerTakeBleedingDamageEvent;
import myz.api.PlayerTakePoisonDamageEvent;
import myz.api.PlayerTakeWaterDamageEvent;
import myz.nmscode.compat.PathUtils;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
import myz.utilities.NMSUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
	private static final Random random = new Random();
	private final boolean isDisguise, isDisguise2;

	public aSync() {
		isDisguise = MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("DisguiseCraft").isEnabled();

		isDisguise2 = MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises") != null
				&& MyZ.instance.getServer().getPluginManager().getPlugin("LibsDisguises").isEnabled();
	}

	/**
	 * Get a list of online players synchronously.
	 * 
	 * @param world
	 *            The name of the world to get players in.
	 * @return The list of players.
	 */
	private List<Player> getSyncPlayers(final String world) {
		Future<List<Player>> f = MyZ.instance.getServer().getScheduler().callSyncMethod(MyZ.instance, new Callable<List<Player>>() {
			@Override
			public List<Player> call() throws Exception {
				return MyZ.instance.getServer().getWorld(world).getPlayers();
			}
		});
		try {
			return f.get();
		} catch (Exception exc) {
			return new ArrayList<Player>();
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
		for (int y = loc.getBlockY(); y < loc.getBlockY() + 30; y++) {
			Block b;
			if ((b = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ())) != null && b.getType() != Material.AIR)
				return false;
		}
		return true;
	}

	/**
	 * Force a sync check of the location surrounding the player to see if it's
	 * liquidy around them.
	 * 
	 * @param player
	 *            The player.
	 * @param isRaining
	 *            Whether or not it's raining where they are.
	 */
	private void syncLiquidCheck(final Player player, final boolean isRaining) {
		// MyZ.instance.getServer().getScheduler().runTask(MyZ.instance, new
		// Runnable() {
		// public void run() {
		if (player.getLevel() < (Integer) Configuration.getConfig(Configuration.THIRST_MAX)
				&& (player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid() || isRaining
						&& noBlocksAbove(player.getLocation())))
			if (ticks % (random.nextInt(2) + 1) == 0) {
				PlayerDrinkWaterEvent event = new PlayerDrinkWaterEvent(player);
				if (!event.isCancelled())
					MyZ.instance.setThirst(player, player.getLevel() + 1);
			}
		// }
		// });
	}

	@Override
	public void run() {
		ticks++;

		for (String world : (List<String>) Configuration.getConfig(Configuration.WORLDS)) {
			if (Bukkit.getWorld(world) == null) {
				Messenger.sendConsoleMessage("&4Specified world (" + world + ") does not exist! Please update your config.yml");
				continue;
			}
			for (final Player player : getSyncPlayers(world)) {
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
						MyZ.instance.getSQLManager().set(player.getUniqueId(), "minutes_alive",
								MyZ.instance.getSQLManager().getLong(player.getUniqueId(), "minutes_alive") + 1, true);
						MyZ.instance.getSQLManager().set(player.getUniqueId(), "minutes_alive_life",
								amount = MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "minutes_alive_life") + 1, true);
						if (amount > MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "minutes_alive_record"))
							MyZ.instance.getSQLManager().set(player.getUniqueId(), "minutes_alive_record", amount, true);
					}
				}

				if (isDisguise)
					if (myz.utilities.DisguiseUtils.isZombie(player))
						continue;

				if (isDisguise2)
					if (myz.utilities.LibsDisguiseUtils.isZombie(player))
						continue;

				player.setExp(PathUtils.expVisibility(player));

				MyZ.instance.getServer().getScheduler().runTask(MyZ.instance, new Runnable() {
					@Override
					public void run() {
						// Increase thirst level by swimming or by standing in
						// the rain.
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
						syncLiquidCheck(player, isRaining);
					}
				});

				// Take bleeding damage.
				if (ticks % (Integer) Configuration.getConfig("damage.bleed_damage_frequency") == 0 && MyZ.instance.isBleeding(player)
						&& player.getHealth() > (Integer) Configuration.getConfig("damage.bleed_damage")) {
					PlayerTakeBleedingDamageEvent event = new PlayerTakeBleedingDamageEvent(player);
					if (!event.isCancelled()) {
						player.damage((Integer) Configuration.getConfig("damage.bleed_damage"));
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1, 20));
						Messenger.sendConfigMessage(player, "damage.bleed_begin");
					}
				}

				// Take poison damage.
				if ((ticks + 11) % (Integer) Configuration.getConfig("damage.poison_damage_frequency") == 0
						&& MyZ.instance.isPoisoned(player)
						&& player.getHealth() > (Integer) Configuration.getConfig("damage.poison_damage")) {
					PlayerTakePoisonDamageEvent event = new PlayerTakePoisonDamageEvent(player);
					if (!event.isCancelled()) {
						player.damage((Integer) Configuration.getConfig("damage.poison_damage"));
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1, 20));
						player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 2, 80));
						Messenger.sendConfigMessage(player, "damage.poison_begin");
					}
				}

				// Take thirst decay and damage.
				if ((ticks + 22) % (Integer) Configuration.getConfig(Configuration.THIRST_DECAY) == 0)
					if (player.getLevel() > 0)
						MyZ.instance.setThirst(player, player.getLevel() - 1);
					else if (player.getHealth() > (Integer) Configuration.getConfig("damage.water_damage")) {
						PlayerTakeWaterDamageEvent event = new PlayerTakeWaterDamageEvent(player);
						if (!event.isCancelled())
							player.damage((Integer) Configuration.getConfig("damage.water_damage"));
					}
			}

			// Ensure we don't exceed the integer size.
			if (ticks >= Integer.MAX_VALUE)
				ticks = 0;
		}
	}
}
