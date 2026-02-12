package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired every client tick.
 * Can be used for modules that need to update every tick.
 */
public class TickEvent extends Event {

    /**
     * Creates a new tick event.
     */
    public TickEvent() {
    }
}
