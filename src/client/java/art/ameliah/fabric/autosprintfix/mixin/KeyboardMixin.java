package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.events.KeyPressEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for keyboard input handling.
 * Hooks into KeyboardHandler to intercept all key inputs.
 * Fires KeyPressEvent for our event system.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    /**
     * Injects into the keyPress method to fire KeyPressEvent.
     * This is called for all keyboard input events.
     * 
     * @param window   The window handle
     * @param action   The key action (GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT)
     * @param keyEvent The key event containing key code, scancode, and modifiers
     * @param ci       Callback info for potential cancellation
     */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        try {
            // Extract key information from the event
            int keyCode = keyEvent.key();
            int scanCode = keyEvent.scancode();
            int modifiers = keyEvent.modifiers();

            // Create and dispatch our custom event
            KeyPressEvent event = new KeyPressEvent(keyCode, scanCode, action, modifiers);
            EventBus.getInstance().post(event);

            // Cancel the original event if our event was cancelled
            if (event.isCancelled()) {
                ci.cancel();
            }
        } catch (Exception e) {
            // Log any errors but don't crash the game
            ModLogger.getInstance().error("Error in keyboard mixin", e);
        }
    }
}