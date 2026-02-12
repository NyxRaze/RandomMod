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
import art.ameliah.fabric.autosprintfix.core.util.BlockUtil;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * AutoAnchor
 *
 * Automatically charges and explodes respawn anchors while holding use.
 *
 * Behaviour:
 * - Only activates if the player STARTS while holding the anchor.
 * - Case 1: If anchor has 0 charge → switch to glowstone (charge).
 * - Case 2: If anchor is charged → switch back to original anchor slot
 * (explode).
 */
@AutoRegister(priority = 0)
public class AutoAnchor extends Module {

    // Randomized delay before charging anchor
    public final RangeSetting chargeDelay;

    // Randomized delay before exploding charged anchor
    public final RangeSetting explodeDelay;

    // Enables debug logging
    public final BooleanSetting debugMode;

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    // True while a delayed action is pending
    private boolean isBusy = false;

    // Slot player originally held (must be anchor)
    private int initialSlot = -1;

    public AutoAnchor() {
        super(
                "AutoAnchor",
                "Automatically charge and explode respawn anchors while holding use.",
                ModuleCategory.COMBAT);

        this.chargeDelay = addSetting(new RangeSetting(
                "Charge Delay",
                "Randomized delay before switching to glowstone",
                1, 2, 0, 5));

        this.explodeDelay = addSetting(new RangeSetting(
                "Explode Delay",
                "Randomized delay before exploding charged anchor",
                1, 2, 0, 5));

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));
    }

    /**
     * Finds glowstone in hotbar.
     */
    private int findGlowstone(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() == Items.GLOWSTONE)
                return slot;
        }
        return -1;
    }

    /**
     * Finds respawn anchor in hotbar.
     */
    private int findAnchor(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() == Items.RESPAWN_ANCHOR)
                return slot;
        }
        return -1;
    }

    /**
     * Reset state when module is toggled.
     */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        isBusy = false;
        initialSlot = -1;

        if (debugMode.getValue()) {
            logger.debug("AutoAnchor {}", event.enabled ? "enabled" : "disabled");
        }
    }

    /**
     * Main execution logic.
     * Runs every render task tick.
     */
    @Listener
    public void onGameRenderTaskQueue(GameRenderTaskQueueEvent event) {

        LocalPlayer player = mc.player;
        HitResult hit = mc.hitResult;

        if (player == null || hit == null)
            return;

        if (!mc.options.keyUse.isDown())
            return;

        if (hit.getType() != HitResult.Type.BLOCK)
            return;

        if (isBusy)
            return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();

        if (!(BlockUtil.getBlock(pos) instanceof RespawnAnchorBlock))
            return;

        Inventory inventory = player.getInventory();
        int glowstoneSlot = findGlowstone(player);
        int anchorSlot = findAnchor(player);

        if (glowstoneSlot == -1 || anchorSlot == -1)
            return;

        int currentSlot = inventory.getSelectedSlot();

        // Only begin cycle if player initially holds anchor
        if (initialSlot == -1) {
            if (currentSlot != anchorSlot)
                return;

            initialSlot = currentSlot;

            if (debugMode.getValue())
                logger.debug("Initial anchor slot confirmed: {}", initialSlot);
        }

        int charge = BlockUtil.getBlockState(pos)
                .getValue(RespawnAnchorBlock.CHARGE);

        isBusy = true;

        // ===== CASE 1: Anchor not charged → switch to glowstone =====
        if (charge == 0) {

            int delay = chargeDelay.getRandomValueInt();

            if (debugMode.getValue())
                logger.debug("Charging anchor at {} (delay {} ticks)", pos, delay);

            AsyncUtil.delayTicks(delay, () -> {
                inventory.setSelectedSlot(glowstoneSlot);
                isBusy = false;
            });

        }
        // ===== CASE 2: Anchor charged → explode and revert =====
        else {

            int delay = explodeDelay.getRandomValueInt();

            if (debugMode.getValue())
                logger.debug("Exploding anchor at {} (delay {} ticks)", pos, delay);

            AsyncUtil.delayTicks(delay, () -> {
                inventory.setSelectedSlot(initialSlot);

                if (debugMode.getValue())
                    logger.debug("Reverted to initial anchor slot {}", initialSlot);

                // Reset cycle
                initialSlot = -1;
                isBusy = false;
            });
        }
    }
}
