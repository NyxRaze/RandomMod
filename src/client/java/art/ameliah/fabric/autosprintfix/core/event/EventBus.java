package art.ameliah.fabric.autosprintfix.core.event;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central event bus for the mod's event system.
 * Handles registration and dispatching of events to listeners.
 * Thread-safe implementation using concurrent collections.
 */
public class EventBus {

    // Singleton instance
    private static EventBus instance;

    // Map of event types to their registered listeners
    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners;

    // Logger reference
    private final ModLogger logger;

    /**
     * Internal class to hold listener registration data.
     */
    private static class RegisteredListener implements Comparable<RegisteredListener> {
        final Object instance; // The object containing the listener method
        final Method method; // The listener method
        final EventPriority priority; // Priority of this listener
        final boolean receiveCancelled; // Whether to receive cancelled events

        RegisteredListener(Object instance, Method method, EventPriority priority, boolean receiveCancelled) {
            this.instance = instance;
            this.method = method;
            this.priority = priority;
            this.receiveCancelled = receiveCancelled;
        }

        @Override
        public int compareTo(RegisteredListener other) {
            return Integer.compare(this.priority.getValue(), other.priority.getValue());
        }
    }

    /**
     * Private constructor for singleton pattern.
     */
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.logger = ModLogger.getInstance();
    }

    /**
     * Gets the singleton instance of the event bus.
     * 
     * @return The EventBus instance
     */
    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Registers all listener methods in the given object.
     * Methods must be annotated with @Listener and have exactly one Event
     * parameter.
     * 
     * @param listenerObject The object containing listener methods
     */
    public void register(Object listenerObject) {
        if (listenerObject == null) {
            logger.warn("Attempted to register null listener object");
            return;
        }

        Class<?> clazz = listenerObject.getClass();
        int registeredCount = 0;

        // Scan all methods for @Listener annotation
        for (Method method : clazz.getDeclaredMethods()) {
            // First check if the method has the @Listener annotation
            // If not, skip it entirely without logging any warnings
            if (!method.isAnnotationPresent(Listener.class)) {
                continue;
            }

            // Get the annotation
            @SuppressWarnings("null")
            Listener annotation = method.getAnnotation(Listener.class);

            // Validate method parameters
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                logger.warn("Listener method " + method.getName() + " must have exactly one parameter");
                continue;
            }

            // Check if parameter is an Event subclass
            if (!Event.class.isAssignableFrom(params[0])) {
                logger.warn("Listener method " + method.getName() + " parameter must be an Event type");
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) params[0];

            // Make method accessible if private
            method.setAccessible(true);

            // Create and register the listener
            RegisteredListener registeredListener = new RegisteredListener(
                    listenerObject,
                    method,
                    annotation.priority(),
                    annotation.receiveCancelled());

            // Add to listeners map
            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(registeredListener);

            // Sort listeners by priority
            List<RegisteredListener> eventListeners = listeners.get(eventType);
            Collections.sort(eventListeners);

            logger.debug("Registered listener: " + clazz.getSimpleName() + "." + method.getName() +
                    " for event: " + eventType.getSimpleName());

            registeredCount++;
        }

        // Only log if we actually registered something
        if (registeredCount > 0) {
            logger.debug("Registered " + registeredCount + " listener(s) from " + clazz.getSimpleName());
        }
    }

    /**
     * Unregisters all listener methods from the given object.
     * 
     * @param listenerObject The object to unregister
     */
    public void unregister(Object listenerObject) {
        if (listenerObject == null) {
            return;
        }

        // Remove all listeners belonging to this object
        for (List<RegisteredListener> listenerList : listeners.values()) {
            listenerList.removeIf(listener -> listener.instance == listenerObject);
        }

        logger.debug("Unregistered listener object: " + listenerObject.getClass().getSimpleName());
    }

    /**
     * Posts an event to all registered listeners.
     * Listeners are called in priority order.
     * 
     * @param event The event to post
     * @param <T>   The event type
     * @return The posted event (may be modified by listeners)
     */
    @SuppressWarnings("null")
    public <T extends Event> T post(T event) {
        if (event == null) {
            logger.warn("Attempted to post null event");
            return null;
        }

        List<RegisteredListener> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null || eventListeners.isEmpty()) {
            return event;
        }

        // Call each listener in priority order
        for (RegisteredListener listener : eventListeners) {
            // Skip cancelled events unless listener wants them
            if (event.isCancelled() && !listener.receiveCancelled) {
                continue;
            }

            try {
                listener.method.invoke(listener.instance, event);
            } catch (Exception e) {
                logger.error("Error invoking listener " + listener.method.getName() +
                        " in " + listener.instance.getClass().getSimpleName(), e);
            }
        }

        return event;
    }
}