/**
 * 
 */
package myz.utilities;

import java.util.List;

import myz.support.interfacing.Configuration;

import org.bukkit.Location;

/**
 * @author Jordan
 * 
 */
public class Validate {

	public static boolean inWorld(Location l) {
		return ((List<String>) Configuration.getConfig(Configuration.WORLDS)).contains(l.getWorld().getName());
	}
}
