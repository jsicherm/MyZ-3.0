/**
 * 
 */
package myz.nmscode.compat;

import myz.MyZ;
import myz.nmscode.v1_7_R1.utilities.NMSCodeUtils;

import org.bukkit.entity.Player;

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
		case v1_7_9:
			myz.nmscode.v1_7_R3.utilities.NMSCodeUtils.revive(player);
			break;
		}
	}
}
