/**
 * 
 */
package myz.nmscode.v1_7_R2.utilities;

import myz.MyZ;
import net.minecraft.server.v1_7_R2.EnumClientCommand;
import net.minecraft.server.v1_7_R2.PacketPlayInClientCommand;

import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class NMSCodeUtils {

	/**
	 * Bypass the respawn screen and come back to life immediately.
	 * 
	 * @param p
	 *            The player to respawn immediately.
	 */
	public static void revive(Player p) {
		if (!p.isDead())
			return;
		PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
		((CraftPlayer) p).getHandle().playerConnection.a(packet);
		MyZ.instance.putPlayerAtSpawn(p, true, true);
	}
}
