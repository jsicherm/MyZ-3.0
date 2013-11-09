/**
 * 
 */
package myz.Scheduling;

import java.util.Random;

import myz.MyZ;
import myz.API.PlayerDrinkWaterEvent;
import myz.API.PlayerTakeBleedingDamageEvent;
import myz.API.PlayerTakePoisonDamageEvent;
import myz.API.PlayerTakeWaterDamageEvent;
import myz.Support.Configuration;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.mobs.pathing.PathingSupport;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class aSync implements Runnable {

	private static int ticks = 0;
	private static final Random random = new Random();

	@Override
	public void run() {
		ticks++;

		for (String world : MyZ.instance.getWorlds())
			for (Player player : MyZ.instance.getServer().getWorld(world).getPlayers()) {
				if (player.getGameMode() == GameMode.CREATIVE || Configuration.isInLobby(player))
					continue;

				// Increment minutes alive.
				if (ticks % 60 == 0) {
					PlayerData data = PlayerData.getDataFor(player);
					int amount;
					if (data != null) {
						data.setAutosave(false, true);
						data.setMinutesAlive(data.getMinutesAlive() + 1);
						data.setMinutesAliveLife(amount = data.getMinutesAliveLife() + 1);
						if (amount > data.getMinutesAliveLifeRecord())
							data.setMinutesAliveLifeRecord(amount);
						data.setAutosave(true, true);
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

				player.setExp((float) PathingSupport.experienceBarVisibility(player) / 18);

				// Increase thirst level by swimming or by standing in the rain.
				if (player.getLevel() < Configuration.getMaxThirstLevel()
						&& (player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid() || ((CraftPlayer) player).getHandle().world
								.isRainingAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation()
										.getBlockZ())
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
						Messenger.sendConfigMessage(player, "damage.bleed_begin");
					}
				}

				// Take poison damage.
				if ((ticks + 11) % Configuration.getPoisonDamageFrequency() == 0 && MyZ.instance.isPoisoned(player)
						&& player.getHealth() > Configuration.getPoisonDamage()) {
					PlayerTakePoisonDamageEvent event = new PlayerTakePoisonDamageEvent(player);
					if (!event.isCancelled()) {
						player.damage(Configuration.getPoisonDamage());
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
