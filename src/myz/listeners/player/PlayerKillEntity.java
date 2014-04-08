/**
 * 
 */
package myz.listeners.player;

import java.util.Random;

import myz.MyZ;
import myz.nmscode.compat.MessageUtils;
import myz.nmscode.compat.MobUtils;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;
import myz.utilities.Validate;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author Jordan
 * 
 */
public class PlayerKillEntity implements Listener {

	private static final Random random = new Random();

	/**
	 * Increment the statistics for the given player for killing the specified
	 * entity.
	 * 
	 * @param typeFor
	 *            The Entity that was killed.
	 * @param playerFor
	 *            The Player that killed the entity.
	 * @param e
	 *            The EntityDeathEvent that drives this call.
	 */
	private void incrementKills(Entity typeFor, Player playerFor) {
		PlayerData data = PlayerData.getDataFor(playerFor);
		int amount = 0;
		String slug = "";
		String mobslug = "";
		switch (typeFor.getType()) {
		case ZOMBIE:
			if (data != null) {
				data.setZombieKillsLife(amount = data.getZombieKillsLife() + 1);
				data.setZombieKills(data.getZombieKills() + 1);
				if (amount > data.getZombieKillsLifeRecord())
					data.setZombieKillsLifeRecord(amount);
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "zombie_kills_life",
						amount = MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "zombie_kills_life") + 1, true);
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "zombie_kills",
						MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "zombie_kills") + 1, true);
				if (amount > MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "zombie_kills_life_record"))
					MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "zombie_kills_life_record", amount, true);
			}
			mobslug = slug = "zombie";
			break;
		case PIG_ZOMBIE:
			if (data != null) {
				data.setPigmanKillsLife(amount = data.getPigmanKillsLife() + 1);
				data.setPigmanKills(data.getPigmanKills() + 1);
				if (amount > data.getPigmanKillsLifeRecord())
					data.setPigmanKillsLifeRecord(amount);
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "pigman_kills_life",
						amount = MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "pigman_kills_life") + 1, true);
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "pigman_kills",
						MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "pigman_kills") + 1, true);
				if (amount > MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "pigman_kills_life_record"))
					MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "pigman_kills_life_record", amount, true);
			}
			mobslug = slug = "pigman";
			break;
		case GIANT:
			if (data != null) {
				data.setGiantKillsLife(amount = data.getGiantKillsLife() + 1);
				data.setGiantKills(data.getGiantKills() + 1);
				if (amount > data.getGiantKillsLifeRecord())
					data.setGiantKillsLifeRecord(amount);
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "giant_kills_life",
						amount = MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "giant_kills_life") + 1, true);
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "giant_kills",
						MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "giant_kills") + 1, true);
				if (amount > MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "giant_kills_life_record"))
					MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "giant_kills_life_record", amount, true);
			}
			mobslug = slug = "giant";
			break;
		case PLAYER:
			Messenger.sendFancyDeathMessage(playerFor, (Player) typeFor);

			if (data != null) {
				data.setPlayerKillsLife(amount = data.getPlayerKillsLife() + 1);
				data.setPlayerKills(data.getPlayerKills() + 1);
				if (amount > data.getPlayerKillsLifeRecord())
					data.setPlayerKillsLifeRecord(amount);
			}
			if (MyZ.instance.getSQLManager().isConnected()) {
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "player_kills_life",
						amount = MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "player_kills_life") + 1, true);
				MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "player_kills",
						MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "player_kills") + 1, true);
				if (amount > MyZ.instance.getSQLManager().getInt(playerFor.getUniqueId(), "player_kills_life_record"))
					MyZ.instance.getSQLManager().set(playerFor.getUniqueId(), "player_kills_life_record", amount, true);
			}
			if (MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI") != null
					&& MyZ.instance.getServer().getPluginManager().getPlugin("TagAPI").isEnabled())
				KittehTag.colorName(playerFor);
			mobslug = "player";
			slug = "notnull";
			break;
		default:
			break;
		}

		if (mobslug != "")
			ResearchItem.research(playerFor, (Integer) Configuration.getConfig("mobs." + mobslug + ".research-reward"),
					typeFor.getLocation(), "research.success-short");

		if (slug != "") {
			String message = slug == "notnull" ? Messenger.getConfigMessage(Localizer.getLocale(playerFor), "bandit.amount", "\n" + amount)
					: Messenger.getConfigMessage(Localizer.getLocale(playerFor), slug + ".kill_amount", "\n" + amount);
			String delimiter = message.contains(" \n") ? " \n" : "\n";
			MessageUtils.holographicDisplay(typeFor.getLocation(), playerFor, message.split(delimiter));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onEntityDeath(EntityDeathEvent e) {
		if (!Validate.inWorld(e.getEntity().getLocation()))
			return;
		e.setDroppedExp(0);
		if (e.getEntity().getKiller() != null)
			incrementKills(e.getEntity(), e.getEntity().getKiller());
		if (e.getEntity().getType() == EntityType.PIG_ZOMBIE && !((PigZombie) e.getEntity()).isBaby()
				&& (Boolean) Configuration.getConfig("mobs.pigman.pigsplosion.enabled")) {
			double chance = (Double) Configuration.getConfig("mobs.pigman.pigsplosion.chance");
			if (random.nextDouble() <= chance && chance != 0.0) {
				int min = (Integer) Configuration.getConfig("mobs.pigman.pigsplosion.min");
				int max = (Integer) Configuration.getConfig("mobs.pigman.pigsplosion.max");
				int amount = min + (int) (random.nextDouble() * (max - min + 1));
				Location location = e.getEntity().getLocation();
				while (amount > 0) {
					Location spawn = location.clone().add(random.nextInt(6) * random.nextInt(2) == 0 ? -1 : 1, 0,
							random.nextInt(6) * random.nextInt(2) == 0 ? -1 : 1);
					spawn.setY(spawn.getWorld().getHighestBlockYAt(spawn) + 1);
					MobUtils.create(spawn, EntityType.PIG_ZOMBIE, SpawnReason.CUSTOM, true, true);
					spawn.getWorld().playEffect(spawn, Effect.STEP_SOUND, 11);
					spawn.getWorld().playEffect(spawn, Effect.STEP_SOUND, 11);
					amount--;
				}
			}
		}
	}
}
