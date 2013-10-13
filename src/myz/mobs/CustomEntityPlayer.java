/**
 * 
 */
package myz.mobs;

import java.util.ArrayList;
import java.util.List;

import myz.Utilities.Utilities;
import myz.mobs.support.NullEntityNetworkManager;
import myz.mobs.support.NullNetServerHandler;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.EnumGamemode;
import net.minecraft.server.v1_6_R3.MinecraftServer;
import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;
import net.minecraft.server.v1_6_R3.PlayerInventory;
import net.minecraft.server.v1_6_R3.StatisticList;
import net.minecraft.server.v1_6_R3.World;

/**
 * @author kumpelblase2
 * 
 */
public class CustomEntityPlayer extends EntityPlayer {

	protected List<org.bukkit.inventory.ItemStack> inventoryItems = new ArrayList<org.bukkit.inventory.ItemStack>();

	public CustomEntityPlayer(MinecraftServer server, World world, String s, PlayerInteractManager iteminworldmanager) {
		super(server, world, s, iteminworldmanager);
		try {
			NetworkManager manager = new NullEntityNetworkManager(server);
			playerConnection = new NullNetServerHandler(server, manager, this);
			manager.a(playerConnection);
		} catch (Exception e) {
		}

		iteminworldmanager.setGameMode(EnumGamemode.SURVIVAL);
		noDamageTicks = 1;
	}

	public PlayerInventory getInventory() {
		return inventory;
	}

	/**
	 * Set this player's inventory contents.
	 * 
	 * @param inventory
	 *            The list of items to set.
	 */
	public void setInventory(List<org.bukkit.inventory.ItemStack> inventory) {
		this.inventoryItems = inventory;
	}

	@Override
	public void die(DamageSource source) {
		Utilities.spawnPlayerZombie(getBukkitEntity(), inventoryItems);

		EntityLiving entityliving = this.aS();
		if (entityliving != null) {
			entityliving.b(this, this.bb);
		}
		this.a(StatisticList.y, 1);
	}

	@Override
	public void die() {
		Utilities.spawnPlayerZombie(getBukkitEntity(), inventoryItems);

		EntityLiving entityliving = this.aS();
		if (entityliving != null) {
			entityliving.b(this, this.bb);
		}
		this.a(StatisticList.y, 1);
	}

	@Override
	public void g(double x, double y, double z) {
		this.motX += x;
		this.motY += y;
		this.motZ += z;
		this.an = true;
	}
}
