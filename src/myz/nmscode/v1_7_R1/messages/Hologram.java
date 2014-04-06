/**
 * 
 */
package myz.nmscode.v1_7_R1.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myz.MyZ;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Messenger;
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

	private static final List<Hologram> holograms = new ArrayList<Hologram>();;
	public static final double distance = 0.24;
	private List<String> lines = new ArrayList<String>();
	private Map<Integer, Integer> entities = new HashMap<Integer, Integer>();
	private boolean showing = false;
	private Player following;

	private class HologramRunnable extends BukkitRunnable {
		int ticks = 0;
		BukkitTask task;
		List<Player> players;
		Hologram holo;

		public HologramRunnable(Hologram holo, Player... players) {
			this.players = new ArrayList<Player>(Arrays.asList(players));
			this.players.remove(following);
			this.holo = holo;
		}

		@Override
		public void run() {
			ticks++;
			if (holo.showing && holo.following != null && holo.following.isOnline() && !holo.following.isDead()) {
				Location l = holo.following.getLocation();
				for (int id : holo.entities.keySet()) {
					PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(id, (int) (l.getX() * 32),
							(int) ((l.getY() + holo.entities.get(id)) * 32) + 30, (int) (l.getZ() * 32), (byte) 0, (byte) 0);
					for (Player p : players)
						((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
			}
			if (ticks >= (Integer) Configuration.getConfig("hologram.showtime") * 20 || !holo.showing) {
				holograms.remove(holo);
				following = null;
				destroy();
				task.cancel();
			}
		}
	}

	public Hologram(String... lines) {
		this.lines.addAll(Arrays.asList(lines));
	}

	public static void removeAll() {
		for (Hologram h : new ArrayList<Hologram>(holograms))
			h.destroy();
	}

	private void interrupt() {
		showing = false;
	}

	private Map<Integer, Integer> showLine(final Location loc, final String text, final Player... p) {
		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		final WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
		final EntityWitherSkull skull = new EntityWitherSkull(world);
		final EntityHorse horse = new EntityHorse(world);
		horse.setLocation(loc.getX(), loc.getY() + 55, loc.getZ(), 0, 0);
		horse.setAge(-1700000);
		horse.setCustomName(text);
		horse.setCustomNameVisible(true);

		MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
			@Override
			public void run() {
				skull.setLocation(loc.getX(), loc.getY() + 56, loc.getZ(), 0, 0);
				((CraftWorld) loc.getWorld()).getHandle().addEntity(skull);
				PacketPlayOutSpawnEntityLiving packedt = new PacketPlayOutSpawnEntityLiving(horse);
				PacketPlayOutAttachEntity pa = new PacketPlayOutAttachEntity(0, horse, skull);

				List<Player> players = new ArrayList<Player>(Arrays.asList(p));
				players.remove(following);
				for (Player player : players) {
					EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
					nmsPlayer.playerConnection.sendPacket(packedt);
					nmsPlayer.playerConnection.sendPacket(pa);
				}
			}
		}, 0L);

		map.put(skull.getId(), 56);
		map.put(horse.getId(), 55);
		return map;
	}

	private int[] toInt(Set<Integer> set) {
		int[] a = new int[set.size()];
		int i = 0;
		for (Integer val : set)
			a[i++] = val;
		return a;
	}

	public void destroy() {
		if (!showing)
			return;

		int[] ints = toInt(entities.keySet());
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ints);
		for (Player player : Bukkit.getOnlinePlayers())
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		showing = false;
	}

	public void follow() {
		if (following == null)
			return;
		for (Hologram h : new ArrayList<Hologram>(holograms))
			if (h.following.equals(following))
				h.interrupt();
		holograms.add(this);
	}

	public void setFollow(Player p) {
		following = p;
	}

	public void show(Location loc, Player... p) {
		if (showing)
			return;

		if ((Boolean) Configuration.getConfig("hologram.enabled")) {
			showing = true;
			Location first = loc.clone().add(0, lines.size() / 2 * distance, 0);
			for (int i = 0; i < lines.size(); i++) {
				entities.putAll(showLine(first.clone(), lines.get(i), p));
				first.subtract(0, distance, 0);
			}

			HologramRunnable runnable = new HologramRunnable(this, p);
			runnable.task = MyZ.instance.getServer().getScheduler().runTaskTimerAsynchronously(MyZ.instance, runnable, 0, 1);
		} else
			for (Player ps : p)
				for (String s : lines)
					Messenger.sendMessage(ps, s);
	}
}