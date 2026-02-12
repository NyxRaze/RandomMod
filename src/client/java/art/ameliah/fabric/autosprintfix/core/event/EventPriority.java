package art.ameliah.fabric.autosprintfix.core.event;

/**
 * Defines the priority levels for event listeners.
 * Higher priority listeners are called first.
 */
public enum EventPriority {

    // Highest priority, called first
    HIGHEST(0),

    // High priority
    HIGH(1),

    // Normal/default priority
    NORMAL(2),

    // Low priority
    LOW(3),

    // Lowest priority, called last
    LOWEST(4);

    // Numeric value for sorting
    private final int value;

    EventPriority(int value) {
        this.value = value;
    }

    /**
     * Gets the numeric priority value.
     * 
     * @return The priority value (lower = higher priority)
     */
    public int getValue() {
        return value;
    }
}