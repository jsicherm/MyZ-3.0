/**
 * 
 */
package myz.API;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Jordan
 * 
 *         This event is fired when a player heals another player on the final
 *         click. Cancelling this event will prevent the final click from
 *         finalizing the healing but the other buffs will remain until either:
 *         a) timeout or b) the event is able to be called successfully
 */
public class PlayerHealOtherEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player, other;
	private final boolean withOintment, withAntiseptic;

	public PlayerHealOtherEvent(Player player, Player other, boolean withOintment, boolean withAntiseptic) {
		this.player = player;
		this.other = other;
		this.withOintment = withOintment;
		this.withAntiseptic = withAntiseptic;
	}

	/**
	 * The healer player.
	 * 
	 * @return The player.
	 */
	public Player getHealer() {
		return player;
	}

	/**
	 * The player that was healed.
	 * 
	 * @return The player.
	 */
	public Player getHealed() {
		return other;
	}

	/**
	 * Whether or not ointment was used.
	 * 
	 * @return True if ointment was used, false otherwise.
	 */
	public boolean withOintment() {
		return withOintment;
	}

	/**
	 * Whether or not antiseptic was used.
	 * 
	 * @return True if antiseptic was used, false otherwise.
	 */
	public boolean withAntiseptic() {
		return withAntiseptic;
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

	/* (non-Javadoc)
	 * @see org.bukkit.event.Event#getHandlers()
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
