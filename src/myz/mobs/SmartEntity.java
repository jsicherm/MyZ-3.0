/**
 * 
 */
package myz.mobs;

import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public interface SmartEntity {

	/**
	 * See a sound at a location with a variable priority.
	 * 
	 * @param location
	 *            The location.
	 * @param priority
	 *            The priority to attract with. Higher priorities take
	 *            precedence.
	 */
	public void see(Location location, int priority);
}
