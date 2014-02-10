/**
 * 
 */
package myz.Utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public class NMS {

	private static final String packageName = Bukkit.getServer().getClass().getPackage().getName();
	public static final String version = packageName.substring(packageName.lastIndexOf(".") + 1);
	private static Class<?> craftPlayer, packet;
	private static Method getHandle, sendPacket;
	private static Field connection;

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
		if (craftPlayer == null)
			try {
				craftPlayer = Class.forName("org.bukkit.craftbukkit." + NMS.version + ".entity.CraftPlayer");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		if (craftPlayer == null) { return null; }
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
		Object craft = castToCraft(player);
		if (craft == null)
			return null;
		if (getHandle == null)
			try {
				getHandle = craftPlayer.getMethod("getHandle");
			} catch (Exception exc) {
				return null;
			}
		try {
			return getHandle.invoke(castToCraft(player));
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
	 *             if an exception occurred.
	 */
	public static void sendPacket(Object inPacket, Player inPlayer) throws Exception {
		if (packet == null)
			packet = Class.forName("net.minecraft.server." + NMS.version + ".Packet");

		Object handle = castToNMS(inPlayer);
		if (handle == null) { return; }
		if (connection == null)
			connection = handle.getClass().getField("playerConnection");
		Object con = connection.get(handle);
		if (con == null) { return; }
		if (sendPacket == null)
			sendPacket = con.getClass().getMethod("sendPacket", packet);
		if (sendPacket != null)
			sendPacket.invoke(con, inPacket);
	}
}
