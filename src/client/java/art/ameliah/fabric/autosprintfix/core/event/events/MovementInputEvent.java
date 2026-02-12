package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.misc.DirectionalInput;
import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired when player movement input is processed.
 * 
 * Can be used to inspect or modify movement directions and actions
 * such as jumping or sneaking.
 */
public class MovementInputEvent extends Event {

    // Combined directional input
    public DirectionalInput directionalInput;

    // Forward movement input
    public boolean forwards;

    // Backward movement input
    public boolean backwards;

    // Sneak (shift) input
    public boolean shift;

    // Jump input
    public boolean jump;

    /**
     * Creates a new MovementInputEvent.
     * 
     * @param directionalInput Combined directional input
     * @param backwards        Backward movement flag
     * @param forwards         Forward movement flag
     * @param shift            Sneak (shift) flag
     * @param jump             Jump flag
     */
    public MovementInputEvent(DirectionalInput directionalInput, boolean backwards, boolean forwards, boolean shift,
            boolean jump) {
        this.directionalInput = directionalInput;

        this.backwards = backwards;
        this.forwards = forwards;

        this.shift = shift;
        this.jump = jump;
    }
}
