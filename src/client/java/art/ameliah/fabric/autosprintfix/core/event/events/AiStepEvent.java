package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;
import net.minecraft.world.entity.LivingEntity;

/**
 * Event fired during entity AI updates.
 * Can be used to modify entity behavior such as jump delay.
 */
public class AiStepEvent extends Event {

    // The entity whose AI is being updated
    public final LivingEntity entity;

    // Jump delay value that can be modified
    public int noJumpDelay;

    /**
     * Creates a new AI step event for the specified entity.
     *
     * @param entity The entity being updated
     */
    public AiStepEvent(LivingEntity entity) {
        this.entity = entity;
    }
}
