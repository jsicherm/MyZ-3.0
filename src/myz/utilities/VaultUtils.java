/**
 * 
 */
package myz.utilities;

import myz.MyZ;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author Jordan
 * 
 */
public class VaultUtils {

	public static Permission permission = null;

	public static boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = MyZ.instance.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null)
			permission = permissionProvider.getProvider();
		return permission != null;
	}
}
