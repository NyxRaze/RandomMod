package art.ameliah.fabric.autosprintfix.core.module.modules.misc;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.AiStepEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

/**
 * NoJumpDelay
 *
 * Removes the vanilla jump cooldown by resetting noJumpDelay every tick.
 * Always-on module.
 */
@AutoRegister(priority = 0)
public class NoJumpDelay extends Module {

    private final ModLogger logger = ModLogger.getInstance();
    private final Minecraft mc = Minecraft.getInstance();

    private final BooleanSetting debugMode;

    public NoJumpDelay() {
        super(
                "NoJumpDelay",
                "Removes the vanilla jump cooldown.",
                ModuleCategory.MISC);

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));
    }

    /**
     * Called when the module is toggled.
     */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("NoJumpDelay enabled");
            }
        } else {
            if (debugMode.getValue()) {
                logger.debug("NoJumpDelay disabled");
            }
        }
    }

    /**
     * Resets the player's jump cooldown each tick.
     */
    @Listener
    public void onAiStep(AiStepEvent event) {
        if (event.entity != mc.player)
            return;

        event.noJumpDelay = 0;
    }
}
