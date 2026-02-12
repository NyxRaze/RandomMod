package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.events.GameRenderTaskQueueEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.TickEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.UseCooldownEvent;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for {@link Minecraft}.
 *
 * Injects into core game loop methods to dispatch tick,
 * render queue, and item use cooldown events to modules.
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    /**
     * Shadowed item use cooldown field.
     * Allows modification of right-click delay after event dispatch.
     */
    @Shadow
    private int rightClickDelay;

    /**
     * Injects at the start of the tick method.
     *
     * Updates asynchronous utilities and dispatches
     * a {@link TickEvent} to registered modules.
     *
     * @param callbackInfo Callback information provided by Mixin
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void hookTickEvent(CallbackInfo callbackInfo) {
        AsyncUtil.tick();

        EventBus.getInstance().post(new TickEvent());
    }

    /**
     * Injects at the start of the runTick method.
     *
     * Dispatches a {@link GameRenderTaskQueueEvent} to allow
     * modules to react to render task processing.
     *
     * @param callbackInfo Callback information provided by Mixin
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    private void hookGameRenderTaskQueueEvent(CallbackInfo callbackInfo) {
        EventBus.getInstance().post(new GameRenderTaskQueueEvent());
    }

    /**
     * Injects after the rightClickDelay field is updated in startUseItem.
     *
     * Fires a {@link UseCooldownEvent}, allowing modules to modify
     * the item use cooldown before it is applied.
     *
     * @param callbackInfo Callback information provided by Mixin
     */
    @Inject(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = At.Shift.AFTER))
    private void hookItemUseCooldown(CallbackInfo callbackInfo) {
        UseCooldownEvent useCooldownEvent = new UseCooldownEvent(rightClickDelay);
        EventBus.getInstance().post(useCooldownEvent);

        rightClickDelay = useCooldownEvent.cooldown;
    }
}
