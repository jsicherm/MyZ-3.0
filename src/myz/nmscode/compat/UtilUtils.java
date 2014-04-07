/**
 * 
 */
package myz.nmscode.compat;

import org.bukkit.entity.Player;

import myz.MyZ;
import myz.nmscode.v1_7_R1.utilities.NMSCodeUtils;

/**
 * @author Jordan
 * 
 */
public class UtilUtils {

	public static void revive(Player player) {
		switch (MyZ.version) {
		case v1_7_2:
			NMSCodeUtils.revive(player);
			break;
		case v1_7_5:
			myz.nmscode.v1_7_R2.utilities.NMSCodeUtils.revive(player);
			break;
		}
	}
}
