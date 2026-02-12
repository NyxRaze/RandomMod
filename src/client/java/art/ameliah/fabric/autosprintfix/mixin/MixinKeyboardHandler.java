package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.events.KeyPressEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for {@link KeyboardHandler}.
 *
 * Hooks into keyboard input to fire {@link KeyPressEvent}
 * through the mod event bus. Allows modules to intercept
 * or cancel keyboard actions.
 */
@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    /**
     * Injects at the start of the keyPress method.
     *
     * Fires a {@link KeyPressEvent} with key information.
     * Cancels the original input if the event is cancelled.
     *
     * @param window   The window handle
     * @param action   The GLFW key action
     * @param keyEvent The key event containing key data
     * @param ci       Callback info for cancellation
     */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        try {
            // Extract key information
            int keyCode = keyEvent.key();
            int scanCode = keyEvent.scancode();
            int modifiers = keyEvent.modifiers();

            // Dispatch event
            KeyPressEvent event = new KeyPressEvent(keyCode, scanCode, action, modifiers);
            EventBus.getInstance().post(event);

            // Cancel original input if event is cancelled
            if (event.isCancelled()) {
                ci.cancel();
            }
        } catch (Exception e) {
            ModLogger.getInstance().error("Error in keyboard mixin", e);
        }
    }
}
