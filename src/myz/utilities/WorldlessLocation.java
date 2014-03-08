/**
 * 
 */
package myz.utilities;

import myz.support.interfacing.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public class WorldlessLocation {

	private final double x, y, z;
	private final float pitch, yaw;

	public WorldlessLocation(Location location) {
		this(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
	}

	public WorldlessLocation(double x, double y, double z, float pitch, float yaw) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public static WorldlessLocation fromString(String spawn) {
		double x = 0, y = 0, z = 0;
		float pitch = 0, yaw = 0;
		String[] location = spawn.split(",");
		try {
			x = Integer.parseInt(location[0]);
			y = Integer.parseInt(location[1]);
			z = Integer.parseInt(location[2]);
			pitch = Float.parseFloat(location[3]);
			yaw = Float.parseFloat(location[4]);
		} catch (Exception exc) {
			Messenger.sendConsoleMessage(ChatColor.RED + "Misconfigured spawnpoint min/max entry for spawnpoint: " + spawn
					+ ". Please re-configure (perhaps you're missing ,pitch,yaw?).");
		}
		return new WorldlessLocation(x, y, z, pitch, yaw);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	@Override
	public String toString() {
		return (int) x + ", " + (int) y + ", " + (int) z + ", " + (int) pitch + " pitch, " + (int) yaw + " yaw";
	}
}
