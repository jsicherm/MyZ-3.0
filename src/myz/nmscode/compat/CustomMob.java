/**
 * 
 */
package myz.nmscode.compat;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jordan
 *
 */
public interface CustomMob {

	public LivingEntity getEntity();
	
	public UUID getUID();

	public String getName();

	public Object getWorld();

	public void setInventory(List<ItemStack> inventory);
}
