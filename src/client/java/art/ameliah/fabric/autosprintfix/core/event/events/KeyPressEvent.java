package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;

/**
 * Event fired when a key is pressed.
 * Can be cancelled to prevent the key press from being processed.
 */
public class KeyPressEvent extends Event {

    // The GLFW key code
    private final int keyCode;

    // The scan code
    private final int scanCode;

    // Key action (press, release, repeat)
    private final int action;

    // Modifier keys (shift, ctrl, alt)
    private final int modifiers;

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

    /**
     * Gets the GLFW key code.
     * 
     * @return The key code
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Gets the scan code.
     * 
     * @return The scan code
     */
    public int getScanCode() {
        return scanCode;
    }

    /**
     * Gets the key action.
     * 
     * @return The action (GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT)
     */
    public int getAction() {
        return action;
    }

    /**
     * Gets the modifier keys.
     * 
     * @return The modifiers bitmask
     */
    public int getModifiers() {
        return modifiers;
    }
}