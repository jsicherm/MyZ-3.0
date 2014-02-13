/**
 * 
 */
package myz.chests;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import myz.MyZ;
import myz.support.interfacing.Configuration;
import myz.support.interfacing.Localizer;
import myz.support.interfacing.Messenger;
import myz.utilities.NMSUtils;
import myz.utilities.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Jordan
 * 
 */
public class ChestScanner implements Listener {

	private static Map<String, MaxMin> scanners = new HashMap<String, MaxMin>();
	public static List<String> getters = new ArrayList<String>();
	public static Map<String, String> setters = new HashMap<String, String>();
	private static Map<String, LootsetCreate> lootCreators = new HashMap<String, LootsetCreate>();
	private static Map<String, String> looters = new HashMap<String, String>();

	private class LootsetCreate {
		private final String name;
		private ItemStack newest;
		private Map<ItemStack, Integer> spawnable = new HashMap<ItemStack, Integer>();

		public LootsetCreate(String name) {
			this.name = name;
		}
	}

	/**
	 * Start the chest scanning process for a player.
	 * 
	 * @param playerFor
	 *            The player to initialize for.
	 */
	public static void initialize(Player playerFor) {
		scanners.put(playerFor.getName(), new MaxMin());
		Messenger.sendConfigMessage(playerFor, "chest.set.begin");
		Messenger.sendConfigMessage(playerFor, "chest.set.coordinate1");
	}

	/**
	 * Add a player to the list of lootset creators.
	 * 
	 * @param player
	 *            The player.
	 * @param lootset
	 *            The name of the lootset.
	 */
	public static void addLooter(Player player, String lootset) {
		looters.put(player.getName(), lootset);
		Messenger.sendConfigMessage(player, "loot.set.info");
	}

	@EventHandler
	private void onChat(AsyncPlayerChatEvent e) {
		if (looters.containsKey(e.getPlayer().getName())) {
			e.getPlayer().openInventory(Bukkit.createInventory(null, 9, "Lootset Creator"));
			lootCreators.put(e.getPlayer().getName(), new LootsetCreate(looters.get(e.getPlayer().getName())));
			looters.remove(e.getPlayer().getName());
			e.setCancelled(true);
		} else if (lootCreators.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			int percent = 0;
			try {
				percent = Integer.parseInt(e.getMessage().replaceAll("%", ""));
				if (percent > 100)
					percent = 100;
			} catch (Exception exc) {
			}
			Messenger.sendMessage(e.getPlayer(), "&e" + Utils.getNameOf(lootCreators.get(e.getPlayer().getName()).newest) + ": &a"
					+ percent + "%");
			lootCreators.get(e.getPlayer().getName()).spawnable.put(lootCreators.get(e.getPlayer().getName()).newest, percent);
			e.getPlayer().openInventory(Bukkit.createInventory(null, 9, "Lootset Creator"));
		}
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().getName().equals("Lootset Creator") && e.getInventory().getSize() == 9
				&& lootCreators.containsKey(e.getPlayer().getName())) {
			LootsetCreate lootset = lootCreators.get(e.getPlayer().getName());
			for (ItemStack item : e.getInventory().getContents())
				if (item != null) {
					lootset.newest = item;
					Messenger.sendConfigMessage((Player) e.getPlayer(), "loot.set.percent");
					return;
				}

			Configuration.setLootset(lootset.name, lootset.spawnable);
			for (ItemStack item : lootset.spawnable.keySet())
				Messenger.sendMessage((Player) e.getPlayer(), "&e" + Utils.getNameOf(item) + ": &a" + lootset.spawnable.get(item) + "%");
			lootCreators.remove(e.getPlayer().getName());
		}
	}

	@EventHandler
	private void onClick(PlayerInteractEvent e) {
		if (scanners.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			MaxMin mm = scanners.get(e.getPlayer().getName());
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				if (!mm.hasSetCoord1()) {
					mm.x1 = e.getClickedBlock().getX();
					mm.z1 = e.getClickedBlock().getZ();
					Messenger.sendConfigMessage(e.getPlayer(), "chest.set.coordinate2");
				} else {
					mm.x2 = e.getClickedBlock().getX();
					mm.z2 = e.getClickedBlock().getZ();
					scanners.remove(e.getPlayer().getName());
					beginScanning(e.getPlayer(), mm);
				}
		} else if (setters.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			Location inLoc = e.getClickedBlock().getLocation();
			String location = inLoc.getWorld().getName() + "," + inLoc.getBlockX() + "," + inLoc.getBlockY() + "," + inLoc.getBlockZ();
			if (e.getClickedBlock().getType() != Material.CHEST) {
				Messenger.sendConfigMessage(e.getPlayer(), "chest.set.nonchest");
				setters.remove(e.getPlayer().getName());
				return;
			}
			Chest chestObject = (Chest) inLoc.getBlock().getState().getData();
			location += "," + chestObject.getFacing().toString();

			Configuration.setChest(location, setters.get(e.getPlayer().getName()));
			String slug = "&4N/A";
			if (setters.get(e.getPlayer().getName()) != null) {
				slug = setters.get(e.getPlayer().getName());

				try {
					Class<?> craftchest = Class.forName("org.bukkit.craftbukkit." + NMSUtils.version + ".block.CraftChest");
					Object chest = craftchest.cast(e.getClickedBlock().getState());

					Field inventoryField = craftchest.getDeclaredField("chest");
					inventoryField.setAccessible(true);
					Class<?> tileentitychest = Class.forName("net.minecraft.server." + NMSUtils.version + ".TileEntityChest");
					Method a = tileentitychest.getMethod("a", String.class);
					Object teChest = tileentitychest.cast(inventoryField.get(chest));
					a.invoke(teChest, slug);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			Messenger.sendMessage(e.getPlayer(), Messenger.getConfigMessage(Localizer.getLocale(e.getPlayer()), "chest.set.typeis", slug));
			setters.remove(e.getPlayer().getName());
		} else if (getters.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
			getters.remove(e.getPlayer().getName());
			Location inLoc = e.getClickedBlock().getLocation();
			String location = inLoc.getBlockX() + "," + inLoc.getBlockY() + "," + inLoc.getBlockZ();
			if (e.getClickedBlock().getType() != Material.CHEST) {
				Messenger.sendConfigMessage(e.getPlayer(), "chest.get.nonchest");
				return;
			}
			String slug = "&4N/A";
			if (Configuration.getChests().containsKey(location)) {
				slug = Configuration.getChests().get(location);
				if (slug == null)
					slug = "&4N/A";
			}
			Messenger.sendMessage(e.getPlayer(), Messenger.getConfigMessage(Localizer.getLocale(e.getPlayer()), "chest.get.typeis", slug));
		}
	}

	/**
	 * Start the scanning.
	 * 
	 * @param playerFor
	 *            The player to report to.
	 * @param maxmin
	 *            The MaxMin underlying class.
	 */
	private void beginScanning(Player playerFor, final MaxMin maxmin) {
		Messenger.sendConfigMessage(playerFor, "chest.set.initialize");
		ScannerRunnable sr = new ScannerRunnable(maxmin, playerFor);
		sr.task = MyZ.instance.getServer().getScheduler().runTaskTimer(MyZ.instance, sr, 0L, 2L);
	}

	public class ScannerRunnable implements Runnable {

		private final int x1, x2, z1, z2, totalIters;
		private int x, iters;
		private final Player player;
		private final World world;
		private BukkitTask task;
		private List<String> chestsFound = new ArrayList<String>();

		public ScannerRunnable(MaxMin maxmin, Player player) {
			world = player.getWorld();
			if (maxmin.x1 < maxmin.x2) {
				x1 = maxmin.x1;
				x2 = maxmin.x2;
			} else {
				x2 = maxmin.x1;
				x1 = maxmin.x2;
			}
			if (maxmin.z1 < maxmin.z2) {
				z1 = maxmin.z1;
				z2 = maxmin.z2;
			} else {
				z2 = maxmin.z1;
				z1 = maxmin.z2;
			}
			x = x1;
			totalIters = x2 - x1;
			this.player = player;
		}

		@Override
		public void run() {
			for (int z = z1; z < z2; z++) {
				int highY = world.getHighestBlockYAt(x, z);
				for (int y = highY; y > 0; y--)
					if (world.getBlockAt(x, y, z).getType() == Material.CHEST)
						chestsFound.add(x + "," + y + "," + z);
			}
			Messenger.sendMessage(player, "&eScan completed: " + iters + "/" + totalIters);
			iters++;
			Messenger.sendMessage(player, "&eChests found: " + chestsFound.size());
			x++;
			if (x > x2) {
				Messenger.sendMessage(player, "&eCompleted scan! Chest log saved in chestlog.yml");
				try {
					File save = new File(MyZ.instance.getDataFolder().getAbsolutePath() + File.separator + "chestlog.yml");
					if (!save.exists())
						save.createNewFile();
					FileConfiguration fc = YamlConfiguration.loadConfiguration(save);
					fc.set("chests", chestsFound);
					fc.save(save);
				} catch (Exception exc) {
					Messenger.sendMessage(player, "&4Unable to save log.");
				}
				task.cancel();
			}
		}
	}

	private static class MaxMin {
		private int x1 = Integer.MAX_VALUE, z1 = Integer.MAX_VALUE, x2 = Integer.MAX_VALUE, z2 = Integer.MAX_VALUE;

		public boolean hasSetCoord1() {
			return x1 != Integer.MAX_VALUE;
		}
	}
}
