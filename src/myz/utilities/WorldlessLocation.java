/**
 * 
 */
package myz.utilities;

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

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public String toString() {
		return (int) x + ", " + (int) y + ", " + (int) z + ", " + (int) pitch + " pitch, " + (int) yaw + " yaw";
	}
}
