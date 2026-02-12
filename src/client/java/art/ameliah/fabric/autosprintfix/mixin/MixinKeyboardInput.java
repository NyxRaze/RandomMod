package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.events.MovementInputEvent;
import art.ameliah.fabric.autosprintfix.core.misc.DirectionalInput;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;

import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

/**
 * Mixin for {@link KeyboardInput}.
 * 
 * Intercepts the creation of {@link Input} during the tick method
 * to dispatch a {@link MovementInputEvent} and allow modules to
 * modify player movement input before it is applied.
 */
@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    /**
     * Modifies the {@link Input} instance created inside the tick method.
     * 
     * Fires a {@link MovementInputEvent}, allowing modules to alter
     * directional and action-based movement flags before returning
     * a new modified {@link Input} instance.
     *
     * @param original The originally constructed Input instance
     * @return A possibly modified Input instance
     */
    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
    private Input modifyInput(Input original) {
        MovementInputEvent event = new MovementInputEvent(
                new DirectionalInput(original),
                original.backward(),
                original.forward(),
                original.shift(),
                original.jump());

        EventBus.getInstance().post(event);

        DirectionalInput directionalInput = event.directionalInput;

        return new Input(
                event.forwards,
                event.backwards,
                directionalInput.left,
                directionalInput.right,
                event.jump,
                event.shift,
                original.sprint());
    }
}
