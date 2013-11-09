/**
 * 
 */
package myz.mobs;

import java.util.Random;

/**
 * @author Jordan
 * 
 */
public enum NPCType {
	FRIEND_ARCHER, FRIEND_SWORDSMAN, FRIEND_WANDERER, ENEMY_ARCHER, ENEMY_SWORDSMAN, ENEMY_WANDERER;

	private static final Random random = new Random();

	public static NPCType getRandom() {
		switch (random.nextInt(6)) {
		case 0:
			return FRIEND_ARCHER;
		case 1:
			return FRIEND_SWORDSMAN;
		case 2:
			return FRIEND_WANDERER;
		case 3:
			return ENEMY_ARCHER;
		case 4:
			return ENEMY_SWORDSMAN;
		default:
			return ENEMY_WANDERER;
		}
	}
}
