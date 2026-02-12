package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.events.TickEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for client tick events.
 * Fires TickEvent every client tick.
 */
@Mixin(Minecraft.class)
public class ClientTickMixin {

    /**
     * Injects at the start of the tick method to fire TickEvent.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // Fire tick event
        TickEvent event = new TickEvent((Minecraft) (Object) this);
        EventBus.getInstance().post(event);
    }
}