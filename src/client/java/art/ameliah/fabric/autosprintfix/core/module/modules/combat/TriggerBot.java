package art.ameliah.fabric.autosprintfix.core.module.modules.combat;

import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.modules.misc.IgnoreList;
import art.ameliah.fabric.autosprintfix.AutoSprintFix;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.TickEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;

import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.StringSetting;

import art.ameliah.fabric.autosprintfix.core.util.InteractionUtil;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.util.FriendsManager;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.Vec3;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * TriggerBot
 *
 * Legit-style automatic combat module.
 *
 * Features:
 * - Player target locking
 * - IgnoreList + Friends integration
 * - Shield-aware axe skipping
 * - Optional crit preference
 * - Human-like cooldown variance via RangeSetting
 */
@AutoRegister(priority = 0)
public class TriggerBot extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    /* ================= SETTINGS ================= */

    private final RangeSetting cooldownRange; // Attack cooldown percent range
    private final BooleanSetting preferCrits; // Only attack while falling
    private final BooleanSetting debugMode; // Debug logging
    private final StringSetting blacklist; // Entity blacklist

    /* ================= STATE ================= */

    private boolean isAttacking = false; // Prevents double fire
    private Entity lockedTarget = null; // Locked player target
    private long lastShieldDetectionTime = 0; // Shield grace timer

    public TriggerBot() {
        super(
                "TriggerBot",
                "Automatically attacks aimed entities with legit locking.",
                ModuleCategory.COMBAT);

        this.cooldownRange = addSetting(new RangeSetting(
                "Cooldown %",
                "Required cooldown percent before attacking",
                94, 99,
                0, 100));

        this.preferCrits = addSetting(new BooleanSetting(
                "Prefer Crits",
                "Only attack while falling",
                true));

        this.debugMode = addSetting(new BooleanSetting(
                "Debug Mode",
                "Enable debug logging",
                false));

        this.blacklist = addSetting(new StringSetting(
                "Blacklist",
                "Comma separated entity ids to ignore",
                "wind_charge, end_crystal"));
    }

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("TriggerBot enabled");
            }
        } else {
            lockedTarget = null;
            isAttacking = false;

            if (debugMode.getValue()) {
                logger.debug("TriggerBot disabled");
            }
        }
    }

    /**
     * Returns the player's attack cooldown percent.
     */
    private float cooldownPercent(LocalPlayer player) {
        return player.getAttackStrengthScale(1.0F) * 100F;
    }

    /**
     * Returns true if entity type matches blacklist.
     */
    private boolean isBlacklisted(Entity entity) {
        if (entity == null)
            return true;

        String raw = blacklist.getValue();
        if (raw == null || raw.isEmpty())
            return false;

        String type = entity.getType().toString().toLowerCase();

        for (String s : raw.split(",")) {
            if (type.contains(s.trim().toLowerCase()))
                return true;
        }

        return false;
    }

    /**
     * Checks if two players share same nametag color or prefix.
     */
    private boolean hasSameNametag(Player a, Player b) {
        ChatFormatting ca = getNameColor(a);
        ChatFormatting cb = getNameColor(b);

        if (ca != null && cb != null && ca == cb)
            return true;

        String pa = getNamePrefix(a);
        String pb = getNamePrefix(b);

        return !pa.isEmpty() && pa.equals(pb);
    }

    /**
     * Extracts display name color.
     */
    @SuppressWarnings("null")
    private ChatFormatting getNameColor(Player p) {
        Component name = p.getDisplayName();

        if (name.getStyle() != null && name.getStyle().getColor() != null) {
            return ChatFormatting.getByName(
                    name.getStyle().getColor().serialize());
        }

        return null;
    }

    /**
     * Extracts nametag prefix.
     */
    private String getNamePrefix(Player p) {
        String display = p.getDisplayName().getString();
        String real = p.getName().getString();

        display = display.replaceAll("§.", "");

        int idx = display.indexOf(real);
        if (idx > 0)
            return display.substring(0, idx).trim();

        return "";
    }

    /**
     * Determines if an entity should be ignored based on friends, team, and
     * IgnoreList module.
     */
    private boolean shouldIgnore(Entity entity, LocalPlayer player) {
        if (!(entity instanceof Player target))
            return false;

        // Check IgnoreList module
        Module ignoreModule = AutoSprintFix.getInstance().getModuleManager().getModule("IgnoreList");
        if (ignoreModule instanceof IgnoreList il && il.isEnabled()) {

            // Friend check
            if (il.isFriendCheckEnabled() && FriendsManager.isFriend(target.getName().getString()))
                return true;

            // Team / nametag check
            if (il.isTeamCheckEnabled() && hasSameNametag(target, player))
                return true;
        }

        return false;
    }

    /**
     * 90° FOV check.
     */
    private boolean isIn90Fov(Entity entity, LocalPlayer player) {
        Vec3 toEntity = entity.position()
                .add(0, entity.getBbHeight() * 0.5, 0)
                .subtract(player.getEyePosition())
                .normalize();

        Vec3 look = player.getViewVector(1.0F).normalize();
        return look.dot(toEntity) >= 0.707;
    }

    /**
     * Updates locked player target.
     */
    @SuppressWarnings("null")
    private void updateLockedTarget(LocalPlayer player, Entity aimed) {
        if (lockedTarget != null) {
            if (!lockedTarget.isAlive()
                    || player.distanceToSqr(lockedTarget) > 20.25
                    || !isIn90Fov(lockedTarget, player)
                    || shouldIgnore(lockedTarget, player)
                    || isBlacklisted(lockedTarget)) {

                lockedTarget = null;
            }

            return;
        }

        if (aimed instanceof Player
                && aimed.isAlive()
                && !shouldIgnore(aimed, player)
                && !isBlacklisted(aimed)) {

            lockedTarget = aimed;
        }
    }

    /**
     * Returns true if attacker holds axe and target blocks with shield facing
     * attacker.
     */
    private boolean isAxeVsShield(LocalPlayer self, Entity target) {
        if (!(target instanceof LivingEntity living))
            return false;

        if (!(self.getMainHandItem().getItem() instanceof AxeItem))
            return false;

        ItemStack useItem = living.getUseItem();

        boolean blocking = living.isUsingItem()
                && !useItem.isEmpty()
                && useItem.getItem() instanceof ShieldItem;

        long now = System.currentTimeMillis();

        if (!blocking && (now - lastShieldDetectionTime) < 50L) {
            blocking = true;
        } else if (blocking) {
            lastShieldDetectionTime = now;
        }

        if (!blocking)
            return false;

        Vec3 targetLook = living.getViewVector(1.0F).normalize();
        Vec3 toSelf = self.position().subtract(living.position()).normalize();

        return targetLook.dot(toSelf) > 0.2;
    }

    /**
     * Main combat loop.
     */
    @Listener
    public void onTick(TickEvent event) {
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        Entity aimed = mc.crosshairPickEntity;
        updateLockedTarget(player, aimed);

        if (isAttacking || aimed == null)
            return;

        if (aimed instanceof Player) {
            if (lockedTarget == null || aimed != lockedTarget)
                return;

            if (shouldIgnore(aimed, player))
                return;
        } else {
            if (isBlacklisted(aimed))
                return;
        }

        if (preferCrits.getValue()) {
            if (player.getDeltaMovement().y >= 0)
                return;
        }

        if (isAxeVsShield(player, aimed))
            return;

        float cooldown = cooldownPercent(player);
        int required = cooldownRange.getRandomValueInt();

        if (cooldown < required)
            return;

        isAttacking = true;

        mc.execute(() -> {
            InteractionUtil.attack(true, 0);

            AsyncUtil.delayTicks(6, () -> isAttacking = false);

            if (debugMode.getValue()) {
                logger.debug(
                        "TriggerBot attacked {} at {:.1f}%",
                        aimed.getName().getString(),
                        cooldown);
            }
        });
    }
}
