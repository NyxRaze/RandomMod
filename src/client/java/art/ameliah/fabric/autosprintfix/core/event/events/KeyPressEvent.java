package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired when a key is pressed.
 * Can be cancelled to prevent the key press from being processed.
 */
public class KeyPressEvent extends Event {

    // The GLFW key code
    public final int keyCode;

    // The scan code
    public final int scanCode;

    // Key action (press, release, repeat)
    public final int action;

    // Modifier keys (shift, ctrl, alt)
    public final int modifiers;

    /**
     * Creates a new key press event.
     * 
     * @param keyCode   The GLFW key code
     * @param scanCode  The scan code
     * @param action    The key action
     * @param modifiers The modifier keys
     */
    public KeyPressEvent(int keyCode, int scanCode, int action, int modifiers) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.action = action;
        this.modifiers = modifiers;
    }
}
