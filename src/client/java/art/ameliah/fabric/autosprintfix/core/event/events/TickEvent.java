package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;
import net.minecraft.client.Minecraft;

/**
 * Event fired every client tick.
 * Can be used for modules that need to update every tick.
 */
public class TickEvent extends Event {

    // The Minecraft client instance
    private final Minecraft client;

    /**
     * Creates a new tick event.
     * 
     * @param client The Minecraft client instance
     */
    public TickEvent(Minecraft client) {
        this.client = client;
    }

    /**
     * Gets the Minecraft client instance.
     * 
     * @return The client instance
     */
    public Minecraft getClient() {
        return client;
    }
}