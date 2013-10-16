/**
 * 
 */
package myz.Listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import myz.MyZ;
import myz.API.PlayerDrinkWaterEvent;
import myz.Support.Configuration;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * @author Jordan
 * 
 */
public class ConsumeFood implements Listener {

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onConsume(final PlayerItemConsumeEvent e) {
		if (isFood(e.getItem())) {
			if (e.getPlayer().getHealth() + Configuration.getFoodHealthValue() <= e.getPlayer().getMaxHealth()) {
				EntityRegainHealthEvent regainEvent = new EntityRegainHealthEvent(e.getPlayer(), Configuration.getFoodHealthValue(),
						RegainReason.EATING);
				MyZ.instance.getServer().getPluginManager().callEvent(regainEvent);
				if (!regainEvent.isCancelled()) {
					e.getPlayer().setHealth(e.getPlayer().getHealth() + Configuration.getFoodHealthValue());
					addEffects(e.getPlayer(), e.getItem());
				}
			}
		} else if (e.getItem().getType() == Material.POTION && e.getItem().getDurability() == (short) 0) {
			PlayerDrinkWaterEvent event = new PlayerDrinkWaterEvent(e.getPlayer());
			MyZ.instance.getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled())
				MyZ.instance.setThirst(e.getPlayer(), Configuration.getMaxThirstLevel());
		} else if (e.getItem().getType() == Material.POTION && e.getItem().getDurability() != (short) 0
				|| e.getItem().getType() == Material.MILK_BUCKET) {
			if (e.getItem().getType() == Material.MILK_BUCKET) {
				MyZ.instance.stopPoison(e.getPlayer());
				PlayerDrinkWaterEvent event = new PlayerDrinkWaterEvent(e.getPlayer());
				MyZ.instance.getServer().getPluginManager().callEvent(event);
				if (!event.isCancelled())
					MyZ.instance.setThirst(e.getPlayer(), Configuration.getMaxThirstLevel());
			}
			MyZ.instance.getServer().getScheduler().runTaskLater(MyZ.instance, new Runnable() {
				@Override
				public void run() {
					e.getPlayer().setItemInHand(null);
				}
			}, 0L);
		} else if (e.getItem().getType() == Material.ROTTEN_FLESH && random.nextDouble() <= Configuration.getPoisonChanceFlesh()
				&& Configuration.getPoisonChanceFlesh() != 0.0)
			MyZ.instance.startPoison(e.getPlayer());
	}

	/**
	 * Whether or not the specified item is a normal food item. Does not include
	 * consumables: Rotten Flesh, Potion, Milk Bucket
	 * 
	 * @param stack
	 *            The ItemStack in question.
	 * @return True if the stack is a food item from the pre-defined list, false
	 *         otherwise.
	 */
	private boolean isFood(ItemStack stack) {
		return getFoodTypes().contains(stack.getType());
	}

	/**
	 * Get a list of all the current Minecraft food types by Material.
	 * 
	 * @return The list of materials.
	 */
	public static List<Material> getFoodTypes() {
		List<Material> foodTypes = new ArrayList<Material>();
		foodTypes.addAll(Arrays.asList(Material.APPLE, Material.BAKED_POTATO, Material.BREAD, Material.CARROT, Material.COOKED_BEEF,
				Material.COOKED_CHICKEN, Material.COOKED_FISH, Material.COOKIE, Material.GOLDEN_APPLE, Material.GRILLED_PORK,
				Material.MELON, Material.MUSHROOM_SOUP, Material.POISONOUS_POTATO, Material.PORK, Material.POTATO, Material.PUMPKIN_PIE,
				Material.RAW_BEEF, Material.RAW_CHICKEN, Material.RAW_FISH, Material.SPIDER_EYE));
		return foodTypes;
	}

	/**
	 * Add the potion effects and thirst value specified in the config for the
	 * given food item.
	 * 
	 * @param player
	 *            The player to effect.
	 * @param food
	 *            The food that was consumed.
	 */
	private void addEffects(Player player, ItemStack food) {
		if (!isFood(food)) { return; }
		int thirstValue = Configuration.getFoodThirstValues().get(food.getType().toString().toUpperCase()) == null ? 0 : Configuration
				.getFoodThirstValues().get(food.getType().toString().toUpperCase());
		List<PotionEffect> potionEffects = Configuration.getFoodPotionEffects().get(food.getType().toString().toUpperCase()) == null ? new ArrayList<PotionEffect>()
				: Configuration.getFoodPotionEffects().get(food.getType().toString().toUpperCase());
		MyZ.instance.setThirst(player, player.getLevel() + thirstValue);
		player.addPotionEffects(potionEffects);
	}
}
