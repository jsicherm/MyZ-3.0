/**
 * 
 */
package myz.nmscode.v1_7_R2.mobs;

import java.util.List;
import java.util.UUID;

import myz.MyZ;
import myz.nmscode.compat.CustomMob;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalLookAtTarget;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalNearestAttackableHorseTarget;
import myz.nmscode.v1_7_R2.pathfinders.PathfinderGoalZombieAttack;
import myz.nmscode.v1_7_R2.pathfinders.Support;
import myz.support.interfacing.Configuration;
import net.minecraft.server.v1_7_R2.Block;
import net.minecraft.server.v1_7_R2.Blocks;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityHorse;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.EntitySkeleton;
import net.minecraft.server.v1_7_R2.EntityVillager;
import net.minecraft.server.v1_7_R2.GenericAttributes;
import net.minecraft.server.v1_7_R2.Item;
import net.minecraft.server.v1_7_R2.ItemStack;
import net.minecraft.server.v1_7_R2.Items;
import net.minecraft.server.v1_7_R2.MathHelper;
import net.minecraft.server.v1_7_R2.MobEffectList;
import net.minecraft.server.v1_7_R2.PathfinderGoalBreed;
import net.minecraft.server.v1_7_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R2.PathfinderGoalFollowParent;
import net.minecraft.server.v1_7_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R2.PathfinderGoalPanic;
import net.minecraft.server.v1_7_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R2.PathfinderGoalTame;
import net.minecraft.server.v1_7_R2.PathfinderGoalTempt;
import net.minecraft.server.v1_7_R2.StepSound;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R2.util.UnsafeList;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Horse.Variant;

/**
 * @author Jordan
 * 
 */
public class CustomEntityHorse extends EntityHorse implements CustomMob {

	private int bE, bF, bP;
	private float bJ, bM, bL, bN, bK, bO;
	private boolean bI;

	public CustomEntityHorse(World world) {
		super(world);

		try {
			Support.getField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getSecondField().set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			Support.getSecondField().set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(1, new PathfinderGoalPanic(this, 1.2D));
		goalSelector.a(1, new PathfinderGoalZombieAttack(this, EntityHuman.class, (Double) Configuration.getConfig("mobs.horse.speed"),
				false));
		goalSelector.a(1, new PathfinderGoalZombieAttack(this, EntityVillager.class, (Double) Configuration.getConfig("mobs.horse.speed"),
				false));
		goalSelector.a(1, new PathfinderGoalZombieAttack(this, EntitySkeleton.class, (Double) Configuration.getConfig("mobs.horse.speed"),
				false));
		goalSelector.a(1, new PathfinderGoalTame(this, 1.2D));
		goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
		goalSelector.a(3,
				new PathfinderGoalTempt(this, 1.0D, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.ROTTEN_FLESH))
						.getItem(), false));
		goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.0D));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 6.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableHorseTarget(this, EntityHuman.class, 0, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableHorseTarget(this, EntityVillager.class, 0, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableHorseTarget(this, EntitySkeleton.class, 0, true));
	}

	public LivingEntity getEntity() {
		return (LivingEntity) getBukkitEntity();
	}

	public UUID getUID() {
		return getUniqueID();
	}

	public Object getWorld() {
		return world;
	}

	public void setInventory(List<org.bukkit.inventory.ItemStack> inventory) {
	}

	private void b(int i, boolean flag) {
		int j = datawatcher.getInt(16);

		if (flag)
			datawatcher.watch(16, Integer.valueOf(j | i));
		else
			datawatcher.watch(16, Integer.valueOf(j & ~i));
	}

	private void cL() {
		cS();
		world.makeSound(this, "eating", 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
	}

	private void cP() {
	}

	private void cS() {
		if (!world.isStatic) {
			bE = 1;
			this.b(128, true);
		}
	}

	private void i(EntityHuman entityhuman) {
		entityhuman.yaw = yaw;
		entityhuman.pitch = pitch;
		this.o(false);
		this.p(false);
		if (!world.isStatic)
			entityhuman.mount(this);
	}

	private boolean x(int i) {
		return (datawatcher.getInt(16) & i) != 0;
	}

	@Override
	public boolean a(EntityHuman entityhuman) {
		if (((Horse) getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
				|| ((Horse) getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
			return false;
		ItemStack itemstack = entityhuman.inventory.getItemInHand();

		if (itemstack != null && itemstack.getItem() == Items.MONSTER_EGG)
			return super.a(entityhuman);
		else if (!isTame() && this.cE())
			return false;
		else if (isTame() && cb() && entityhuman.isSneaking()) {
			this.g(entityhuman);
			return true;
		} else if (cg() && passenger != null)
			return super.a(entityhuman);
		else {
			if (itemstack != null) {
				boolean flag = false;

				if (cB()) {
					byte b0 = -1;

					if (itemstack.getItem() == Items.HORSE_ARMOR_IRON)
						b0 = 1;
					else if (itemstack.getItem() == Items.HORSE_ARMOR_GOLD)
						b0 = 2;
					else if (itemstack.getItem() == Items.HORSE_ARMOR_DIAMOND)
						b0 = 3;

					if (b0 >= 0) {
						if (!isTame()) {
							cJ();
							return true;
						}

						this.g(entityhuman);
						return true;
					}
				}

				if (!flag && !cE()) {
					float f = 0.0F;
					short short1 = 0;
					byte b1 = 0;

					if (itemstack.getItem() == Items.WHEAT) {
						f = 2.0F;
						short1 = 60;
						b1 = 3;
					} else if (itemstack.getItem() == Items.ROTTEN_FLESH) {
						f = 4.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.getItem() == Items.COOKIE) {
						f = 5.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.getItem() == Items.SUGAR) {
						f = 1.0F;
						short1 = 30;
						b1 = 3;
					} else if (itemstack.getItem() == Items.BREAD) {
						f = 7.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.getItem() == Item.getItemOf(Blocks.HAY_BLOCK)) {
						f = 20.0F;
						short1 = 180;
					} else if (itemstack.getItem() == Items.APPLE) {
						f = 3.0F;
						short1 = 60;
						b1 = 3;
					} else if (itemstack.getItem() == Items.CARROT_GOLDEN) {
						f = 4.0F;
						short1 = 60;
						b1 = 5;
						if (isTame() && getAge() == 0) {
							flag = true;
							f(entityhuman);
						}
					} else if (itemstack.getItem() == Items.GOLDEN_APPLE) {
						f = 10.0F;
						short1 = 240;
						b1 = 10;
						if (isTame() && getAge() == 0) {
							flag = true;
							f(entityhuman);
						}
					}

					if (getHealth() < getMaxHealth() && f > 0.0F) {
						this.heal(f);
						flag = true;
					}

					if (!cb() && short1 > 0) {
						this.a(short1);
						flag = true;
					}

					if (b1 > 0 && (flag || !isTame()) && b1 < getMaxDomestication()) {
						flag = true;
						v(b1);
					}

					if (flag)
						cL();
				}

				if (!isTame() && !flag) {
					if (itemstack != null && itemstack.a(entityhuman, (EntityLiving) this))
						return true;

					cJ();
					return true;
				}

				if (!flag && cC() && !hasChest() && itemstack.getItem() == Item.getItemOf(Blocks.CHEST)) {
					setHasChest(true);
					makeSound("mob.chickenplop", 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
					flag = true;
					loadChest();
				}

				if (!flag && cg() && !cu() && itemstack.getItem() == Items.SADDLE) {
					this.g(entityhuman);
					return true;
				}

				if (flag) {
					if (!entityhuman.abilities.canInstantlyBuild && --itemstack.count == 0)
						entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, (ItemStack) null);

					return true;
				}
			}

			if (cg() && passenger == null) {
				if (itemstack != null && itemstack.a(entityhuman, (EntityLiving) this))
					return true;
				else {
					this.i(entityhuman);
					return true;
				}
			} else
				return super.a(entityhuman);
		}
	}

	@Override
	public void ab() {
		super.ab();
		if (bM > 0.0F) {
			float f = MathHelper.sin(aN * 3.1415927F / 180.0F);
			float f1 = MathHelper.cos(aN * 3.1415927F / 180.0F);
			float f2 = 0.7F * bM;
			float f3 = 0.15F * bM;

			passenger.setPosition(locX + f2 * f, locY + ae() + passenger.ad() + f3, locZ - f2 * f1);
			if (passenger instanceof EntityLiving)
				((EntityLiving) passenger).aM = aM;
		}
	}

	/**
	 * BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY
	 * TO PROPERLY OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY TO PROPERLY
	 * OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD
	 * TAMING. BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD TAMING.
	 */

	@Override
	public boolean cC() {
		int i = getType();

		return i == 2 || i == 1;
	}

	@Override
	public void e(float f, float f1) {
		if (this.passenger != null && this.passenger instanceof EntityLiving && this.cu()) {
			this.lastYaw = this.yaw = this.passenger.yaw;
			this.pitch = this.passenger.pitch * 0.5F;
			this.b(this.yaw, this.pitch);
			this.aO = this.aM = this.yaw;
			f = ((EntityLiving) this.passenger).bd * 0.5F;
			f1 = ((EntityLiving) this.passenger).be;
			if (f1 <= 0.0F) {
				f1 *= 0.25F;
				this.bP = 0;
			}

			if (this.onGround && this.bt == 0.0F && this.cn() && !this.bI) {
				f = 0.0F;
				f1 = 0.0F;
			}

			if (this.bt > 0.0F && !this.cj() && this.onGround) {
				this.motY = this.getJumpStrength() * (double) this.bt;
				if (this.hasEffect(MobEffectList.JUMP)) {
					this.motY += (double) ((float) (this.getEffect(MobEffectList.JUMP).getAmplifier() + 1) * 0.1F);
				}

				this.j(true);
				this.al = true;
				if (f1 > 0.0F) {
					float f2 = MathHelper.sin(this.yaw * 3.1415927F / 180.0F);
					float f3 = MathHelper.cos(this.yaw * 3.1415927F / 180.0F);

					this.motX += (double) (-0.4F * f2 * this.bt);
					this.motZ += (double) (0.4F * f3 * this.bt);
					this.makeSound("mob.horse.jump", 0.4F, 1.0F);
				}

				this.bt = 0.0F;
			}

			this.W = 1.0F;
			this.aQ = this.bk() * 0.1F;
			if (!this.world.isStatic) {
				this.i((float) this.getAttributeInstance(GenericAttributes.d).getValue());
				super.e(f, f1);
			}

			if (this.onGround) {
				this.bt = 0.0F;
				this.j(false);
			}

			this.aE = this.aF;
			double d0 = this.locX - this.lastX;
			double d1 = this.locZ - this.lastZ;
			float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

			if (f4 > 1.0F) {
				f4 = 1.0F;
			}

			this.aF += (f4 - this.aF) * 0.4F;
			this.aG += this.aF;
		} else {
			this.W = 0.5F;
			this.aQ = 0.02F;
			super.e(f, f1);
		}
	}

	@Override
	public EntityLiving getGoalTarget() {
		if (((Horse) getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
				|| ((Horse) getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
			return super.getGoalTarget();
		if (getOwnerName() == null || getOwnerName().isEmpty())
			return null;
		if (!MyZ.instance.isBandit(MyZ.instance.getUID(getOwnerName())))
			return null;
		return super.getGoalTarget();
	}

	@Override
	public void h() {
		super.h();
		if (this.world.isStatic && this.datawatcher.a()) {
			this.datawatcher.e();
			this.cP();
		}

		if (this.bE > 0 && ++this.bE > 30) {
			this.bE = 0;
			this.b(128, false);
		}

		if (!this.world.isStatic && this.bF > 0 && ++this.bF > 20) {
			this.bF = 0;
			this.p(false);
		}

		if (this.bp > 0 && ++this.bp > 8) {
			this.bp = 0;
		}

		if (this.bq > 0) {
			++this.bq;
			if (this.bq > 300) {
				this.bq = 0;
			}
		}

		this.bK = this.bJ;
		if (this.cm()) {
			this.bJ += (1.0F - this.bJ) * 0.4F + 0.05F;
			if (this.bJ > 1.0F) {
				this.bJ = 1.0F;
			}
		} else {
			this.bJ += (0.0F - this.bJ) * 0.4F - 0.05F;
			if (this.bJ < 0.0F) {
				this.bJ = 0.0F;
			}
		}

		this.bM = this.bL;
		if (this.cn()) {
			this.bK = this.bJ = 0.0F;
			this.bL += (1.0F - this.bL) * 0.4F + 0.05F;
			if (this.bL > 1.0F) {
				this.bL = 1.0F;
			}
		} else {
			this.bI = false;
			this.bL += (0.8F * this.bL * this.bL * this.bL - this.bL) * 0.6F - 0.05F;
			if (this.bL < 0.0F) {
				this.bL = 0.0F;
			}
		}

		this.bO = this.bN;
		if (this.x(128)) {
			this.bN += (1.0F - this.bN) * 0.7F + 0.05F;
			if (this.bN > 1.0F) {
				this.bN = 1.0F;
			}
		} else {
			this.bN += (0.0F - this.bN) * 0.7F - 0.05F;
			if (this.bN < 0.0F) {
				this.bN = 0.0F;
			}
		}
	}

	private void cU() {
		if (!this.world.isStatic) {
			this.bF = 1;
			this.p(true);
		}
	}

	@Override
	public void w(int i) {
		if (cu()) {
			if (i < 0)
				i = 0;
			else {
				bI = true;
				cU();
			}

			if (i >= 90)
				bt = 1.0F;
			else
				bt = 0.4F + 0.4F * i / 90.0F;
		}
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
		StepSound stepsound = block.stepSound;

		if (this.world.getType(i, j + 1, k) == Blocks.SNOW) {
			stepsound = Blocks.SNOW.stepSound;
		}

		if (!block.getMaterial().isLiquid()) {
			int l = this.getType();

			if (this.passenger != null && l != 1 && l != 2) {
				++this.bP;
				if (this.bP > 5 && this.bP % 3 == 0) {
					this.makeSound("mob.horse.gallop", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
					if (l == 0 && this.random.nextInt(10) == 0) {
						this.makeSound("mob.horse.breathe", stepsound.getVolume1() * 0.6F, stepsound.getVolume2());
					}
				} else if (this.bP <= 5) {
					this.makeSound("mob.horse.wood", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
				}
			} else if (stepsound == Block.f) {
				this.makeSound("mob.horse.wood", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
			} else {
				this.makeSound("mob.horse.soft", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
			}
		}
	}

	@Override
	protected void aC() {
		super.aC();
		bb().b(GenericAttributes.e);
		getAttributeInstance(GenericAttributes.e).setValue((Double) Configuration.getConfig("mobs.horse.damage"));
	}

	@Override
	protected Entity findTarget() {
		if ((getOwnerName() == null || getOwnerName().isEmpty()) && ((Horse) getBukkitEntity()).getVariant() != Variant.UNDEAD_HORSE
				&& ((Horse) getBukkitEntity()).getVariant() != Variant.SKELETON_HORSE)
			return null;
		EntityHuman entityhuman = Support.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.p(entityhuman) ? entityhuman : null;
	}
}
