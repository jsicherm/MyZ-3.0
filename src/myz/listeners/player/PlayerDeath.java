/**
 * 
 */
package myz.listeners.player;

import myz.MyZ;
import myz.utilities.Utils;
import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Does not include player-kill-player events.
 * 
 * @author Jordan
 */
public class PlayerDeath implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onDeath(PlayerDeathEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getEntity().getWorld().getName()))
			return;

		// No deathmessages for pvp.
		if (e.getEntity().getKiller() != null)
			e.setDeathMessage(null);

		// Get rid of our horse.
		for (Horse horse : e.getEntity().getWorld().getEntitiesByClass(Horse.class))
			if (horse.getOwner() != null && horse.getOwner().getName() != null
					&& horse.getOwner().getName().equals(e.getEntity().getName())) {
				horse.setOwner(null);
				horse.setTamed(false);
				horse.setDomestication(0);
			}

		// Become a zombie and teleport back to spawn to be kicked.
		Utils.spawnPlayerZombie(e.getEntity(), null);

		if (e.getDeathMessage() != null && e.getDeathMessage().contains("Skeleton"))
			e.setDeathMessage(e.getDeathMessage().replaceAll("Skeleton", "NPC"));
		e.setDroppedExp(0);
		e.getDrops().clear();

		final Player player = e.getEntity();
		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				revive(player);
			}
		}, 15L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	private void onRespawn(final PlayerRespawnEvent e) {
		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				if (e.getPlayer().isOnline())
					MyZ.instance.putPlayerAtSpawn(e.getPlayer(), true, true);
			}
		}, 10L);
	}

	/**
	 * Bypass the respawn screen and come back to life immediately.
	 * 
	 * @param p
	 *            The player to respawn immediately.
	 */
	private void revive(Player p) {
		if (!p.isDead())
			return;
		PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
		((CraftPlayer) p).getHandle().playerConnection.a(packet);
		MyZ.instance.putPlayerAtSpawn(p, true, true);
	}
}
