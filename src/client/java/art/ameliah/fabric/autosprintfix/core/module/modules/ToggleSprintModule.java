package art.ameliah.fabric.autosprintfix.core.module.modules;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.TickEvent;
import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.ModeSetting;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

/**
 * Toggle Sprint module - allows toggling sprint on/off.
 * When enabled, the player will always sprint when moving.
 */
@AutoRegister(priority = 0)
public class ToggleSprintModule extends Module {

    // Settings
    private final ModeSetting mode;
    private final BooleanSetting sprintInWater;
    private final BooleanSetting showHudIndicator;

    /**
     * Creates the Toggle Sprint module.
     * Default keybind is G.
     */
    public ToggleSprintModule() {
        super(
                "ToggleSprint",
                "Toggle sprint on/off permanently",
                ModuleCategory.MOVEMENT,
                GLFW.GLFW_KEY_G);

        // Initialize settings
        this.mode = addSetting(new ModeSetting(
                "Mode",
                "Sprint behavior mode",
                "Always",
                "Always", "Hold Forward", "Toggle Key"));

        this.sprintInWater = addSetting(new BooleanSetting(
                "Sprint In Water",
                "Allow sprinting while in water",
                true));

        this.showHudIndicator = addSetting(new BooleanSetting(
                "HUD Indicator",
                "Show sprint status on screen",
                false));
    }

    /**
     * Called every tick to maintain sprint state.
     * 
     * @param event The tick event
     */
    @Listener
    public void onTick(TickEvent event) {
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        // Don't sprint in certain conditions
        if (player.isPassenger() || player.isSleeping()) {
            return;
        }

        // Check water condition
        if (player.isInWater() && !sprintInWater.getValue()) {
            return;
        }

        // Check if player has enough food to sprint
        if (player.getFoodData().getFoodLevel() <= 6) {
            return;
        }

        // Apply sprint based on mode
        String currentMode = mode.getValue();

        switch (currentMode) {
            case "Always":
                // Always sprint when module is enabled
                player.setSprinting(true);
                break;

            case "Hold Forward":
                // Sprint only when holding forward (using zza which is forward movement)
                if (player.zza > 0) {
                    player.setSprinting(true);
                }
                break;

            case "Toggle Key":
                // Sprint state maintained (toggled via keybind)
                if (player.isSprinting()) {
                    player.setSprinting(true);
                }
                break;
        }
    }

    // Getters for settings

    public ModeSetting getModeSetting() {
        return mode;
    }

    public BooleanSetting getSprintInWaterSetting() {
        return sprintInWater;
    }

    public BooleanSetting getShowHudIndicatorSetting() {
        return showHudIndicator;
    }
}