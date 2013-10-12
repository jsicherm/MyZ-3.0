/**
 * 
 */
package myz.Utilities;

import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public class WorldlessLocation {

	private final double x, y, z;

	public WorldlessLocation(Location location) {
		this(location.getX(), location.getY(), location.getZ());
	}

	public WorldlessLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
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
		return (int) x + ", " + (int) y + ", " + (int) z;
	}
}
