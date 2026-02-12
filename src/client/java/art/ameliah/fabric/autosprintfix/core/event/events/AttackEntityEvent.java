package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;
import net.minecraft.world.entity.Entity;

/**
 * Event fired when the player attacks an entity.
 * Can be used by modules to modify or react to attacks.
 */
public class AttackEntityEvent extends Event {

    // The entity being attacked
    public final Entity entity;

    /**
     * Creates a new AttackEntityEvent.
     *
     * @param entity The entity being attacked
     */
    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }
}
