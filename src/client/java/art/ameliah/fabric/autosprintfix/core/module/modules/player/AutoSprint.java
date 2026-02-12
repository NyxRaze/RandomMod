package art.ameliah.fabric.autosprintfix.core.module.modules.player;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.AttackEntityEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.MovementInputEvent;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

/**
 * AutoSprint
 *
 * Automatically sprint resets using W-Tap & S-Tap.
 * Replicates skilled player sprint reset techniques.
 */
public class AutoSprint extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    // Enables debug logging
    public final BooleanSetting debugMode;

    /* ================= STATE ================= */

    private boolean tapping = false;
    private boolean useSTap = false;

    private int remainingTapTicks = 0;

    // Velocity tracking
    private Entity lastTarget = null;
    private Vec3 lastTargetPos = null;
    private Vec3 lastTargetVelocity = null;
    private long lastAttackTime = 0;
    private int consecutiveHits = 0;

    // Pattern state
    private boolean inComboLock = false;
    private int hitsWithoutTap = 0;

    public AutoSprint() {
        super(
                "AutoSprint",
                "Automatically sprint resets using W-Tap & S-Tap, with pro-level adaptive behaviour.",
                ModuleCategory.PLAYER);

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));
    }

    /* ================= UTIL ================= */

    private int gaussianInt(int base, int jitter) {
        if (jitter <= 0)
            return Math.max(0, base);
        double g = ThreadLocalRandom.current().nextGaussian() * (jitter / 2.0);
        return Math.max(0, base + (int) Math.round(g));
    }

    private boolean isCritical(LocalPlayer p) {
        return !p.onGround()
                && p.fallDistance > 0.0F
                && p.getDeltaMovement().y < 0.0D
                && !p.isInWater()
                && !p.isPassenger();
    }

    // Debug logging helper
    private void debug(String message) {
        if (debugMode != null && debugMode.getValue()) {
            logger.debug("[AutoSprint] " + message);
        }
    }

    /* ================= VELOCITY ANALYSIS ================= */

    /**
     * Analyze target's movement relative to player
     * Uses current and previous position/velocity for accurate detection
     */
    private TargetVelocityState analyzeTargetVelocity(LocalPlayer player, Entity target) {

        TargetVelocityState state = new TargetVelocityState();

        Vec3 targetVel = target.getDeltaMovement();
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        Vec3 playerToTarget = targetPos.subtract(playerPos);

        // Avoid division by zero
        if (playerToTarget.lengthSqr() > 0.001) {
            playerToTarget = playerToTarget.normalize();

            // Calculate how much target is moving toward/away from us
            double relativeVelocity = targetVel.dot(playerToTarget);

            state.movingAway = relativeVelocity > 0.15;
            state.movingToward = relativeVelocity < -0.15;
        } else {
            state.movingAway = false;
            state.movingToward = false;
        }

        // Calculate absolute speeds
        double targetSpeed = targetVel.horizontalDistance();
        double playerSpeed = player.getDeltaMovement().horizontalDistance();

        state.stationary = targetSpeed < 0.05;
        state.speed = targetSpeed;

        // === USE HISTORICAL DATA FOR BETTER ANALYSIS ===

        if (lastTarget == target && lastTargetPos != null) {
            // Calculate actual distance change since last attack
            double previousDistance = playerPos.distanceTo(lastTargetPos);
            double currentDistance = playerPos.distanceTo(targetPos);
            state.distanceChange = currentDistance - previousDistance;

            debug(String.format("Distance change: %.3f (was %.2f, now %.2f)",
                    state.distanceChange, previousDistance, currentDistance));

            // Target is escaping if distance increased significantly
            state.escaping = state.distanceChange > 0.3 || (state.movingAway && targetSpeed > playerSpeed);

            // Target is closing in if distance decreased
            state.closing = state.distanceChange < -0.2;
        } else {
            // First hit on this target or different target
            state.distanceChange = 0;
            state.escaping = state.movingAway && targetSpeed > playerSpeed;
            state.closing = state.movingToward;
            debug("First hit on target or new target - no historical data");
        }

        // === VELOCITY TREND ANALYSIS ===

        if (lastTarget == target && lastTargetVelocity != null) {
            // Compare current velocity to previous velocity
            double previousSpeed = lastTargetVelocity.horizontalDistance();
            double currentSpeed = targetSpeed;

            // Detect acceleration (opponent speeding up to escape)
            state.accelerating = currentSpeed > previousSpeed + 0.05;

            // Detect deceleration (opponent slowing down, maybe turning to fight)
            state.decelerating = currentSpeed < previousSpeed - 0.05;

            debug(String.format("Speed change: %.3f → %.3f (accel: %b, decel: %b)",
                    previousSpeed, currentSpeed, state.accelerating, state.decelerating));

            // Detect direction change
            if (lastTargetVelocity.horizontalDistanceSqr() > 0.01 && targetVel.horizontalDistanceSqr() > 0.01) {
                Vec3 lastDir = lastTargetVelocity.normalize();
                Vec3 currentDir = targetVel.normalize();
                double dotProduct = lastDir.dot(currentDir);
                state.changedDirection = dotProduct < 0.7; // Turned more than ~45 degrees

                if (state.changedDirection) {
                    debug(String.format("Target changed direction! (dot: %.2f)", dotProduct));
                }
            } else {
                state.changedDirection = false;
            }
        } else {
            state.accelerating = false;
            state.decelerating = false;
            state.changedDirection = false;
        }

        return state;
    }

    private static class TargetVelocityState {
        boolean movingAway = false;
        boolean movingToward = false;
        boolean stationary = false;
        boolean escaping = false;
        boolean closing = false;
        boolean accelerating = false;
        boolean decelerating = false;
        boolean changedDirection = false;
        double speed = 0;
        double distanceChange = 0;
    }

    /**
     * Detect if we're in a combo lock (opponent can't escape)
     */
    private boolean isInComboLock(LocalPlayer player, Entity target, double distance, TargetVelocityState velState) {
        if (!(target instanceof LivingEntity living))
            return false;

        long timeSinceLastHit = System.currentTimeMillis() - lastAttackTime;
        boolean recentHit = timeSinceLastHit < 600;
        boolean sameTarget = lastTarget == target;
        boolean inRange = distance >= 1.8 && distance <= 2.8;
        boolean inHitstun = living.hurtTime > 0;

        // Additional check: target is not successfully escaping
        boolean notEscaping = !velState.escaping && velState.distanceChange < 0.5;

        boolean comboLock = recentHit && sameTarget && inRange && inHitstun && consecutiveHits >= 2 && notEscaping;

        if (comboLock) {
            debug(String.format("COMBO LOCK! (hits: %d, hurtTime: %d, distance: %.2f)",
                    consecutiveHits, living.hurtTime, distance));
        }

        return comboLock;
    }

    /* ================= SMART TAP TYPE SELECTION ================= */

    /**
     * Pro-level W-Tap vs S-Tap decision making
     */
    private boolean shouldUseSTap(LocalPlayer player, Entity target, double distance,
            float cooldown, TargetVelocityState velState) {

        // Never S-Tap during crits
        if (isCritical(player))
            return false;

        // Must be on ground and sprinting
        if (!player.onGround() || !player.isSprinting())
            return false;

        // === CLOSE RANGE AGGRESSIVE S-TAP ===
        if (distance < 2.5 && cooldown > 0.88f) {
            debug("S-Tap: Close range aggressive");
            return true;
        }

        // === COMBO LOCK S-TAP PATTERN ===
        if (inComboLock && consecutiveHits % 2 == 0) {
            debug("S-Tap: Combo lock pattern");
            return true;
        }

        // === OPPONENT RUSHING (TRADING) ===
        if (velState.movingToward && velState.speed > 0.2) {
            debug("S-Tap: Opponent rushing");
            return true;
        }

        // === OPPONENT CLOSING IN (detected from position history) ===
        if (velState.closing && distance < 2.8) {
            debug("S-Tap: Opponent closing in");
            return true;
        }

        // === STATIONARY TARGET ===
        if (velState.stationary && distance < 2.6 && cooldown > 0.85f) {
            debug("S-Tap: Stationary target");
            return true;
        }

        // === OPPONENT DECELERATED (stopping to fight) ===
        if (velState.decelerating && velState.changedDirection && distance < 2.8) {
            debug("S-Tap: Opponent decelerated + turned");
            return true;
        }

        debug("W-Tap: Default");
        return false;
    }

    /* ================= HOLD DURATION CALCULATION ================= */

    /**
     * Calculate hold duration based on pro player patterns
     */
    private int calculateHoldDuration(LocalPlayer player, Entity target, double distance,
            float cooldown, TargetVelocityState velState) {

        int hold;
        int baseHold;

        // === BASE DISTANCE MAPPING ===
        if (distance < 1.5) {
            baseHold = hold = 3;
        } else if (distance < 2.0) {
            baseHold = hold = 4;
        } else if (distance < 2.5) {
            baseHold = hold = 5;
        } else if (distance < 3.0) {
            baseHold = hold = 6;
        } else if (distance < 3.5) {
            baseHold = hold = 7;
        } else {
            baseHold = hold = 9;
        }

        debug(String.format("Base hold: %d ticks (distance: %.2f)", baseHold, distance));

        // === VELOCITY ADJUSTMENTS ===

        if (velState.escaping) {
            // CHASE MODE: Short taps to maintain speed
            hold = Math.min(hold, 3);
            debug("CHASE MODE: Reduced to 3 ticks max");

            // Skip some taps entirely in chase mode
            if (hitsWithoutTap < 2) {
                hold = 0;
                debug("Skipping tap to maintain chase speed");
            }
        }

        // Opponent accelerating away = even shorter taps
        if (velState.accelerating && velState.movingAway) {
            hold = Math.min(hold, 2);
            debug("Target accelerating away: 2 ticks max");
        }

        if (velState.movingToward || velState.closing) {
            // TRADE SCENARIO: Longer taps for better trades
            int oldHold = hold;
            hold = Math.min(hold + 2, 9);
            debug(String.format("TRADE SCENARIO: %d → %d ticks", oldHold, hold));
        }

        // Opponent decelerating = they might fight, prepare for trade
        if (velState.decelerating && !velState.escaping) {
            hold = Math.min(hold + 1, 8);
            debug("Target decelerating: +1 tick");
        }

        // Opponent changed direction = be ready for anything
        if (velState.changedDirection) {
            hold = 5;
            debug("Target changed direction: reset to 5 ticks");
        }

        if (velState.stationary) {
            // FREE COMBO: Consistent medium taps
            hold = 5;
            debug("Stationary target: 5 ticks");
        }

        // === DISTANCE CHANGE ADJUSTMENTS ===

        if (velState.distanceChange > 0.5) {
            hold = Math.min(hold, 3);
            debug("Distance increasing rapidly: 3 ticks max");
        } else if (velState.distanceChange < -0.3) {
            hold = Math.min(hold + 1, 8);
            debug("Distance decreasing: +1 tick");
        }

        // === COMBO LOCK PATTERN ===
        if (inComboLock) {
            hold = useSTap ? 5 : 4;
            debug(String.format("Combo lock hold: %d ticks", hold));
        }

        // === COOLDOWN ADJUSTMENTS ===
        if (cooldown < 0.70f) {
            int oldHold = hold;
            hold = Math.max(2, hold - 2);
            debug(String.format("Low cooldown (%.0f%%): %d → %d ticks", cooldown * 100, oldHold, hold));
        } else if (cooldown >= 0.85f && cooldown < 0.95f) {
            hold = Math.min(hold + 1, 8);
            debug(String.format("High cooldown (%.0f%%): +1 tick", cooldown * 100));
        } else if (cooldown >= 0.95f) {
            int oldHold = hold;
            hold = Math.max(3, hold - 1);
            debug(String.format("Perfect cooldown (%.0f%%): %d → %d ticks", cooldown * 100, oldHold, hold));
        }

        // === CRIT ADJUSTMENTS ===
        if (isCritical(player)) {
            hold = 3;
            debug("CRIT: 3 ticks");
        }

        // === MOVEMENT STATE ===
        if (!player.onGround()) {
            int oldHold = hold;
            hold = Math.max(2, hold - 2);
            debug(String.format("Airborne: %d → %d ticks", oldHold, hold));
        }

        // === S-TAP NEEDS LONGER HOLD ===
        if (useSTap) {
            hold += 1;
            debug("S-Tap: +1 tick");
        }

        // === LOST SPRINT = FULL RESET ===
        if (!player.isSprinting()) {
            hold = 8;
            debug("Lost sprint: 8 ticks (full reset)");
        }

        // Apply jitter
        int preJitter = hold;
        hold = gaussianInt(hold, 1);

        // Clamp between 2-10 ticks
        hold = Math.max(2, Math.min(10, hold));

        debug(String.format("FINAL HOLD: %d ticks (pre-jitter: %d, clamped: %d)", hold, preJitter, hold));

        return hold;
    }

    /* ================= DELAY CALCULATION ================= */

    private int calculateDelay(LocalPlayer player, float cooldown, TargetVelocityState velState) {

        int delay = 1;

        if (!player.onGround())
            delay += 1;

        if (!player.isSprinting())
            delay = 0;

        if (cooldown < 0.75f)
            delay += 1;

        if (cooldown > 0.92f)
            delay = Math.max(0, delay - 1);

        // Escaping or accelerating away = minimal delay
        if (velState.escaping || velState.accelerating)
            delay = 0;

        // Opponent closing in = slight delay to time the trade
        if (velState.closing)
            delay = Math.min(delay + 1, 2);

        if (inComboLock)
            delay = 1;

        delay = gaussianInt(delay, 1);
        delay = Math.max(0, Math.min(3, delay));

        debug(String.format("Delay: %d ticks", delay));

        return delay;
    }

    /* ================= MODULE ================= */

    @Override
    protected void onEnable() {
        resetState();
        debug("=== AutoSprint ENABLED ===");
    }

    @Override
    protected void onDisable() {
        resetState();
        debug("=== AutoSprint DISABLED ===");
    }

    private void resetState() {
        tapping = false;
        remainingTapTicks = 0;
        lastTarget = null;
        lastTargetPos = null;
        lastTargetVelocity = null;
        consecutiveHits = 0;
        inComboLock = false;
        hitsWithoutTap = 0;
        lastAttackTime = 0;
    }

    /* ================= ATTACK ================= */

    @Listener
    public void onAttack(AttackEntityEvent event) {

        LocalPlayer player = mc.player;
        Entity entity = event.entity;

        if (player == null || entity == null)
            return;

        if (!player.input.hasForwardImpulse())
            return;

        debug("\n=== NEW ATTACK ===");

        // Update combo tracking
        if (lastTarget == entity) {
            consecutiveHits++;
            debug(String.format("Consecutive hits: %d", consecutiveHits));
        } else {
            consecutiveHits = 1;
            debug("New target - reset combo counter");
        }

        double distance = player.distanceTo(entity);
        float cooldown = player.getAttackStrengthScale(0.5F);

        debug(String.format("Distance: %.2f | Cooldown: %.0f%% | Sprinting: %b | OnGround: %b",
                distance, cooldown * 100, player.isSprinting(), player.onGround()));

        // Analyze target velocity (uses lastTargetPos and lastTargetVelocity)
        TargetVelocityState velState = analyzeTargetVelocity(player, entity);

        debug(String.format("VelState: escaping=%b, closing=%b, stationary=%b, speed=%.3f",
                velState.escaping, velState.closing, velState.stationary, velState.speed));

        // Detect combo lock (FIXED: now passes player parameter)
        inComboLock = isInComboLock(player, entity, distance, velState);

        // Determine tap type
        useSTap = shouldUseSTap(player, entity, distance, cooldown, velState);

        // === CHASE MODE: Skip some taps ===
        if (velState.escaping && distance > 3.0) {
            hitsWithoutTap++;
            if (hitsWithoutTap < 2) {
                debug(String.format("Skipping tap %d/2 for chase mode", hitsWithoutTap));
                updateTracking(entity);
                return;
            }
        }

        hitsWithoutTap = 0;

        // Calculate hold duration
        int hold = calculateHoldDuration(player, entity, distance, cooldown, velState);

        // No tap needed
        if (hold == 0) {
            debug("Hold = 0, no tap executed");
            updateTracking(entity);
            return;
        }

        // Skip if already tapping
        if (tapping) {
            debug("Already tapping, skipping this tap");
            updateTracking(entity);
            return;
        }

        // Calculate delay
        int delay = calculateDelay(player, cooldown, velState);

        debug(String.format(">>> EXECUTING: %s-TAP | Hold: %d ticks | Delay: %d ticks <<<",
                useSTap ? "S" : "W", hold, delay));

        // IMPORTANT: Update tracking BEFORE the async callback
        updateTracking(entity);

        final int finalHold = hold;
        AsyncUtil.delayTicks(delay, () -> {
            tapping = true;
            remainingTapTicks = finalHold;
            debug(String.format("Tap started! Holding for %d ticks", finalHold));
        });
    }

    private void updateTracking(Entity entity) {
        lastTarget = entity;
        lastTargetPos = entity.position();
        lastTargetVelocity = entity.getDeltaMovement();
        lastAttackTime = System.currentTimeMillis();
    }

    /* ================= INPUT ================= */

    @Listener
    public void onMovementInput(MovementInputEvent event) {

        if (!tapping)
            return;

        if (useSTap) {
            event.forwards = false;
            event.backwards = true;
        } else {
            event.forwards = false;
            event.backwards = false;
        }

        remainingTapTicks--;

        if (remainingTapTicks <= 0) {
            tapping = false;
            debug("Tap released!");

            event.forwards = true;
            event.backwards = false;
        }
    }
}
