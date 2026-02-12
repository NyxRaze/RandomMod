package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.AutoSprintFix;

import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {
    @Inject(at = @At("HEAD"), method = "respawn")
    private void requestRespawnHead(CallbackInfo ci) {
        AutoSprintFix.respawning = true;
    }

    @Inject(at = @At("TAIL"), method = "respawn")
    private void requestRespawnTail(CallbackInfo ci) {
        AutoSprintFix.respawning = false;
    }
}
