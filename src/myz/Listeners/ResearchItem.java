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

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

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
			if (lastDropped.containsValue(e.getItem().getUniqueId())) {
				for (String entry : lastDropped.keySet()) {
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
									if (data != null) {
										data.setResearchPoints(data.getResearchPoints() + points);
									}
									if (MyZ.instance.getSQLManager().isConnected())
										MyZ.instance.getSQLManager().set(player.getName(), "research",
												MyZ.instance.getSQLManager().getInt(player.getName(), "research"), true);
									return;
								}
							Messenger.sendConfigMessage(player, "research.fail");
							e.getItem().teleport(player);
							e.getItem().setPickupDelay(0);

						} else {
							e.getItem().remove();
						}
					}
				}
			}
	}
}
