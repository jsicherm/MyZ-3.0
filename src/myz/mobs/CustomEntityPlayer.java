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
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityArrow;
import net.minecraft.server.v1_6_R3.EntityDamageSource;
import net.minecraft.server.v1_6_R3.EntityHorse;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.EntityWolf;
import net.minecraft.server.v1_6_R3.EnumGamemode;
import net.minecraft.server.v1_6_R3.MinecraftServer;
import net.minecraft.server.v1_6_R3.MobEffectList;
import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;
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

	/**
	 * Set this player's inventory contents.
	 * 
	 * @param inventory
	 *            The list of items to set.
	 */
	public void setInventory(List<org.bukkit.inventory.ItemStack> inventory) {
		inventoryItems = inventory;
	}

	@Override
	public void die(DamageSource source) {
		Utilities.playerNPCDied(this);
		Utilities.spawnPlayerZombie(getBukkitEntity(), inventoryItems);

		EntityLiving entityliving = aS();
		if (entityliving != null)
			entityliving.b(this, bb);
		this.a(StatisticList.y, 1);
		dead = true;
		getBukkitEntity().remove();
	}

	@Override
	public void die() {
		Utilities.playerNPCDied(this);
		Utilities.spawnPlayerZombie(getBukkitEntity(), inventoryItems);

		EntityLiving entityliving = aS();
		if (entityliving != null)
			entityliving.b(this, bb);
		this.a(StatisticList.y, 1);
		dead = true;
		getBukkitEntity().remove();
	}

	@Override
	public void g(double x, double y, double z) {
		motX += x;
		motY += y;
		motZ += z;
		an = true;
	}

	@Override
	public void l_() {
		// Taken from RemoteEntities#RemotePlayerEntity.java
		yaw = az;
		super.l_();
		this.h();

		if (noDamageTicks > 0)
			noDamageTicks--;

		// Taken from Citizens2#EntityHumanNPC.java#129 - #138 - slightly
		// modified.
		if (Math.abs(motX) < 0.001F && Math.abs(motY) < 0.001F && Math.abs(motZ) < 0.001F)
			motX = motY = motZ = 0;

		// applyMovement();
		// End Citizens
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		if (isInvulnerable())
			return false;
		else if (world.isStatic)
			return false;
		else {
			// EntityPlayer default start.
			boolean flag = server.V() && server.getPvP() && "fall".equals(damagesource.translationIndex);

			if (!flag && invulnerableTicks > 0 && !damagesource.ignoresInvulnerability())
				return false;
			else if (damagesource instanceof EntityDamageSource) {
				Entity entity = damagesource.getEntity();

				if (entity instanceof EntityHuman && !this.a((EntityHuman) entity))
					return false;

				if (entity instanceof EntityArrow) {
					EntityArrow entityarrow = (EntityArrow) entity;

					if (entityarrow.shooter instanceof EntityHuman && !this.a((EntityHuman) entityarrow.shooter))
						return false;
				}
			}

			aV = 0;
			if (getHealth() <= 0.0F)
				return false;
			else if (damagesource.m() && this.hasEffect(MobEffectList.FIRE_RESISTANCE))
				return false;
			else {
				// EntityHuman default start.
				if (isSleeping() && !world.isStatic)
					this.a(true, true, false);

				if (damagesource.p()) {
					if (world.difficulty == 0)
						f = 0.0F;

					if (world.difficulty == 1)
						f = f / 2.0F + 1.0F;

					if (world.difficulty == 3)
						f = f * 3.0F / 2.0F;
				}

				if (f == 0.0F)
					return false;
				else {
					Entity entity = damagesource.getEntity();

					if (entity instanceof EntityArrow && ((EntityArrow) entity).shooter != null)
						entity = ((EntityArrow) entity).shooter;

					this.a(StatisticList.x, Math.round(f * 10.0F));
				}
				// EntityHuman default end.

				if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
					this.getEquipment(4).damage((int) (f * 4.0F + random.nextFloat() * f * 2.0F), this);
					f *= 0.75F;
				}

				aG = 1.5F;
				flag = true;

				if (noDamageTicks > maxNoDamageTicks / 2.0F) {
					if (f <= lastDamage)
						return false;

					this.d(damagesource, f - lastDamage);
					lastDamage = f;
					flag = false;
				} else {
					lastDamage = f;
					ax = getHealth();
					noDamageTicks = maxNoDamageTicks;
					this.d(damagesource, f);
					hurtTicks = az = 10;
				}

				aA = 0.0F;
				Entity entity = damagesource.getEntity();

				if (entity != null) {
					if (entity instanceof EntityLiving)
						this.b((EntityLiving) entity);

					if (entity instanceof EntityHuman) {
						lastDamageByPlayerTime = 100;
						killer = (EntityHuman) entity;
					} else if (entity instanceof EntityWolf) {
						EntityWolf entitywolf = (EntityWolf) entity;

						if (entitywolf.isTamed()) {
							lastDamageByPlayerTime = 100;
							killer = null;
						}
						// MyZ start
					} else if (entity instanceof EntityHorse) {
						EntityHorse entityhorse = (EntityHorse) entity;

						if (entityhorse.getOwnerName() != null && !entityhorse.getOwnerName().isEmpty()) {
							lastDamageByPlayerTime = 100;
							killer = null;
						}
					}
					// MyZ end
				}

				if (flag) {
					world.broadcastEntityEffect(this, (byte) 2);
					if (damagesource != DamageSource.DROWN)
						K();

					if (entity != null /* MyZ start */&& onGround /* MyZ end */) {
						double d0 = entity.locX - locX;

						double d1;

						for (d1 = entity.locZ - locZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D)
							d0 = (Math.random() - Math.random()) * 0.01D;

						aA = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - yaw;
						this.a(entity, f, d0, d1);
					} else
						aA = (int) (Math.random() * 2.0D) * 180;
				}

				if (getHealth() <= 0.0F) {
					if (flag)
						makeSound(aP(), ba(), bb());

					this.die(damagesource);
				} else if (flag)
					makeSound(aO(), ba(), bb());

				return true;
			}
		}
	}
}
