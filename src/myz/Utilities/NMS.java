/**
 * 
 */
package myz.Utilities;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class NMS {

	private static final String packageName = Bukkit.getServer().getClass().getPackage().getName();
	public static final String version = packageName.substring(packageName.lastIndexOf(".") + 1);

	public static void setDeclaredField(Object obj, String fieldName, Object value) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(obj, value);
			f.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to cast a player to a CraftPlayer.
	 * 
	 * @param player
	 *            The bukkit player.
	 * @return The Craft alternative or null if the class for CraftPlayer could
	 *         not be found.
	 */
	public static Object castToCraft(Player player) {
		Class<?> craftPlayer;
		try {
			craftPlayer = Class.forName("org.bukkit.craftbukkit." + NMS.version + ".entity.CraftPlayer");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return craftPlayer.cast(player);
	}

	/**
	 * Try to cast a player to a NMS player.
	 * 
	 * @param player
	 *            The bukkit player.
	 * @return The NMS alternative or null if problems with reflection.
	 */
	public static Object castToNMS(Player player) {
		Class<?> craftPlayer;
		try {
			craftPlayer = Class.forName("org.bukkit.craftbukkit." + NMS.version + ".entity.CraftPlayer");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		Object craft = castToCraft(player);
		if (craft == null) { return null; }
		try {
			return craftPlayer.getMethod("getHandle").invoke(castToCraft(player));
		} catch (Exception exc) {
			return null;
		}
	}

	/**
	 * Send a player a packet.
	 * 
	 * @param inPacket
	 *            The packet.
	 * @param inPlayer
	 *            The player.
	 * @throws Exception
	 *             if an exception occured.
	 */
	public static void sendPacket(Object inPacket, Player inPlayer) throws Exception {
		Class<?> packet = Class.forName("net.minecraft.server." + NMS.version + ".Packet");

		Object handle = castToNMS(inPlayer);
		Object con = handle.getClass().getField("playerConnection").get(handle);
		con.getClass().getMethod("sendPacket", packet).invoke(con, inPacket);
	}
}
