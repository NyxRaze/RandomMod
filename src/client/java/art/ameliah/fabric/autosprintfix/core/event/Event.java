package art.ameliah.fabric.autosprintfix.core.event;

/**
 * Base class for all events in the mod's event system.
 * Events can be cancelled to prevent further processing.
 */
public abstract class Event {

    // Whether this event has been cancelled
    private boolean cancelled = false;

    /**
     * Checks if this event has been cancelled.
     * 
     * @return true if the event is cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of this event.
     * 
     * @param cancelled Whether to cancel the event
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the name of this event type.
     * 
     * @return The simple class name of this event
     */
    public String getEventName() {
        return this.getClass().getSimpleName();
    }
}