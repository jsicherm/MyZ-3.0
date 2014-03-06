/**
 * 
 */
package myz.utilities;

import java.lang.reflect.Method;

import myz.support.interfacing.Messenger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class SoulboundUtils {

	private static Class<?> itemUtils;
	private static Method soulbindItem;

	public static void soulbindItem(ItemStack item, Player player) {
		if (Bukkit.getPluginManager().getPlugin("Soulbound") == null || !Bukkit.getPluginManager().getPlugin("Soulbound").isEnabled()) { return; }
		try {
			if (itemUtils == null) {
				itemUtils = Class.forName("com.me.tft_02.soulbound.util.ItemUtils");
			}
			if (soulbindItem == null) {
				soulbindItem = itemUtils.getMethod("soulbindItem", Player.class, ItemStack.class);
			}
			soulbindItem.invoke(null, player, item);
		} catch (Exception exc) {
			Messenger.sendConsoleMessage("&4Unable to Soulbind item: " + exc.getMessage());
		}
	}
}
