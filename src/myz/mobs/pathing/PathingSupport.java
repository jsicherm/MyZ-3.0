/**
 * 
 */
package myz.mobs.pathing;

import myz.MyZ;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityCreature;
import net.minecraft.server.v1_6_R3.EntityHorse;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.PathEntity;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 * 
 */
public class PathingSupport {

	/**
	 * @see findNearbyVulnerablePlayer(Entity entity, double x, double y, double
	 *      z)
	 */
	public static EntityHuman findNearbyVulnerablePlayer(Entity entity) {
		return findNearbyVulnerablePlayer(entity, entity.locX, entity.locY, entity.locZ);
	}

	/**
	 * A modified version of NMS findNearbyVulnerablePlayer(double d1, double
	 * d2, double d3, double d0) Returns the nearest player to the location,
	 * taking movement factors into account.
	 * 
	 * @param entity
	 *            The Entity.
	 * @param x
	 *            The x location.
	 * @param y
	 *            The y location.
	 * @param z
	 *            The z location.
	 * @return The nearest EntityHuman or null if none are nearby.
	 */
	private static EntityHuman findNearbyVulnerablePlayer(Entity entity, double x, double y, double z) {
		World world = entity.world;
		double shortest_distance = -1.0D;
		EntityHuman entityhuman = null;

		for (int i = 0; i < world.players.size(); ++i) {
			EntityHuman player = (EntityHuman) world.players.get(i);

			if (!player.abilities.isInvulnerable && player.isAlive()) {
				// Make sure we don't target our owner if we're a horse.
				if (entity instanceof EntityHorse
						&& ((EntityHorse) entity).getOwnerName() != null
						&& (((EntityHorse) entity).getOwnerName().equals(player.getName()) || MyZ.instance.isFriend(
								((EntityHorse) entity).getOwnerName(), player.getName())))
					continue;
				// Get the players distance from the x, y, z.
				double distance_to_player_squared = player.e(x, y, z);
				double refined_radius = experienceBarVisibility((Player) player.getBukkitEntity());

				if (distance_to_player_squared < refined_radius * refined_radius
						&& (shortest_distance == -1.0D || distance_to_player_squared < shortest_distance)) {

					shortest_distance = distance_to_player_squared;
					entityhuman = player;
				}
			}
		}

		if (entity instanceof EntityInsentient) {
			((EntityInsentient) entity).setGoalTarget(entityhuman);
		}
		return entityhuman;
	}

	/**
	 * Set a creature's target.
	 * 
	 * @param creature
	 *            The creature.
	 * @param path
	 *            The PathEntity object.
	 * @param speed
	 *            The speed to move at.
	 */
	public static void setTarget(EntityCreature creature, PathEntity path, double speed) {
		creature.getNavigation().a(path, speed);
	}

	/**
	 * The number of experience bars the player must have full, depending on
	 * environmental factors.
	 * 
	 * @param player
	 *            The player.
	 * @return The number of full exp segments (full is 18, empty is 0). To set
	 *         bars, you must set this value divided by 18f.
	 */
	public static double experienceBarVisibility(Player player) {
		double total = 10;
		// Default exp bars full is 10. There are 18 total.

		CraftPlayer p = (CraftPlayer) player;

		// Sneaking players must be nearer to be seen.
		if (player.isSneaking())
			total = 6; // 6 bars of exp filled.

		// Sprinting players can be seen more easily.
		if (player.isSprinting())
			total = 16; // 16 bars of exp filled.

		// Jumping players can be seen more easily.
		if (!p.isOnGround())
			total += 2; // Add two blocks of visibility.

		// Rain reduces zombie sight slightly.
		if (p.getHandle().world.isRainingAt((int) player.getLocation().getX(), (int) player.getLocation().getY(), (int) player
				.getLocation().getZ()))
			total -= 1.75; // Subtract 1.75 blocks of visibility.

		// Night reduces zombie sight slightly.
		if (p.getHandle().world.getTime() > 12300 && p.getHandle().world.getTime() < 23850)
			total -= 1.25; // Subtract 1.25 blocks of visibility.

		// Wearing a zombie head makes you nearly invisible to zombies.
		if (p.getEquipment().getHelmet() != null && p.getEquipment().getHelmet().isSimilar(new ItemStack(Material.SKULL_ITEM, 1, (byte) 2)))
			total -= 5.5; // Subtract 5.5 blocks of visibility.

		// Invisible players must be very close to be seen.
		if (p.getHandle().isInvisible()) {
			float f = p.getHandle().bx();

			if (f < 0.1F)
				f = 0.1F;

			total *= 0.7F * f;
		}

		if (total < 0.5)
			total = 0.5;
		return total;
	}
}
