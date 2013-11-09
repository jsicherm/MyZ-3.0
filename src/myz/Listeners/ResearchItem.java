/**
 * 
 */
package myz.Listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import myz.MyZ;
import myz.Support.Messenger;
import myz.Support.PlayerData;
import myz.Utilities.Utilities;

import org.bukkit.Bukkit;
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

	private Map<String, UUID> lastDropped = new HashMap<String, UUID>();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onDrop(PlayerDropItemEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getPlayer().getWorld().getName()))
			return;
		lastDropped.put(e.getPlayer().getName(), e.getItemDrop().getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onEnterHopper(InventoryPickupItemEvent e) {
		if (!MyZ.instance.getWorlds().contains(e.getItem().getWorld().getName()))
			return;
		if (e.getInventory().getType() == InventoryType.HOPPER)
			if (lastDropped.containsValue(e.getItem().getUniqueId()))
				for (String entry : lastDropped.keySet())
					if (lastDropped.get(entry).equals(e.getItem().getUniqueId())) {
						lastDropped.remove(entry);
						Player player = Bukkit.getPlayerExact(entry);
						e.setCancelled(true);
						if (player != null) {
							FileConfiguration config = MyZ.instance.getResearchConfig();
							for (String key : config.getConfigurationSection("item").getKeys(false))
								if (config.getItemStack("item." + key + ".item").equals(e.getItem().getItemStack())) {
									e.getItem().remove();
									int points = config.getInt("item." + key + ".value");
									Messenger.sendMessage(player, Messenger.getConfigMessage("research.success", points));
									PlayerData data = PlayerData.getDataFor(player);
									if (data != null)
										data.setResearchPoints(data.getResearchPoints() + points);
									if (MyZ.instance.getSQLManager().isConnected())
										MyZ.instance.getSQLManager().set(player.getName(), "research",
												MyZ.instance.getSQLManager().getInt(player.getName(), "research"), true);
									return;
								}
							Messenger.sendConfigMessage(player, "research.fail");
							e.getItem().teleport(player);
							e.getItem().setPickupDelay(0);

						} else
							e.getItem().remove();
					}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onClickResearchItem(InventoryClickEvent e) {
		if (e.getInventory().getHolder() == null
				&& e.getInventory().getTitle().contains(Messenger.getConfigMessage("science_gui", 69).split("69")[0])
				&& e.getInventory().getSize() == 9) {
			e.setCancelled(true);
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= 8) {
				int page = Integer.parseInt(e.getInventory().getTitle().substring(e.getInventory().getTitle().lastIndexOf("(") + 1)
						.replace(")", ""));
				if (e.getRawSlot() == 0) {
					Utilities.showResearchDialog((Player) e.getWhoClicked(), page - 1);
					return;
				} else if (e.getRawSlot() == 0) {
					Utilities.showResearchDialog((Player) e.getWhoClicked(), page + 1);
					return;
				}
				ItemStack item = e.getInventory().getItem(e.getRawSlot());
				if (item != null)
					for (String key : MyZ.instance.getResearchConfig().getConfigurationSection("item").getKeys(false)) {
						ItemStack configured = null;
						if ((configured = MyZ.instance.getResearchConfig().getItemStack("item." + key + ".item")) != null
								&& isSimilar(configured, item)) {
							int points = 0;
							PlayerData data = PlayerData.getDataFor(e.getWhoClicked().getName());
							if (data != null)
								points = data.getResearchPoints();
							if (MyZ.instance.getSQLManager().isConnected())
								points = MyZ.instance.getSQLManager().getInt(e.getWhoClicked().getName(), "research");

							if (points > MyZ.instance.getResearchConfig().getInt("item." + key + ".cost")) {
								if (e.getWhoClicked().getInventory().firstEmpty() >= 0)
									e.getWhoClicked().getInventory().addItem(configured.clone());
								else
									e.getWhoClicked().getWorld().dropItem(e.getWhoClicked().getLocation(), configured.clone());

								if (data != null)
									data.setResearchPoints(points - MyZ.instance.getResearchConfig().getInt("item." + key + ".cost"));
								if (MyZ.instance.getSQLManager().isConnected())
									MyZ.instance.getSQLManager().set(e.getWhoClicked().getName(), "research",
											points - MyZ.instance.getResearchConfig().getInt("item." + key + ".cost"), true);

								e.getWhoClicked().closeInventory();
								Utilities.showResearchDialog((Player) e.getWhoClicked(), page);
								Messenger.sendMessage(
										(Player) e.getWhoClicked(),
										Messenger.getConfigMessage("gui.purchased",
												points - MyZ.instance.getResearchConfig().getInt("item." + key + ".cost")));
							} else
								Messenger.sendConfigMessage((Player) e.getWhoClicked(), "gui.afford");
							return;
						}
					}
			}
		}
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
			else {
				if (one != null)
					return one.getLore() != null && one.getLore().contains(Messenger.getConfigMessage("research_gui", 69).split("69")[0]);
				return two.getLore() != null && two.getLore().contains(Messenger.getConfigMessage("research_gui", 69).split("69")[0]);
			}
		} else
			return false;
	}
}
