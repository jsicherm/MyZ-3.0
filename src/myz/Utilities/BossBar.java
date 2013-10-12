/**
 * 
 */
package myz.Utilities;

import java.lang.reflect.Field;
import java.util.HashMap;

import myz.MyZ;
import net.minecraft.server.v1_6_R3.DataWatcher;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet24MobSpawn;
import net.minecraft.server.v1_6_R3.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author chasechocolate
 * 
 */
public class BossBar {

	public static final int ENTITY_ID = 1234;
	private static HashMap<String, Boolean> hasHealthBar = new HashMap<String, Boolean>();

	public static void sendPacket(Player player, Packet packet) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		entityPlayer.playerConnection.sendPacket(packet);
	}

	// Accessing packets
	@SuppressWarnings("deprecation")
	private static Packet24MobSpawn getMobPacket(String text, Location loc) {
		Packet24MobSpawn mobPacket = new Packet24MobSpawn();
		mobPacket.a = (int) ENTITY_ID; // Entity ID
		mobPacket.b = (byte) EntityType.WITHER.getTypeId(); // Mob type (ID: 64)
		mobPacket.c = (int) Math.floor(loc.getBlockX() * 32.0D); // X position
		mobPacket.d = (int) Math.floor(loc.getBlockY() * 32.0D); // Y position
		mobPacket.e = (int) Math.floor(loc.getBlockZ() * 32.0D); // Z position
		mobPacket.f = (byte) 0; // Pitch
		mobPacket.g = (byte) 0; // Head Pitch
		mobPacket.h = (byte) 0; // Yaw
		mobPacket.i = (short) 0; // X velocity
		mobPacket.j = (short) 0; // Y velocity
		mobPacket.k = (short) 0; // Z velocity
		DataWatcher watcher = getWatcher(text, 300);
		try {
			Field t = Packet24MobSpawn.class.getDeclaredField("t");
			t.setAccessible(true);
			t.set(mobPacket, watcher);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mobPacket;
	}

	private static Packet29DestroyEntity getDestroyEntityPacket() {
		Packet29DestroyEntity packet = new Packet29DestroyEntity();
		packet.a = new int[] { ENTITY_ID };
		return packet;
	}

	private static Packet40EntityMetadata getMetadataPacket(DataWatcher watcher) {
		Packet40EntityMetadata metaPacket = new Packet40EntityMetadata();
		metaPacket.a = (int) ENTITY_ID;
		try {
			Field b = Packet40EntityMetadata.class.getDeclaredField("b");
			b.setAccessible(true);
			b.set(metaPacket, watcher.c());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metaPacket;
	}

	private static DataWatcher getWatcher(String text, int health) {
		DataWatcher watcher = new DataWatcher();
		watcher.a(0, (Byte) (byte) 0x20); // Flags, 0x20 = invisible
		watcher.a(6, (Float) (float) health);
		watcher.a(10, (String) text); // Entity name
		watcher.a(11, (Byte) (byte) 1); // Show name, 1 = show, 0 = don't show
		// watcher.a(16, (Integer) (int) health); //Wither health, 300 = full
		// health
		return watcher;
	}

	// Other methods
	public static void displayTextBar(String text, final Player player) {
		Packet24MobSpawn mobPacket = getMobPacket(text, player.getLocation());
		sendPacket(player, mobPacket);
		hasHealthBar.put(player.getName(), true);
		new BukkitRunnable() {
			@Override
			public void run() {
				Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
				sendPacket(player, destroyEntityPacket);
				hasHealthBar.put(player.getName(), false);
			}
		}.runTaskLater(MyZ.instance, 120L);
	}

	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int healthAdd,
			final long delay, final boolean loadUp) {
		Packet24MobSpawn mobPacket = getMobPacket(text, player.getLocation());
		sendPacket(player, mobPacket);
		hasHealthBar.put(player.getName(), true);
		new BukkitRunnable() {
			int health = (loadUp ? 0 : 300);

			@Override
			public void run() {
				if ((loadUp ? health < 300 : health > 0)) {
					DataWatcher watcher = getWatcher(text, health);
					Packet40EntityMetadata metaPacket = getMetadataPacket(watcher);
					sendPacket(player, metaPacket);
					if (loadUp) {
						health += healthAdd;
					} else {
						health -= healthAdd;
					}
				} else {
					DataWatcher watcher = getWatcher(text, (loadUp ? 300 : 0));
					Packet40EntityMetadata metaPacket = getMetadataPacket(watcher);
					Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
					sendPacket(player, metaPacket);
					sendPacket(player, destroyEntityPacket);
					hasHealthBar.put(player.getName(), false);
					// Complete text
					Packet24MobSpawn mobPacket = getMobPacket(completeText, player.getLocation());
					sendPacket(player, mobPacket);
					hasHealthBar.put(player.getName(), true);
					DataWatcher watcher2 = getWatcher(completeText, 300);
					Packet40EntityMetadata metaPacket2 = getMetadataPacket(watcher2);
					sendPacket(player, metaPacket2);
					new BukkitRunnable() {
						@Override
						public void run() {
							Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
							sendPacket(player, destroyEntityPacket);
							hasHealthBar.put(player.getName(), false);
						}
					}.runTaskLater(MyZ.instance, 40L);
					this.cancel();
				}
			}
		}.runTaskTimer(MyZ.instance, delay, delay);
	}

	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int secondsDelay,
			final boolean loadUp) {
		final int healthChangePerSecond = 300 / secondsDelay;
		displayLoadingBar(text, completeText, player, healthChangePerSecond, 20L, loadUp);
	}
}
