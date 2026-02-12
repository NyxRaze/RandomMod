package art.ameliah.fabric.autosprintfix.core.misc;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

/**
 * Represents directional movement input using boolean flags.
 *
 * Provides utility constructors for converting from Minecraft input
 * classes or movement vectors into a simplified directional representation.
 */
public final class DirectionalInput {

    // Forward movement input
    public final boolean forwards;

    // Backward movement input
    public final boolean backwards;

    // Left movement input
    public final boolean left;

    // Right movement input
    public final boolean right;

    public static final DirectionalInput NONE = new DirectionalInput(false, false, false, false);
    public static final DirectionalInput FORWARDS = new DirectionalInput(true, false, false, false);
    public static final DirectionalInput BACKWARDS = new DirectionalInput(false, true, false, false);
    public static final DirectionalInput LEFT = new DirectionalInput(false, false, true, false);
    public static final DirectionalInput RIGHT = new DirectionalInput(false, false, false, true);
    public static final DirectionalInput FORWARDS_LEFT = new DirectionalInput(true, false, true, false);
    public static final DirectionalInput FORWARDS_RIGHT = new DirectionalInput(true, false, false, true);
    public static final DirectionalInput BACKWARDS_LEFT = new DirectionalInput(false, true, true, false);
    public static final DirectionalInput BACKWARDS_RIGHT = new DirectionalInput(false, true, false, true);

    /**
     * Creates a new directional input from explicit boolean flags.
     *
     * @param forwards  Whether forward input is active
     * @param backwards Whether backward input is active
     * @param left      Whether left input is active
     * @param right     Whether right input is active
     */
    public DirectionalInput(boolean forwards, boolean backwards, boolean left, boolean right) {
        this.forwards = forwards;
        this.backwards = backwards;
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a directional input from a {@link ClientInput} instance.
     *
     * @param input The client input instance
     */
    public DirectionalInput(ClientInput input) {
        this(Input.EMPTY);
    }

    /**
     * Creates a directional input from a Minecraft {@link Input} instance.
     *
     * @param input The input instance to extract movement flags from
     */
    public DirectionalInput(Input input) {
        this(
                input.forward(),
                input.backward(),
                input.left(),
                input.right());
    }

    /**
     * Creates a directional input from movement vector values.
     *
     * Positive forward values indicate forward movement,
     * negative indicate backward. The same logic applies
     * for sideways movement.
     *
     * @param movementForward  Forward/backward movement value
     * @param movementSideways Sideways movement value
     */
    public DirectionalInput(float movementForward, float movementSideways) {
        this(
                movementForward > 0.0f,
                movementForward < 0.0f,
                movementSideways > 0.0f,
                movementSideways < 0.0f);
    }

    /**
     * Returns a new {@link DirectionalInput} with all directions inverted.
     *
     * @return The inverted directional input
     */
    public DirectionalInput invert() {
        return new DirectionalInput(
                backwards,
                forwards,
                right,
                left);
    }
}
