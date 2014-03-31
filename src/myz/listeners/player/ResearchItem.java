/**
 * 
 */
package myz.listeners.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import myz.MyZ;
import myz.support.PlayerData;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;
import myz.utilities.Hologram;
import myz.utilities.Utils;
import myz.utilities.Validate;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jordan
 * 
 */
public class ResearchItem implements Listener {

	private Map<UUID, UUID> lastDropped = new HashMap<UUID, UUID>();

	/**
	 * Check to see if a player has to get an increased rank. If so, increase
	 * their rank.
	 * 
	 * @param Player
	 *            The player.
	 * @param before
	 *            The points before a research.
	 * @param after
	 *            The points after a research.
	 * @param rank
	 *            The players current rank.
	 */
	private static void checkRankIncrease(Player player, int before, int after, int rank) {
		// TODO
	}

	/**
	 * This method is the same as ItemStack.equals but does not consider lore.
	 * 
	 * @param stack
	 *            The first ItemStack.
	 * @param stack1
	 *            The ItemStack to compare to.
	 * @return True if both ItemStacks are equal, apart from lore.
	 */
	private static boolean isSimilar(ItemStack stack, ItemStack stack1) {
		if (stack1.getType() == stack.getType() && stack1.getAmount() == stack.getAmount()
				&& stack1.getDurability() == stack.getDurability()) {
			ItemMeta one = stack1.getItemMeta();
			ItemMeta two = stack.getItemMeta();
			if (one == null && two == null)
				return true;
			if (one != null && two != null)
				return one.getEnchants().equals(two.getEnchants())
						&& (one.getDisplayName() != null ? one.getDisplayName().equals(two.getDisplayName()) : two.getDisplayName() == null);
		}
		return false;
	}

	/**
	 * Make a player research something for the set points.
	 * 
	 * @param player
	 *            The player.
	 * @param points
	 *            The points gained.
	 * @param location
	 *            The location to show the research message.
	 * @param slug
	 *            The config message to send.
	 * @return True if the research completed, false otherwise.
	 */
	public static boolean research(Player player, int points, org.bukkit.Location location, String slug) {
		PlayerData data = PlayerData.getDataFor(player);
		int rank = 0;
		if (data != null)
			rank = data.getRank();
		if (MyZ.instance.getSQLManager().isConnected())
			rank = MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "rank");

		if (rank < (Integer) Configuration.getConfig(Configuration.RANKED_RESEARCH))
			return false;

		Set<Integer> keys = new HashSet<Integer>();
		for (String s : MyZ.instance.getConfig().getConfigurationSection("ranks.research-multiplier").getKeys(false))
			keys.add(Integer.parseInt(s));
		double mult = (Double) Configuration.getConfig("ranks.research-multiplier." + Configuration.nearestInt(rank, keys));
		if ((int) mult <= 0)
			mult = 1.0;

		points *= mult;

		int before = 0, after;
		if (data != null)
			data.setResearchPoints((before = data.getResearchPoints()) + points);
		if (MyZ.instance.getSQLManager().isConnected())
			MyZ.instance.getSQLManager().set(player.getUniqueId(), "research",
					(before = MyZ.instance.getSQLManager().getInt(player.getUniqueId(), "research")) + points, true);
		after = before + points;
		if (points != 0) {
			checkRankIncrease(player, before, after, rank);

			String msg = Messenger.getConfigMessage(Localizer.getLocale(player), slug, points + "");
			Hologram hologram = new Hologram(msg);
			hologram.show(location.clone().subtract(0, Hologram.distance, 0), player);
		}
		return true;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onClickResearchItem(InventoryClickEvent e) {
		if (e.getInventory().getHolder() == null
				&& e.getInventory().getTitle().contains(Messenger.getConfigMessage(Localizer.DEFAULT, "science_gui", "69").split("69")[0])
				&& e.getInventory().getSize() == 9) {
			e.setCancelled(true);
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= 8) {
				int page = Integer.parseInt(e.getInventory().getTitle().substring(e.getInventory().getTitle().lastIndexOf("(") + 1)
						.replace(")", ""));
				if (e.getRawSlot() == 0) {
					Utils.showResearchDialog((Player) e.getWhoClicked(), page - 1);
					return;
				} else if (e.getRawSlot() == 8) {
					Utils.showResearchDialog((Player) e.getWhoClicked(), page + 1);
					return;
				}
				ItemStack item = e.getInventory().getItem(e.getRawSlot());
				if (item != null && MyZ.instance.getResearchConfig().getConfigurationSection("item") != null)
					for (String key : MyZ.instance.getResearchConfig().getConfigurationSection("item").getKeys(false)) {
						ItemStack configured = null;
						if ((configured = MyZ.instance.getResearchConfig().getItemStack("item." + key + ".item")) != null
								&& isSimilar(configured, item)) {
							int points = 0;
							PlayerData data = PlayerData.getDataFor(e.getWhoClicked().getUniqueId());
							if (data != null)
								points = data.getResearchPoints();
							if (MyZ.instance.getSQLManager().isConnected())
								points = MyZ.instance.getSQLManager().getInt(e.getWhoClicked().getUniqueId(), "research");

							if (points > MyZ.instance.getResearchConfig().getInt("item." + key + ".cost")) {
								if (e.getWhoClicked().getInventory().firstEmpty() >= 0)
									e.getWhoClicked().getInventory().addItem(configured.clone());
								else
									e.getWhoClicked().getWorld().dropItem(e.getWhoClicked().getLocation(), configured.clone());

								if (data != null)
									data.setResearchPoints(points - MyZ.instance.getResearchConfig().getInt("item." + key + ".cost"));
								if (MyZ.instance.getSQLManager().isConnected())
									MyZ.instance.getSQLManager().set(e.getWhoClicked().getUniqueId(), "research",
											points - MyZ.instance.getResearchConfig().getInt("item." + key + ".cost"), true);

								e.getWhoClicked().closeInventory();
								Utils.showResearchDialog((Player) e.getWhoClicked(), page);
								Messenger.sendMessage(
										(Player) e.getWhoClicked(),
										Messenger.getConfigMessage(Localizer.getLocale((Player) e.getWhoClicked()), "gui.purchased", points
												- MyZ.instance.getResearchConfig().getInt("item." + key + ".cost") + ""));
							} else
								Messenger.sendConfigMessage((Player) e.getWhoClicked(), "gui.afford");
							return;
						}
					}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onDrop(PlayerDropItemEvent e) {
		if (!Validate.inWorld(e.getPlayer().getLocation()))
			return;
		lastDropped.put(e.getPlayer().getUniqueId(), e.getItemDrop().getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onEnterHopper(InventoryPickupItemEvent e) {
		if (!Validate.inWorld(e.getItem().getLocation()))
			return;
		if (e.getInventory().getType() == InventoryType.HOPPER)
			if (lastDropped.containsValue(e.getItem().getUniqueId()))
				for (UUID entry : lastDropped.keySet())
					if (lastDropped.get(entry).equals(e.getItem().getUniqueId())) {
						lastDropped.remove(entry);
						Player player = MyZ.instance.getPlayer(entry);
						e.setCancelled(true);
						if (player != null) {
							PlayerData data = PlayerData.getDataFor(player);
							int rank = 0;
							if (data != null)
								rank = data.getRank();
							if (MyZ.instance.getSQLManager().isConnected())
								rank = MyZ.instance.getSQLManager().getInt(entry, "rank");

							if (rank < (Integer) Configuration.getConfig(Configuration.RANKED_RESEARCH))
								Messenger.sendConfigMessage(player, "research.rank");
							FileConfiguration config = MyZ.instance.getResearchConfig();
							for (String key : config.getConfigurationSection("item").getKeys(false))
								if (config.getItemStack("item." + key + ".item").equals(e.getItem().getItemStack())) {
									e.getItem().remove();
									int points = config.getInt("item." + key + ".value");
									research(player, points, ((org.bukkit.block.Hopper) e.getInventory().getHolder()).getLocation(),
											"research.success");
									return;
								}
							Messenger.sendConfigMessage(player, "research.fail");
							e.getItem().teleport(player);
							e.getItem().setPickupDelay(0);

						} else
							e.getItem().remove();
					}
	}
}
