package art.ameliah.fabric.autosprintfix.core.util;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;

/**
 * InteractionUtil - Utilities for simulating player interactions.
 * Cached Minecraft keys to avoid repeated reflection lookups.
 */
public class InteractionUtil {

    // Cached key mappings (reflection only once)
    private static final InputConstants.Key keyAttack = fetchKey(() -> Minecraft.getInstance().options.keyAttack);
    private static final InputConstants.Key keyUse = fetchKey(() -> Minecraft.getInstance().options.keyUse);
    private static final InputConstants.Key keyJump = fetchKey(() -> Minecraft.getInstance().options.keyJump);
    private static final InputConstants.Key keySneak = fetchKey(() -> Minecraft.getInstance().options.keyShift);
    private static final InputConstants.Key keySprint = fetchKey(() -> Minecraft.getInstance().options.keySprint);
    private static final InputConstants.Key keyForward = fetchKey(() -> Minecraft.getInstance().options.keyUp);
    private static final InputConstants.Key keyBack = fetchKey(() -> Minecraft.getInstance().options.keyDown);
    private static final InputConstants.Key keyLeft = fetchKey(() -> Minecraft.getInstance().options.keyLeft);
    private static final InputConstants.Key keyRight = fetchKey(() -> Minecraft.getInstance().options.keyRight);

    /**
     * Fetches the InputConstants.Key from a KeyMapping using reflection.
     * Returns null if key cannot be retrieved.
     */
    private static InputConstants.Key fetchKey(KeyMappingSupplier supplier) {
        try {
            KeyMapping km = supplier.get();
            if (km == null)
                return null;

            Field field = KeyMapping.class.getDeclaredField("key");
            field.setAccessible(true);
            return (InputConstants.Key) field.get(km);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Functional interface to lazily supply a KeyMapping for caching.
     */
    @FunctionalInterface
    private interface KeyMappingSupplier {
        KeyMapping get();
    }

    /**
     * Checks if any GUI screen is open.
     */
    private static boolean isScreenOpen() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.screen != null;
    }

    /**
     * Simulates pressing a key.
     */
    private static boolean pressKey(InputConstants.Key key, boolean checkScreen, int duration) {
        try {
            if (checkScreen && isScreenOpen())
                return false;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || key == null)
                return false;

            KeyMapping.set(key, true);
            KeyMapping.click(key);

            if (duration != 0) {
                AsyncUtil.delayTicks(duration, () -> mc.execute(() -> KeyMapping.set(key, false)));
            } else {
                KeyMapping.set(key, false);
            }

            return true;
        } catch (Exception e) {
            ModLogger.getInstance().error("Key press error: {}", e.getMessage());
            return false;
        }
    }

    // ==========================
    // Public API
    // ==========================

    public static boolean attack(boolean checkScreen, int duration) {
        return pressKey(keyAttack, checkScreen, duration);
    }

    public static boolean use(boolean checkScreen, int duration) {
        return pressKey(keyUse, checkScreen, duration);
    }

    public static boolean jump(boolean checkScreen, int duration) {
        return pressKey(keyJump, checkScreen, duration);
    }

    public static boolean sneak(boolean checkScreen, int duration) {
        return pressKey(keySneak, checkScreen, duration);
    }

    public static boolean sprint(boolean checkScreen, int duration) {
        return pressKey(keySprint, checkScreen, duration);
    }

    public static boolean moveKey(boolean checkScreen, String keyType, int duration) {
        switch (keyType.toLowerCase()) {
            case "forward":
                return pressKey(keyForward, checkScreen, duration);
            case "back":
                return pressKey(keyBack, checkScreen, duration);
            case "left":
                return pressKey(keyLeft, checkScreen, duration);
            case "right":
                return pressKey(keyRight, checkScreen, duration);
            default:
                return false;
        }
    }
}
