package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.events.AttackEntityEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for {@link MultiPlayerGameMode}.
 *
 * Injects into the attack method to dispatch an
 * {@link AttackEntityEvent} whenever the player attacks an entity.
 */
@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {

    /**
     * Injects at the start of the attack method.
     *
     * Dispatches an {@link AttackEntityEvent} to allow
     * modules to react to entity attacks.
     *
     * @param player The attacking player
     * @param target The entity being attacked
     * @param ci     Callback information provided by Mixin
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void hookAttack(Player player, Entity target, CallbackInfo ci) {
        AttackEntityEvent event = new AttackEntityEvent(target);
        EventBus.getInstance().post(event);

        // Cancel original input if event is cancelled
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
