/**
 * 
 */
package myz.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Jordan
 * 
 *         This event is fired when a player heals above a certain threshold and
 *         becomes a 'healer'. Cancelling it will prevent the player from
 *         attaining healer status.
 */
public class PlayerBecomeHealerEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player;

	public PlayerBecomeHealerEvent(Player player) {
		this.player = player;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.Event#getHandlers()
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * The player that became a healer.
	 * 
	 * @return the player.
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
