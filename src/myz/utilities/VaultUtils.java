/**
 * 
 */
package myz.utilities;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;
import myz.MyZ;

/**
 * @author Jordan
 * 
 */
public class VaultUtils {

	public static Permission permission = null;

	public static boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = MyZ.instance.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
}
