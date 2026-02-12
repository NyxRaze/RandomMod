package art.ameliah.fabric.autosprintfix.mixin;

import art.ameliah.fabric.autosprintfix.AutoSprintFix;
import net.minecraft.client.ToggleKeyMapping;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ToggleKeyMapping.class)
public class MixinStickyKeyBinding {
    @Inject(at = @At("HEAD"), method = "reset", cancellable = true)
    public void onUnToggle(CallbackInfo ci) {
        if (AutoSprintFix.respawning) {
            ci.cancel();
        }
    }
}
