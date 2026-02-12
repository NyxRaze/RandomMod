package art.ameliah.fabric.autosprintfix.core.module.modules.combat;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.GameRenderTaskQueueEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.util.InteractionUtil;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * AutoCrystal module
 *
 * Automatically attacks End Crystals the player is aiming at
 * while holding the use key. Uses a configurable attack delay
 * for human-like timing. Debug logging is optional.
 */
@AutoRegister(priority = 0)
public class AutoCrystal extends Module {

    // Minecraft client instance
    private final Minecraft mc = Minecraft.getInstance();

    // ModLogger singleton instance for logging
    private final ModLogger logger = ModLogger.getInstance();

    // RangeSetting for randomized attack delay in ticks
    public final RangeSetting attackDelay;

    // BooleanSetting for enabling debug logging
    public final BooleanSetting debugMode;

    // Tracks whether the module is currently performing an attack
    private boolean isAttacking = false;

    /**
     * Constructor: initializes module name, description, category, and settings.
     */
    public AutoCrystal() {
        super(
                "AutoCrystal",
                "Automatically attacks aimed end crystals while holding the use key.",
                ModuleCategory.COMBAT);

        this.attackDelay = addSetting(new RangeSetting(
                "Attack Delay",
                "Randomized delay for attacking crystals (ticks)",
                1, 3, 0, 5));

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));
    }

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */

    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("AutoCrystal enabled");
            }
        } else {
            if (debugMode.getValue()) {
                logger.debug("AutoCrystal disabled");
            }
        }
    }

    /**
     * Main logic for attacking aimed End Crystals.
     * Fires on each game render task queue event.
     *
     * @param event The game render task queue event
     */
    @Listener
    public void onGameRenderTaskQueue(GameRenderTaskQueueEvent event) {
        LocalPlayer player = mc.player;
        Entity targetedEntity = mc.crosshairPickEntity;

        // Safety checks: ensure player exists, an entity is targeted,
        // the use key is pressed, and the module is not already attacking
        if (player == null || targetedEntity == null || !mc.options.keyUse.isDown() || isAttacking)
            return;

        // Only attack End Crystals
        if (targetedEntity.getType() != EntityType.END_CRYSTAL)
            return;

        // Determine randomized delay for attack
        int delay = attackDelay.getRandomValueInt();

        // Debug log for chosen attack delay
        if (debugMode.getValue()) {
            logger.debug("AutoCrystal attack delay: {} ticks", delay);
        }

        // Mark module as currently attacking to prevent overlapping attacks
        isAttacking = true;

        // Schedule attack after delay using AsyncUtil
        AsyncUtil.delayTicks(delay, () -> {
            try {
                // Simulate the attack on the targeted crystal
                InteractionUtil.attack(true, 0);
            } catch (Exception e) {
                // Log any exceptions during attack simulation
                if (debugMode.getValue()) {
                    logger.error("AutoCrystal failed to attack: {}", e);
                }
            } finally {
                // Reset attacking state
                isAttacking = false;

                // Debug log for attack completion
                if (debugMode.getValue()) {
                    logger.debug("AutoCrystal: Simulated attack on {}", targetedEntity.getType());
                }
            }
        });
    }
}
