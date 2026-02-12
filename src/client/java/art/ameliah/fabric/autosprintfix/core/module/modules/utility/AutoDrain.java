package art.ameliah.fabric.autosprintfix.core.module.modules.utility;

import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.util.InteractionUtil;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.GameRenderTaskQueueEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * AutoDrain module
 *
 * Automatically drains distant water source blocks using an empty bucket.
 * Uses configurable reaction delay, use delay, and swap-back delay.
 * Fully handles hotbar slot swapping and optional debug logging.
 */
@AutoRegister(priority = 0)
public class AutoDrain extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    // Settings
    public final RangeSetting reactionDelayMs; // Delay (ms) before reacting to water
    public final RangeSetting useDelayTicks; // Ticks to hold use key
    public final RangeSetting swapBackDelay; // Ticks to wait before swapping back original slot
    public final BooleanSetting debugMode; // Enable debug logging

    // Runtime state
    private boolean isUsing = false; // True while using bucket
    private AsyncUtil.Token swapBackToken; // Scheduled swap-back task
    private BlockPos lastSeenWater = null; // Last water block targeted
    private long aimStartMs = 0; // When player started aiming at water

    private static final float MIN_DRAIN_DISTANCE = 2.5f; // Minimum distance to water for draining

    public AutoDrain() {
        super(
                "AutoDrain",
                "Automatically drains distant source water using an empty bucket with configurable delays.",
                ModuleCategory.UTILITY);

        // Configure settings with RangeSettings that already handle
        // min/max/randomization
        this.reactionDelayMs = addSetting(
                new RangeSetting("Reaction Delay (ms)", "Time aiming at water before acting", 30, 50, 0, 500));
        this.useDelayTicks = addSetting(
                new RangeSetting("Use Delay (ticks)", "Ticks to wait before holding use key for draining", 1, 2, 0, 5));
        this.swapBackDelay = addSetting(new RangeSetting("Swap-back Delay (ticks)",
                "Ticks to wait before returning to original slot", 1, 3, 0, 5));
        this.debugMode = addSetting(new BooleanSetting("Debug Mode", "Enable debug logging", false));
    }

    /**
     * Finds the first empty bucket in the player's hotbar.
     *
     * @param inventory The player's hotbar inventory
     * @return Hotbar slot index of empty bucket, or -1 if none found
     */
    private int findEmptyBucket(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() == Items.BUCKET)
                return slot;
        }
        return -1;
    }

    /**
     * Schedules swapping back to the original hotbar slot after using the bucket.
     *
     * @param inventory    Player's inventory
     * @param originalSlot Original hotbar slot to return to
     */
    private void scheduleSwapBack(Inventory inventory, int originalSlot) {
        if (swapBackToken != null)
            swapBackToken.cancel(); // Cancel any previous scheduled swap-back

        int delay = swapBackDelay.getRandomValueInt();
        swapBackToken = AsyncUtil.delayTicks(delay, () -> {
            inventory.setSelectedSlot(originalSlot);
            isUsing = false;
            if (debugMode.getValue())
                logger.debug("Swapped back to original slot {}", originalSlot);
        });
    }

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */

    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("AutoDrain enabled");
            }
        } else {
            if (debugMode.getValue()) {
                logger.debug("AutoDrain disabled");
            }
        }
    }

    /**
     * Main logic executed each render tick.
     * Detects water blocks, applies reaction delay, uses bucket, and swaps back.
     */
    @Listener
    public void onGameRenderTaskQueue(GameRenderTaskQueueEvent event) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;

        if (player == null || level == null || isUsing)
            return;

        // Raycast to detect targeted block
        HitResult hit = player.pick(4.5D, 0.0F, true);
        if (hit.getType() != HitResult.Type.BLOCK) {
            lastSeenWater = null;
            aimStartMs = 0;
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        // Ignore non-source water
        if (!state.getFluidState().isSource() || state.getFluidState().getType() != Fluids.WATER) {
            lastSeenWater = null;
            aimStartMs = 0;
            return;
        }

        // Ignore water blocks too close
        double distSq = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (distSq < MIN_DRAIN_DISTANCE * MIN_DRAIN_DISTANCE) {
            lastSeenWater = null;
            aimStartMs = 0;
            if (debugMode.getValue())
                logger.debug("Skipping drain (water too close)");
            return;
        }

        // Reaction delay logic
        long now = System.currentTimeMillis();
        if (lastSeenWater != null && pos.equals(lastSeenWater)) {
            if (now - aimStartMs < reactionDelayMs.getRandomValueInt())
                return;
        } else {
            lastSeenWater = pos;
            aimStartMs = now;
            return; // Start counting reaction delay
        }

        // Hotbar logic
        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedStack = inventory.getItem(selectedSlot);
        int bucketSlot = (selectedStack.getItem() == Items.BUCKET) ? selectedSlot : findEmptyBucket(inventory);
        if (bucketSlot == -1) {
            if (debugMode.getValue())
                logger.debug("No empty bucket found");
            return;
        }

        // Use bucket
        isUsing = true;
        int originalSlot = selectedSlot; // Remember original slot
        inventory.setSelectedSlot(bucketSlot);
        if (debugMode.getValue())
            logger.debug("Using bucket at {}", pos);

        AsyncUtil.delayTicks(useDelayTicks.getRandomValueInt(), () -> {
            InteractionUtil.use(true, 1); // Use bucket
            scheduleSwapBack(inventory, originalSlot); // Swap back after delay
            lastSeenWater = null;
            aimStartMs = 0;
        });
    }
}
