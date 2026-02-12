package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired when an item use cooldown is applied.
 * Can be used by modules to modify or react to the cooldown.
 */
public class UseCooldownEvent extends Event {

    // The current item use cooldown
    public int cooldown;

    /**
     * Creates a new UseCooldownEvent.
     *
     * @param cooldown The initial cooldown value
     */
    public UseCooldownEvent(int cooldown) {
        this.cooldown = cooldown;
    }
}
