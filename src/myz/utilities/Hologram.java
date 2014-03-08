/**
 * 
 */
package myz.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myz.MyZ;
import net.minecraft.server.v1_7_R1.EntityHorse;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EntityWitherSkull;
import net.minecraft.server.v1_7_R1.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_7_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_7_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Hologram {

	private static final double distance = 0.23;
	private List<String> lines = new ArrayList<String>();
	private Map<Integer, Integer> entities = new HashMap<Integer, Integer>();
	private boolean showing = false;
	private Player following;

	public Hologram(String... lines) {
		this.lines.addAll(Arrays.asList(lines));
	}

	public void show(Location loc, Player... p) {
		if (showing == true) {
			try {
				throw new Exception("Is already showing!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Location first = loc.clone().add(0, (this.lines.size() / 2) * distance, 0);
		for (int i = 0; i < this.lines.size(); i++) {
			entities.putAll(showLine(first.clone(), this.lines.get(i), p));
			first.subtract(0, distance, 0);
		}
		showing = true;

		HologramRunnable runnable = new HologramRunnable(p);
		runnable.task = MyZ.instance.getServer().getScheduler().runTaskTimerAsynchronously(MyZ.instance, runnable, 0, 1);
	}

	public void destroy() {
		if (showing == false) {
			try {
				throw new Exception("Isn't showing!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		int[] ints = toInt(entities.keySet());
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ints);
		for (Player player : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
		showing = false;
	}

	private int[] toInt(Set<Integer> set) {
		int[] a = new int[set.size()];
		int i = 0;
		for (Integer val : set)
			a[i++] = val;
		return a;
	}

	private Map<Integer, Integer> showLine(Location loc, String text, Player... p) {
		WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		EntityWitherSkull skull = new EntityWitherSkull(world);
		skull.setLocation(loc.getX(), loc.getY() + 56, loc.getZ(), 0, 0);
		((CraftWorld) loc.getWorld()).getHandle().addEntity(skull);

		EntityHorse horse = new EntityHorse(world);
		horse.setLocation(loc.getX(), loc.getY() + 55, loc.getZ(), 0, 0);
		horse.setAge(-1700000);
		horse.setCustomName(text);
		horse.setCustomNameVisible(true);
		PacketPlayOutSpawnEntityLiving packedt = new PacketPlayOutSpawnEntityLiving(horse);
		PacketPlayOutAttachEntity pa = new PacketPlayOutAttachEntity(0, horse, skull);

		List<Player> players = Arrays.asList(p);
		for (Player player : players) {
			EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
			nmsPlayer.playerConnection.sendPacket(packedt);
			nmsPlayer.playerConnection.sendPacket(pa);
		}
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(skull.getId(), 56);
		map.put(horse.getId(), 55);
		return map;
	}

	public void follow(Player p) {
		following = p;
	}

	private class HologramRunnable extends BukkitRunnable {
		int ticks = 0;
		BukkitTask task;
		List<Player> players;

		public HologramRunnable(Player... players) {
			this.players = Arrays.asList(players);
		}

		@Override
		public void run() {
			ticks++;
			if (following != null && following.isOnline() && !following.isDead()) {
				Location l = following.getLocation();
				if (showing) {
					for (int id : entities.keySet()) {
						PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(id, (int) (l.getX() * 32),
								(int) ((l.getY() + entities.get(id)) * 32) + 24, (int) (l.getZ() * 32), (byte) 0, (byte) 0);
						for (Player p : players) {
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
						}
					}
				}
			}
			if (ticks >= 100) {
				cancel();
				task.cancel();
			}
		}

		@Override
		public void cancel() {
			following = null;
			destroy();
		}
	}
}