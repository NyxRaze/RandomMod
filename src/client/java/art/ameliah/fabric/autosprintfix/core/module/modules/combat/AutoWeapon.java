package art.ameliah.fabric.autosprintfix.core.module.modules.combat;

import art.ameliah.fabric.autosprintfix.core.module.AutoRegister;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;

import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.AttackEntityEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.TickEvent;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;

import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.NumberSetting;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil;
import art.ameliah.fabric.autosprintfix.core.util.AsyncUtil.Token;
import art.ameliah.fabric.autosprintfix.core.util.InteractionUtil;
import art.ameliah.fabric.autosprintfix.core.util.ItemUtil;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * AutoWeapon
 *
 * Automatically swaps to the best combat weapon depending on situation.
 *
 * Features:
 * - Shield breaking with axes
 * - Density/Breach mace fall detection
 * - Optional stun-slam combo (axe -> mace double hit)
 * - Configurable swap-back delay using range setting
 */
@AutoRegister(priority = 0)
public class AutoWeapon extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private final ModLogger logger = ModLogger.getInstance();

    // Settings
    private final RangeSetting swapBackDelay; // Swap-back delay with random range
    public final NumberSetting fallThreshold; // Minimum fall distance for density mace

    private final BooleanSetting shieldBreaker; // Enable automatic shield breaking
    private final BooleanSetting debugMode; // Enable debug logging
    private final BooleanSetting stunSlam; // Enable axe -> mace combo
    public final BooleanSetting maceSwap; // Enable mace swapping

    // Runtime state
    private boolean lastAttackDown = false; // Tracks attack key state
    private boolean isAttacking = false; // Prevents re-entry during swaps
    private long lastShieldDetectionTime = 0; // Grace window for shield prediction
    private Token swapBackToken; // Scheduled swap-back task

    public AutoWeapon() {
        super(
                "AutoWeapon",
                "Smart weapon swapping with shield breaking, mace fall detection and combo attacks.",
                ModuleCategory.COMBAT);

        this.shieldBreaker = addSetting(new BooleanSetting("ShieldBreaker", "Automatically break shields", true));
        this.maceSwap = addSetting(new BooleanSetting("MaceSwap", "Automatically swap to mace", true));
        this.stunSlam = addSetting(new BooleanSetting("StunSlam", "Axe into mace combo attack", true));
        this.debugMode = addSetting(new BooleanSetting("Debug Mode", "Enable debug logging", false));

        this.swapBackDelay = addSetting(new RangeSetting(
                "Swap-back Delay (ticks)",
                "Delay before switching back to original slot",
                1, 3, 0, 10));

        this.fallThreshold = addSetting(new NumberSetting(
                "Density mace fall threshold",
                "Minimum fall distance to use density mace",
                6, 0, 20, 1));
    }

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (event.enabled) {
            if (debugMode.getValue()) {
                logger.debug("AutoWeapon enabled");
            }
        } else {
            this.lastAttackDown = false;
            this.isAttacking = false;

            if (debugMode.getValue()) {
                logger.debug("AutoWeapon disabled");
            }
        }
    }

    /**
     * Schedules returning to the original hotbar slot.
     *
     * @param inventory    Player inventory
     * @param originalSlot Slot to return to
     */
    private void scheduleSwapBack(Inventory inventory, int originalSlot) {
        if (this.swapBackToken != null) {
            this.swapBackToken.cancel();
        }

        int delay = swapBackDelay.getRandomValueInt();

        if (debugMode.getValue()) {
            logger.debug("AutoWeapon swap-back delay: {}", delay);
        }

        this.swapBackToken = AsyncUtil.delayTicks(delay, () -> {
            inventory.setSelectedSlot(originalSlot);
            this.isAttacking = false;
        });
    }

    /**
     * Finds the best axe in the hotbar based on Sharpness level.
     *
     * @param player Local player instance
     * @return Hotbar slot of best axe or -1 if none found
     */
    private int findBestAxe(LocalPlayer player) {
        int bestSlot = -1;
        int bestSharpness = -1;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.getItem() == Items.NETHERITE_AXE
                    || stack.getItem() == Items.DIAMOND_AXE
                    || stack.getItem() == Items.IRON_AXE
                    || stack.getItem() == Items.STONE_AXE
                    || stack.getItem() == Items.WOODEN_AXE) {

                int sharpness = ItemUtil.getEnchantment(stack, "sharpness");

                if (sharpness > bestSharpness) {
                    bestSharpness = sharpness;
                    bestSlot = slot;
                }
            }
        }

        return bestSlot;
    }

    /**
     * Finds the best mace depending on fall distance and enchantments.
     *
     * @param player Local player instance
     * @return Hotbar slot of best mace or -1 if none found
     */
    private int findBestMace(LocalPlayer player) {
        int densitySlot = -1;
        int densityLevel = -1;
        int breachSlot = -1;
        int breachLevel = -1;
        int anyMaceSlot = -1;

        double fallDistance = player.fallDistance;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.getItem() == Items.MACE) {

                if (anyMaceSlot == -1)
                    anyMaceSlot = slot;

                int density = ItemUtil.getEnchantment(stack, "density");
                int breach = ItemUtil.getEnchantment(stack, "breach");

                if (density > densityLevel) {
                    densityLevel = density;
                    densitySlot = slot;
                }

                if (breach > breachLevel) {
                    breachLevel = breach;
                    breachSlot = slot;
                }
            }
        }

        boolean hasBreach = breachSlot != -1;
        boolean hasDensity = densitySlot != -1;

        if (hasBreach) {
            if (hasDensity && fallDistance >= fallThreshold.getIntValue()) {
                return densitySlot;
            }
            return breachSlot;
        }

        if (hasDensity && fallDistance >= 1)
            return densitySlot;
        if (anyMaceSlot != -1 && fallDistance >= 1)
            return anyMaceSlot;

        return -1;
    }

    /**
     * Handles fake swap when attacking air.
     */
    @Listener
    public void onTick(TickEvent event) {
        LocalPlayer player = mc.player;

        boolean attackDown = mc.options.keyAttack.isDown();
        boolean attackPressed = attackDown && !this.lastAttackDown;
        this.lastAttackDown = attackDown;

        if (player == null || mc.level == null || !attackPressed || this.isAttacking)
            return;

        if (mc.crosshairPickEntity != null)
            return;

        Inventory inventory = player.getInventory();

        int originalSlot = inventory.getSelectedSlot();
        int maceSlot = findBestMace(player);

        if (maceSlot == -1 || maceSlot == originalSlot)
            return;

        this.isAttacking = true;

        inventory.setSelectedSlot(maceSlot);
        scheduleSwapBack(inventory, originalSlot);
    }

    /**
     * Handles entity attack logic including shield breaking and combo.
     */
    @Listener
    public void onAttack(AttackEntityEvent event) {
        LocalPlayer player = mc.player;
        Entity entity = event.entity;

        if (player == null || this.isAttacking || entity == null || !entity.isAlive())
            return;

        this.isAttacking = true;

        Inventory inventory = player.getInventory();

        int originalSlot = inventory.getSelectedSlot();
        int maceSlot = findBestMace(player);
        int axeSlot = findBestAxe(player);

        boolean isBlockingVisible = false;

        if (entity instanceof LivingEntity target) {
            ItemStack targetItem = target.getUseItem();

            boolean holdingShield = !targetItem.isEmpty() && targetItem.getItem() == Items.SHIELD;
            boolean activelyBlocking = target.isUsingItem() && holdingShield;

            long now = System.currentTimeMillis();

            if (!activelyBlocking && (now - lastShieldDetectionTime) < 50L) {
                isBlockingVisible = true;
            } else if (activelyBlocking) {
                isBlockingVisible = true;
                lastShieldDetectionTime = now;
            }
        }

        boolean doStunSlam = stunSlam.getValue()
                && isBlockingVisible
                && axeSlot != -1
                && maceSlot != -1;

        if (doStunSlam) {
            inventory.setSelectedSlot(axeSlot);
            InteractionUtil.attack(true, 0);

            AsyncUtil.delayTicks(1, () -> {
                inventory.setSelectedSlot(maceSlot);
                InteractionUtil.attack(true, 0);
                InteractionUtil.attack(true, 0);

                scheduleSwapBack(inventory, originalSlot);
            });

        } else {
            int slotToUse = (shieldBreaker.getValue() && isBlockingVisible) ? axeSlot
                    : (maceSwap.getValue() ? maceSlot : -1);

            if (slotToUse != -1 && slotToUse != originalSlot) {
                inventory.setSelectedSlot(slotToUse);
                InteractionUtil.attack(true, 0);
                InteractionUtil.attack(true, 0);
            }

            scheduleSwapBack(inventory, originalSlot);
        }
    }
}
