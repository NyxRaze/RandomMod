package art.ameliah.fabric.autosprintfix.core.module.modules.misc;

import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.ModeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.NumberSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.GameRenderTaskQueueEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.UseCooldownEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * FastPlace module
 *
 * Allows faster block placement and projectile usage with configurable
 * cooldowns.
 * Supports mode selection for blocks, projectiles, or both.
 */
public class FastPlace extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    /* ================= SETTINGS ================= */

    private final RangeSetting cooldown; // Combines base + jitter
    private final NumberSetting startDelay; // Delay before activation
    private final ModeSetting targetType; // Blocks / Projectiles / Both
    private final BooleanSetting debugMode;

    private long holdStartTime = 0;

    public FastPlace() {
        super("FastPlace",
                "Allows you to place blocks or use projectiles faster.",
                ModuleCategory.MISC);

        this.cooldown = addSetting(new RangeSetting(
                "Cooldown (ticks)",
                "Cooldown delay for placing items",
                1, 3, 1, 5));

        this.startDelay = addSetting(new NumberSetting(
                "Start Delay (ms)",
                "Delay before fast place starts",
                0, 0, 1000));

        this.targetType = addSetting(new ModeSetting(
                "Target Type",
                "Which items FastPlace applies to",
                "Both", "Both", "Blocks Only", "Projectiles Only"));

        this.debugMode = addSetting(new BooleanSetting("Debug Mode", "Enable debug logging", false));
    }

    /* ================= HELPERS ================= */

    /** Returns true if either item is a block. */
    private boolean isBlock(Item main, Item off) {
        return main instanceof BlockItem || off instanceof BlockItem;
    }

    /** Returns true if either item is a projectile. */
    private boolean isProjectile(Item main, Item off) {
        return main instanceof ProjectileItem || off instanceof ProjectileItem;
    }

    /** Checks if FastPlace should apply for current items based on mode. */
    private boolean shouldApply(Item main, Item off) {
        String mode = targetType.getModes().get(targetType.getIndex());
        return switch (mode) {
            case "Blocks Only" -> isBlock(main, off); // Blocks
            case "Projectiles Only" -> isProjectile(main, off); // Projectiles
            case "Both" -> isBlock(main, off) || isProjectile(main, off); // Both
            default -> false;
        };
    }

    /* ================= TOGGLE ================= */

    /** Called when the module is toggled. Logs debug info. */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue())
                logger.debug("FastPlace enabled");
        } else {
            if (debugMode.getValue())
                logger.debug("FastPlace disabled");
        }
    }

    /* ================= GAME LOOP ================= */

    /** Tracks when the use key is being held. */
    @Listener
    public void onGameRenderTaskQueue(GameRenderTaskQueueEvent event) {
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        long currentTime = System.currentTimeMillis();

        if (!mc.options.keyUse.isDown())
            holdStartTime = 0;
        if (holdStartTime == 0)
            holdStartTime = currentTime;
    }

    /** Overrides item cooldowns according to settings. */
    @Listener
    public void onUseCooldown(UseCooldownEvent event) {
        LocalPlayer player = mc.player;
        if (player == null || holdStartTime == 0)
            return;

        Inventory inventory = player.getInventory();
        ItemStack mainItem = player.getMainHandItem();
        ItemStack offItem = player.getOffhandItem();
        Item currentItem = inventory.getSelectedItem().getItem();

        // Always reduce pearl/wind charge cooldown
        if (currentItem == Items.ENDER_PEARL || currentItem == Items.WIND_CHARGE) {
            event.cooldown = 1;
            if (debugMode.getValue())
                logger.debug("FastPlace pearl/wind-charge override: 1 tick");
            return;
        }

        // Apply based on mode setting
        if (!shouldApply(mainItem.getItem(), offItem.getItem()))
            return;

        if (System.currentTimeMillis() - holdStartTime < startDelay.getIntValue())
            return;

        // Use RangeSetting's random delay to combine base + jitter
        int finalCooldown = cooldown.getRandomValueInt();
        event.cooldown = finalCooldown;

        if (debugMode.getValue()) {
            logger.debug("FastPlace cooldown applied: {}", finalCooldown);
        }
    }
}
