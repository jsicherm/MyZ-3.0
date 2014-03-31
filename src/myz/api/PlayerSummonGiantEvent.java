/**
 * 
 */
package myz.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Jordan
 * 
 *         This event is called when a player places a zombie head, summoning
 *         the giant boss zombie.
 */
public class PlayerSummonGiantEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player;
	private final Location location;

	public PlayerSummonGiantEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.Event#getHandlers()
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * The location the Giant was spawned at.
	 * 
	 * @return The location/
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * The player that summoned the Giant.
	 * 
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.Cancellable#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.Cancellable#setCancelled(boolean)
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;

	}
}
