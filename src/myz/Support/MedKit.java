/**
 * 
 */
package myz.Support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import myz.MyZ;
import myz.Utilities.GlowEnchant;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jordan
 * 
 */
public class MedKit {

	private static int uuid = 50;
	private static List<MedKit> medkits = new ArrayList<MedKit>();
	private static Enchantment glow = new GlowEnchant(69);
	private int uid;
	private String configID, name;
	private List<String> lore = new ArrayList<String>();
	private int antiseptic, ointment;
	private ItemStack input, output;

	public MedKit(String configID, String name, int antiseptic, int ointment, ItemStack input, ItemStack output) {
		uuid++;
		uid = uuid;

		if (ointment > 10)
			ointment = 10;
		if (antiseptic > 10)
			antiseptic = 10;
		if (ointment + antiseptic > 8) {
			if (ointment > 4)
				ointment = 4;
			if (antiseptic > 4)
				antiseptic = 4;
		}

		this.configID = configID;
		this.name = name;
		this.antiseptic = antiseptic;
		this.ointment = ointment;
		this.input = input;
		this.output = output;

		if (antiseptic > 0)
			lore.add(ChatColor.GRAY
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							MyZ.instance.getConfig().getString("heal.medkit.localizable.antiseptic"))) + " "
					+ getRomanNumeralsFor(antiseptic));
		if (ointment > 0)
			lore.add(ChatColor.GRAY
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							MyZ.instance.getConfig().getString("heal.medkit.localizable.regeneration"))) + " "
					+ getRomanNumeralsFor(ointment));
		if (lore.isEmpty())
			lore.add(ChatColor.GRAY
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							MyZ.instance.getConfig().getString("heal.medkit.localizable.heal"))) + " I");

		medkits.add(this);
		save();
		createRecipe();
	}

	public static List<MedKit> getKits() {
		return medkits;
	}

	public static void clearKits() {
		medkits = null;
		glow = null;
	}

	public String getName() {
		return name;
	}

	public String getConfigID() {
		return configID;
	}

	public int getAntisepticRequired() {
		return antiseptic;
	}

	public int getOintmentRequired() {
		return ointment;
	}

	public ItemStack getInput() {
		return input;
	}

	public ItemStack getOutput() {
		return output;
	}

	public int getUID() {
		return uid;
	}

	/**
	 * Save this MedKit to the config.
	 */
	public void save() {
		FileConfiguration config = MyZ.instance.getConfig();
		config.set("heal.medkit.kit." + configID + ".name", name);
		config.set("heal.medkit.kit." + configID + ".antiseptic_required", antiseptic);
		config.set("heal.medkit.kit." + configID + ".ointment_required", ointment);
		config.set("heal.medkit.kit." + configID + ".input", input);
		config.set("heal.medkit.kit." + configID + ".output", output);
		MyZ.instance.saveConfig();
	}

	/**
	 * Get the MedKit associated with a given ItemStack. Compares durability
	 * values and stack materials.
	 * 
	 * @param stack
	 *            The ItemStack in question.
	 * @return The MedKit for the given ItemStack or null if none associated.
	 */
	public static MedKit getMedKitFor(ItemStack stack) {
		short durability = stack.getDurability();
		for (MedKit kit : medkits)
			if ((short) kit.uid == durability && stack.getType() == kit.output.getType())
				return kit;
		return null;
	}

	/**
	 * Load and create the recipe for this medkit.
	 */
	public void createRecipe() {
		ItemStack out = output.clone();
		ItemMeta meta = out.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		meta.setLore(lore);
		out.setItemMeta(meta);
		out.setDurability((short) uid);
		out.addEnchantment(glow, 1);

		ShapelessRecipe recipe = new ShapelessRecipe(out);
		if (ointment > 0)
			recipe.addIngredient(ointment, Configuration.getOintment().getData());
		if (antiseptic > 0)
			recipe.addIngredient(antiseptic, Configuration.getAntiseptic().getData());
		recipe.addIngredient(input.getData());

		MyZ.instance.getServer().addRecipe(recipe);
	}

	@Override
	public String toString() {
		String out = output.getType().toString().toLowerCase().replaceAll("_", " ");
		String in = input.getType().toString().toLowerCase().replaceAll("_", " ");
		return ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET + " requires " + ointment + " ointment, " + antiseptic
				+ " antiseptic and a" + (startsWithVowel(in) ? "n" : "") + in + " and yields a" + (startsWithVowel(out) ? "n" : "") + " "
				+ out;
	}

	/**
	 * Whether or not a specified word starts with a vowel (not including 'y').
	 * 
	 * @param word
	 *            The String in question.
	 * @return True if the word starts with an 'a', 'e', 'i', 'o', or 'u', false
	 *         otherwise.
	 */
	private boolean startsWithVowel(String word) {
		return word.startsWith("a") || word.startsWith("e") || word.startsWith("i") || word.startsWith("o") || word.startsWith("u");
	}

	/**
	 * Get the roman numerals that correspond to a number from 1 to 10.
	 * 
	 * @param number
	 *            The number.
	 * @return The roman numerals for the number.
	 */
	private String getRomanNumeralsFor(int number) {
		switch (number) {
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		case 4:
			return "IV";
		case 5:
			return "V";
		case 6:
			return "VI";
		case 7:
			return "VII";
		case 8:
			return "VIII";
		case 9:
			return "IX";
		case 10:
			return "X";
		}
		return "I";
	}

	/**
	 * Register a custom enchantment for med-kits.
	 * 
	 * @return True if the EnchantmentWrapper could register the new
	 *         enchantment.
	 */
	public static boolean registerNewEnchantment() {
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
			try {
				Enchantment.registerEnchantment(glow);
				return true;
			} catch (IllegalArgumentException e) {

			}
		} catch (Exception e) {
		}
		return false;
	}
}
