package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired at each game render task queue.
 * Can be used for modules that need to run logic during render tasks.
 */
public class GameRenderTaskQueueEvent extends Event {

    /**
     * Creates a new GameRenderTaskQueueEvent.
     */
    public GameRenderTaskQueueEvent() {
    }
}
