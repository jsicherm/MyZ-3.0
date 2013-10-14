/**
 * 
 */
package myz.mobs;

import java.lang.reflect.Field;

import myz.MyZ;
import myz.Support.Configuration;
import myz.mobs.pathing.PathfinderGoalLookAtTarget;
import myz.mobs.pathing.PathfinderGoalNearestAttackableHorseTarget;
import myz.mobs.pathing.PathfinderGoalZombieAttack;
import myz.mobs.pathing.PathingSupport;
import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityHorse;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.GenericAttributes;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.MobEffectList;
import net.minecraft.server.v1_6_R3.PathfinderGoalBreed;
import net.minecraft.server.v1_6_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R3.PathfinderGoalFollowParent;
import net.minecraft.server.v1_6_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_6_R3.PathfinderGoalPanic;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_6_R3.PathfinderGoalTame;
import net.minecraft.server.v1_6_R3.PathfinderGoalTempt;
import net.minecraft.server.v1_6_R3.StepSound;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.craftbukkit.v1_6_R3.util.UnsafeList;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;

/**
 * @author Jordan
 * 
 */
public class CustomEntityHorse extends EntityHorse {

	private int bE, bF, bP;
	private float bJ, bM, bL, bN;
	private boolean bI;

	public CustomEntityHorse(World world) {
		super(world);

		try {
			Field field = PathfinderGoalSelector.class.getDeclaredField("a");
			field.setAccessible(true);

			field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(1, new PathfinderGoalPanic(this, 1.2D));
		goalSelector.a(1, new PathfinderGoalZombieAttack(this, EntityHuman.class, Configuration.getHorseSpeed(), false));
		goalSelector.a(1, new PathfinderGoalTame(this, 1.2D));
		goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
		goalSelector.a(3, new PathfinderGoalTempt(this, 1.0D, Item.ROTTEN_FLESH.id, false));
		goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.0D));
		goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(7, new PathfinderGoalLookAtTarget(this, EntityHuman.class, 6.0F));
		goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		targetSelector.a(2, new PathfinderGoalNearestAttackableHorseTarget(this, EntityHuman.class, 0, true));
	}

	@Override
	public boolean m(Entity entity) {
		if (((Horse) getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
				|| ((Horse) getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
			return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getHorseDamage() * (isBaby() ? 0.5f : 1f));
		if (getOwnerName() == null || getOwnerName().isEmpty())
			return false;
		if (!MyZ.instance.isBandit(getOwnerName()))
			return false;
		if (entity instanceof EntityHuman
				&& (getOwnerName().equals(((EntityHuman) entity).getName()) || MyZ.instance.isFriend(getOwnerName(),
						((EntityHuman) entity).getName())))
			return false;
		return entity.damageEntity(DamageSource.mobAttack(this), (float) Configuration.getHorseDamage() * (isBaby() ? 0.5f : 1f));
	}

	@Override
	protected Entity findTarget() {
		if ((getOwnerName() == null || getOwnerName().isEmpty())
				&& (((Horse) getBukkitEntity()).getVariant() != Variant.UNDEAD_HORSE && ((Horse) getBukkitEntity()).getVariant() != Variant.SKELETON_HORSE))
			return null;
		EntityHuman entityhuman = PathingSupport.findNearbyVulnerablePlayer(this);

		return entityhuman != null && this.o(entityhuman) ? entityhuman : null;
	}

	@Override
	public EntityLiving getGoalTarget() {
		if (((Horse) getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
				|| ((Horse) getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE)
			return super.getGoalTarget();
		if (getOwnerName() == null || getOwnerName().isEmpty())
			return null;
		if (!MyZ.instance.isBandit(getOwnerName()))
			return null;
		return super.getGoalTarget();
	}

	@Override
	public boolean a(EntityHuman entityhuman) {
		if (((Horse) getBukkitEntity()).getVariant() == Variant.UNDEAD_HORSE
				|| ((Horse) getBukkitEntity()).getVariant() == Variant.SKELETON_HORSE) { return false; }
		ItemStack itemstack = entityhuman.inventory.getItemInHand();

		if (itemstack != null && itemstack.id == Item.MONSTER_EGG.id)
			return super.a(entityhuman);
		else if (isTame() && bV() && entityhuman.isSneaking()) {
			this.f(entityhuman);
			return true;
		} else if (ca() && passenger != null)
			return super.a(entityhuman);
		else {
			if (itemstack != null) {
				boolean flag = false;

				if (cv()) {
					byte b0 = -1;

					if (itemstack.id == Item.HORSE_ARMOR_IRON.id)
						b0 = 1;
					else if (itemstack.id == Item.HORSE_ARMOR_GOLD.id)
						b0 = 2;
					else if (itemstack.id == Item.HORSE_ARMOR_DIAMOND.id)
						b0 = 3;

					if (b0 >= 0) {
						if (!isTame()) {
							cD();
							return true;
						}

						this.f(entityhuman);
						return true;
					}
				}

				if (!flag && !cy()) {
					float f = 0.0F;
					short short1 = 0;
					byte b1 = 0;

					if (itemstack.id == Item.WHEAT.id) {
						f = 2.0F;
						short1 = 60;
						b1 = 3;
					} else if (itemstack.id == Item.ROTTEN_FLESH.id) {
						f = 4.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.id == Item.COOKIE.id) {
						f = 5.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.id == Item.SUGAR.id) {
						f = 1.0F;
						short1 = 30;
						b1 = 3;
					} else if (itemstack.id == Item.BREAD.id) {
						f = 7.0F;
						short1 = 180;
						b1 = 3;
					} else if (itemstack.id == Block.HAY_BLOCK.id) {
						f = 20.0F;
						short1 = 180;
					} else if (itemstack.id == Item.APPLE.id) {
						f = 3.0F;
						short1 = 60;
						b1 = 3;
					} else if (itemstack.id == Item.CARROT_GOLDEN.id) {
						f = 4.0F;
						short1 = 60;
						b1 = 5;
						if (isTame() && getAge() == 0) {
							flag = true;
							bX();
						}
					} else if (itemstack.id == Item.GOLDEN_APPLE.id) {
						f = 10.0F;
						short1 = 240;
						b1 = 10;
						if (isTame() && getAge() == 0) {
							flag = true;
							bX();
						}
					}

					if (getHealth() < getMaxHealth() && f > 0.0F) {
						this.heal(f);
						flag = true;
					}

					if (!bV() && short1 > 0) {
						this.a(short1);
						flag = true;
					}

					if (b1 > 0 && (flag || !isTame()) && b1 < getMaxDomestication()) {
						flag = true;
						t(b1);
					}

					if (flag)
						cF();
				}

				if (!isTame() && !flag) {
					if (itemstack != null && itemstack.a(entityhuman, this))
						return true;

					cD();
					return true;
				}

				if (!flag && cw() && !hasChest() && itemstack.id == Block.CHEST.id) {
					setHasChest(true);
					makeSound("mob.chickenplop", 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
					flag = true;
					loadChest();
				}

				if (!flag && ca() && !co() && itemstack.id == Item.SADDLE.id) {
					this.f(entityhuman);
					return true;
				}

				if (flag) {
					if (!entityhuman.abilities.canInstantlyBuild && --itemstack.count == 0)
						entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, (ItemStack) null);

					return true;
				}
			}

			if (ca() && passenger == null) {
				if (itemstack != null && itemstack.a(entityhuman, this))
					return true;
				else {
					this.h(entityhuman);
					return true;
				}
			} else
				return super.a(entityhuman);
		}
	}

	/**
	 * BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY
	 * TO PROPERLY OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY TO PROPERLY
	 * OVERRIDE FOOD TAMING. BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD
	 * TAMING. BELOW REQUIRED ONLY TO PROPERLY OVERRIDE FOOD TAMING.
	 */

	@Override
	public boolean cw() {
		int i = getType();

		return i == 2 || i == 1;
	}

	@Override
	public boolean ca() {
		return bV();
	}

	private void h(EntityHuman entityhuman) {
		entityhuman.yaw = yaw;
		entityhuman.pitch = pitch;
		this.o(false);
		this.p(false);
		if (!world.isStatic)
			entityhuman.mount(this);
	}

	private void cF() {
		cM();
		world.makeSound(this, "eating", 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
	}

	@Override
	public void l_() {
		super.l_();
		if (world.isStatic && datawatcher.a())
			datawatcher.e();

		if (bE > 0 && ++bE > 30) {
			bE = 0;
			this.b(128, false);
		}

		if (!world.isStatic && bF > 0 && ++bF > 20) {
			bF = 0;
			this.p(false);
		}

		if (bp > 0 && ++bp > 8)
			bp = 0;

		if (bq > 0) {
			++bq;
			if (bq > 300)
				bq = 0;
		}

		if (cg()) {
			bJ += (1.0F - bJ) * 0.4F + 0.05F;
			if (bJ > 1.0F)
				bJ = 1.0F;
		} else {
			bJ += (0.0F - bJ) * 0.4F - 0.05F;
			if (bJ < 0.0F)
				bJ = 0.0F;
		}

		bM = bL;
		if (ch()) {
			bJ = 0.0F;
			bL += (1.0F - bL) * 0.4F + 0.05F;
			if (bL > 1.0F)
				bL = 1.0F;
		} else {
			bI = false;
			bL += (0.8F * bL * bL * bL - bL) * 0.6F - 0.05F;
			if (bL < 0.0F)
				bL = 0.0F;
		}

		if (w(128)) {
			bN += (1.0F - bN) * 0.7F + 0.05F;
			if (bN > 1.0F)
				bN = 1.0F;
		} else {
			bN += (0.0F - bN) * 0.7F - 0.05F;
			if (bN < 0.0F)
				bN = 0.0F;
		}
	}

	@Override
	public void u(int i) {
		if (co()) {
			if (i < 0)
				i = 0;
			else {
				bI = true;
				cO();
			}

			if (i >= 90)
				bt = 1.0F;
			else
				bt = 0.4F + 0.4F * i / 90.0F;
		}
	}

	@Override
	public void W() {
		super.W();
		if (bM > 0.0F) {
			float f = MathHelper.sin(aN * 3.1415927F / 180.0F);
			float f1 = MathHelper.cos(aN * 3.1415927F / 180.0F);
			float f2 = 0.7F * bM;
			float f3 = 0.15F * bM;

			passenger.setPosition(locX + f2 * f, locY + Y() + passenger.X() + f3, locZ - f2 * f1);
			if (passenger instanceof EntityLiving)
				((EntityLiving) passenger).aN = aN;
		}
	}

	private boolean w(int i) {
		return (datawatcher.getInt(16) & i) != 0;
	}

	private void cO() {
		if (!world.isStatic) {
			bF = 1;
			this.p(true);
		}
	}

	private void cM() {
		if (!world.isStatic) {
			bE = 1;
			this.b(128, true);
		}
	}

	private void b(int i, boolean flag) {
		int j = datawatcher.getInt(16);

		if (flag)
			datawatcher.watch(16, Integer.valueOf(j | i));
		else
			datawatcher.watch(16, Integer.valueOf(j & ~i));
	}

	@Override
	protected void a(int i, int j, int k, int l) {
		StepSound stepsound = Block.byId[l].stepSound;

		if (world.getTypeId(i, j + 1, k) == Block.SNOW.id)
			stepsound = Block.SNOW.stepSound;

		if (!Block.byId[l].material.isLiquid()) {
			int i1 = getType();

			if (passenger != null && i1 != 1 && i1 != 2) {
				++bP;
				if (bP > 5 && bP % 3 == 0) {
					makeSound("mob.horse.gallop", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
					if (i1 == 0 && random.nextInt(10) == 0)
						makeSound("mob.horse.breathe", stepsound.getVolume1() * 0.6F, stepsound.getVolume2());
				} else if (bP <= 5)
					makeSound("mob.horse.wood", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
			} else if (stepsound == Block.h)
				makeSound("mob.horse.soft", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
			else
				makeSound("mob.horse.wood", stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
		}
	}

	@Override
	public void e(float f, float f1) {
		if (passenger != null && co()) {
			lastYaw = yaw = passenger.yaw;
			pitch = passenger.pitch * 0.5F;
			this.b(yaw, pitch);
			aP = aN = yaw;
			f = ((EntityLiving) passenger).be * 0.5F;
			f1 = ((EntityLiving) passenger).bf;
			if (f1 <= 0.0F) {
				f1 *= 0.25F;
				bP = 0;
			}

			if (onGround && bt == 0.0F && ch() && !bI) {
				f = 0.0F;
				f1 = 0.0F;
			}

			if (bt > 0.0F && !cd() && onGround) {
				motY = getJumpStrength() * bt;
				if (this.hasEffect(MobEffectList.JUMP))
					motY += (getEffect(MobEffectList.JUMP).getAmplifier() + 1) * 0.1F;

				this.j(true);
				an = true;
				if (f1 > 0.0F) {
					float f2 = MathHelper.sin(yaw * 3.1415927F / 180.0F);
					float f3 = MathHelper.cos(yaw * 3.1415927F / 180.0F);

					motX += -0.4F * f2 * bt;
					motZ += 0.4F * f3 * bt;
					makeSound("mob.horse.jump", 0.4F, 1.0F);
				}

				bt = 0.0F;
			}

			Y = 1.0F;
			aR = bg() * 0.1F;
			if (!world.isStatic) {
				this.i((float) getAttributeInstance(GenericAttributes.d).getValue());
				super.e(f, f1);
			}

			if (onGround) {
				bt = 0.0F;
				this.j(false);
			}

			aF = aG;
			double d0 = locX - lastX;
			double d1 = locZ - lastZ;
			float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

			if (f4 > 1.0F)
				f4 = 1.0F;

			aG += (f4 - aG) * 0.4F;
			aH += aG;
		} else {
			Y = 0.5F;
			aR = 0.02F;
			super.e(f, f1);
		}
	}
}
