package art.ameliah.fabric.autosprintfix.core.module.modules.player;

import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.MovementInputEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Eagle module
 *
 * Automatically sneaks at edges while bridging or stair-jumping.
 * Helps prevent accidental falls during fast bridging.
 */
public class Eagle extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    /** Enable debug logging */
    private final BooleanSetting debugMode;

    /** Track if we were on ground last tick (for jump detection) */
    private boolean wasOnGround = false;

    public Eagle() {
        super("Eagle",
                "Automatically sneaks at edges for bridging and stair-jumping.",
                ModuleCategory.MISC);

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging for Eagle module",
                false));
    }

    /* ================= EDGE DETECTION ================= */

    @SuppressWarnings("deprecation")
    private boolean isAtEdge(LocalPlayer player) {
        ClientLevel level = mc.level;
        if (level == null)
            return false;

        AABB box = player.getBoundingBox();

        double expand = 0.001;
        double minX = box.minX - expand;
        double maxX = box.maxX + expand;
        double minZ = box.minZ - expand;
        double maxZ = box.maxZ + expand;
        double checkY = box.minY - 0.5;

        BlockPos[] checkPositions = {
                BlockPos.containing(minX, checkY, minZ),
                BlockPos.containing(minX, checkY, maxZ),
                BlockPos.containing(maxX, checkY, minZ),
                BlockPos.containing(maxX, checkY, maxZ),
                BlockPos.containing((minX + maxX) / 2, checkY, minZ),
                BlockPos.containing((minX + maxX) / 2, checkY, maxZ),
                BlockPos.containing(minX, checkY, (minZ + maxZ) / 2),
                BlockPos.containing(maxX, checkY, (minZ + maxZ) / 2),
                BlockPos.containing(player.getX(), checkY, player.getZ())
        };

        int solidCount = 0;
        int airCount = 0;

        for (BlockPos pos : checkPositions) {
            if (pos != null && level.getBlockState(pos).isSolid()) {
                solidCount++;
            } else {
                airCount++;
            }
        }

        return solidCount > 0 && airCount > 0;
    }

    private boolean isJumping(LocalPlayer player) {
        boolean justLeftGround = wasOnGround && !player.onGround();
        boolean hasUpwardVelocity = player.getDeltaMovement().y > 0;
        return justLeftGround || (!player.onGround() && hasUpwardVelocity);
    }

    /* ================= MAIN LOGIC ================= */

    @Listener
    public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled())
            return;

        LocalPlayer player = mc.player;
        if (player == null || player.getAbilities().flying)
            return;

        if (mc.options.keyShift.isDown())
            return;

        boolean shouldSneak = isJumping(player) || (player.onGround() && isAtEdge(player));

        if (shouldSneak && debugMode.getValue()) {
            logger.debug("Eagle: Sneaking activated");
        }

        event.shift = shouldSneak;
        wasOnGround = player.onGround();
    }
}
