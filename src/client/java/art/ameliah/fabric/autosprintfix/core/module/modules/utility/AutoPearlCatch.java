package art.ameliah.fabric.autosprintfix.core.module.modules.utility;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.GameRenderTaskQueueEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.UseCooldownEvent;
import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.NumberSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * AutoPearlCatch module
 *
 * Automatically switches to Wind Charge after throwing an Ender Pearl
 * when the player is looking upwards. Adds a randomized swap delay
 * for more natural timing.
 */
@AutoRegister(priority = 0)
public class AutoPearlCatch extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    // Randomized swap delay after pearl use (ticks)
    public final RangeSetting swapDelay;

    // Minimum upward pitch to trigger catch
    public final NumberSetting minUpPitch;

    // Enable debug logging
    public final BooleanSetting debugMode;

    // Tracks if swap is pending
    private boolean pendingCatch = false;

    // Stores cooldown from UseCooldownEvent
    private int cooldown = -1;

    public AutoPearlCatch() {
        super(
                "AutoPearlCatch",
                "Automatically switches to Wind Charge after throwing an Ender Pearl when looking upward.",
                ModuleCategory.UTILITY);

        this.swapDelay = addSetting(new RangeSetting(
                "Swap Delay",
                "Randomized delay (ticks) before swapping to Wind Charge after throwing pearl",
                1, 2, 0, 5));

        this.minUpPitch = addSetting(new NumberSetting(
                "Min Upward Pitch",
                "Minimum upward pitch angle to trigger pearl catch",
                45, 0, 90, 1));

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));
    }

    /**
     * Gets the hotbar slot containing a Wind Charge.
     *
     * @param player The local player
     * @return Slot index or -1 if not found
     */
    private int findWindCharge(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.WIND_CHARGE) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the player is looking upward past the configured pitch.
     *
     * @param player The local player
     * @return true if looking up
     */
    private boolean isLookingUp(LocalPlayer player) {
        return player.getXRot() <= -minUpPitch.getIntValue();
    }

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */

    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("AutoPearlCatch enabled");
            }
        } else {
            if (debugMode.getValue()) {
                logger.debug("AutoPearlCatch disabled");
            }
        }
    }

    /**
     * Called when a UseCooldownEvent is fired.
     * Tracks the pearl cooldown.
     *
     * @param event The cooldown event
     */
    @Listener
    public void onUseCooldown(UseCooldownEvent event) {
        cooldown = event.cooldown;
    }

    /**
     * Main logic for detecting pearl use and swapping to Wind Charge.
     * Fires on each game render task queue.
     *
     * @param event The render task queue event
     */
    @Listener
    public void onGameRenderTaskQueue(GameRenderTaskQueueEvent event) {
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        Inventory inventory = player.getInventory();
        int currentSlot = inventory.getSelectedSlot();
        ItemStack currentStack = inventory.getItem(currentSlot);

        // Detect pearl use while looking up
        if (currentStack.getItem() == Items.ENDER_PEARL
                && player.getCooldowns().isOnCooldown(currentStack)
                && mc.options.keyUse.isDown()
                && cooldown != -1
                && !pendingCatch
                && isLookingUp(player)) {

            int windSlot = findWindCharge(player);
            if (windSlot != -1) {
                pendingCatch = true;

                if (debugMode.getValue()) {
                    logger.debug("AutoPearlCatch: Swapping to Wind Charge in {} ticks", swapDelay.getRandomValueInt());
                }

                // Delay swap using RangeSetting's getRandomValueInt()
                AsyncUtil.delayTicks(swapDelay.getRandomValueInt(), () -> {
                    inventory.setSelectedSlot(windSlot);
                    pendingCatch = false;

                    if (debugMode.getValue()) {
                        logger.debug("AutoPearlCatch: Swapped to Wind Charge (slot {})", windSlot);
                    }
                });
            }
        }
    }
}
