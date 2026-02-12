package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.events.AiStepEvent;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for {@link LivingEntity}.
 *
 * Injects into the aiStep method to dispatch an {@link AiStepEvent},
 * allowing modules to modify entity-related behavior such as jump delay.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    /**
     * Shadowed jump delay field from {@link LivingEntity}.
     * Allows modification after event dispatch.
     */
    @Shadow
    public int noJumpDelay;

    /**
     * Injects at the start of the aiStep method.
     *
     * Fires an {@link AiStepEvent} and applies any modified
     * values back to the entity instance.
     *
     * @param ci Callback information provided by Mixin
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void hookAiStep(CallbackInfo ci) {
        AiStepEvent event = new AiStepEvent((LivingEntity) (Object) this);
        EventBus.getInstance().post(event);

        this.noJumpDelay = event.noJumpDelay;
    }
}
