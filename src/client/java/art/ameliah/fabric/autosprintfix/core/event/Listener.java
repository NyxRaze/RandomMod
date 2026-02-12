package art.ameliah.fabric.autosprintfix.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as event listeners.
 * Methods with this annotation will be automatically registered
 * when the containing object is registered with the EventBus.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listener {

    /**
     * The priority of this listener.
     * Higher priority listeners are called first.
     * 
     * @return The event priority
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Whether this listener should receive cancelled events.
     * 
     * @return true if cancelled events should be received
     */
    boolean receiveCancelled() default false;
}