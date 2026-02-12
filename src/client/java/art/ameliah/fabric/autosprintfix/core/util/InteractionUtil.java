package art.ameliah.fabric.autosprintfix.core.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.InputConstants;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

import java.lang.reflect.Field;

/**
 * InteractionUtil - Utilities for player interactions.
 * Simulates key presses to trigger Minecraft's normal input handling pipeline.
 * Automatically checks if a screen is open and prevents interactions when UI is
 * displayed.
 */
public class InteractionUtil {

    private static final ModLogger logger = ModLogger.getInstance();

    /**
     * Checks if any GUI screen is currently open.
     * 
     * @return true if a screen is open, false if the game is in normal play mode
     */
    private static boolean isScreenOpen() {
        Minecraft mc = Minecraft.getInstance();

        return mc != null && mc.screen != null;
    }

    /**
     * Gets the InputConstants.Key from a KeyMapping using reflection
     */
    public static InputConstants.Key getKeyFromKeyMapping(KeyMapping keyMapping) {
        try {
            // Try the direct field first
            Field key = KeyMapping.class.getDeclaredField("key");
            key.setAccessible(true);

            return (InputConstants.Key) key.get(keyMapping);
        } catch (NoSuchFieldException e) {
            // Try alternative field names (obfuscated in some versions)
            try {
                for (Field field : KeyMapping.class.getDeclaredFields()) {
                    if (InputConstants.Key.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object value = field.get(keyMapping);
                        if (value instanceof InputConstants.Key) {
                            return (InputConstants.Key) value;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Simulates pressing the attack key.
     * Optionally prevents interaction if any screen (GUI, inventory, etc) is open.
     * Simulates the actual attack key press, allowing Minecraft to handle all
     * validation.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param duration    the duration in ticks to hold the key
     * @return true if attack key simulation was successful, false if screen is open
     *         or error occurred
     */
    public static boolean attack(boolean checkScreen, int duration) {
        try {
            // Check if screen is open - do not attack if UI is active and checkScreen is
            // true
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            // Simulate pressing and releasing the attack key
            KeyMapping keyAttack = mc.options.keyAttack;
            InputConstants.Key key = getKeyFromKeyMapping(keyAttack);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Attack error: {}", e.getMessage());

            return false;
        }
    }

    /**
     * Simulates pressing the use key (right click).
     * Optionally prevents interaction if any screen is open.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param duration    the duration in ticks to hold the key
     * @return true if use key simulation was successful, false if screen is open or
     *         error occurred
     */
    public static boolean use(boolean checkScreen, int duration) {
        try {
            // Check if screen is open
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            // Simulate pressing and releasing the use key
            KeyMapping keyUse = mc.options.keyUse;
            InputConstants.Key key = getKeyFromKeyMapping(keyUse);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (

        Exception e) {
            logger.error("Use error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simulates pressing the jump key.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param duration    the duration in ticks to hold the key
     * @return true if jump key simulation was successful
     */
    public static boolean jump(boolean checkScreen, int duration) {
        try {
            // Check if screen is open
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            // Get the jump key using reflection for consistency
            KeyMapping keyJump = mc.options.keyJump;
            InputConstants.Key key = getKeyFromKeyMapping(keyJump);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Jump error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simulates pressing a movement key.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param keyType     the movement key type ("forward", "back", "left", "right")
     * @param duration    the duration in ticks to hold the key
     * @return true if key simulation was successful
     */
    public static boolean moveKey(boolean checkScreen, String keyType, int duration) {
        try {
            // Check if screen is open
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            KeyMapping keyMapping = null;

            switch (keyType.toLowerCase()) {
                case "forward":
                    keyMapping = mc.options.keyUp;
                    break;
                case "back":
                    keyMapping = mc.options.keyDown;
                    break;
                case "left":
                    keyMapping = mc.options.keyLeft;
                    break;
                case "right":
                    keyMapping = mc.options.keyRight;
                    break;
                default:
                    return false;
            }

            InputConstants.Key key = getKeyFromKeyMapping(keyMapping);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("MoveKey error for {}: {}", keyType, e.getMessage());
            return false;
        }
    }

    /**
     * Simulates pressing the sneak key.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param duration    the duration in ticks to hold the key
     * @return true if sneak key simulation was successful
     */
    public static boolean sneak(boolean checkScreen, int duration) {
        try {
            // Check if screen is open
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            // Get the sneak key using reflection
            KeyMapping keySneak = mc.options.keyShift;
            InputConstants.Key key = getKeyFromKeyMapping(keySneak);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Sneak error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simulates pressing the sprint key.
     * 
     * @param checkScreen if true, prevents interaction when a screen is open
     * @param duration    the duration in ticks to hold the key
     * @return true if sprint key simulation was successful
     */
    public static boolean sprint(boolean checkScreen, int duration) {
        try {
            // Check if screen is open
            if (checkScreen && isScreenOpen()) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return false;
            }

            // Get the sprint key using reflection
            KeyMapping keySprint = mc.options.keySprint;
            InputConstants.Key key = getKeyFromKeyMapping(keySprint);

            if (key != null) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);

                if (duration != 0) {
                    AsyncUtil.delayTicks(duration, () -> {
                        mc.execute(() -> KeyMapping.set(key, false));
                    });
                } else {
                    KeyMapping.set(key, false);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Sprint error: {}", e.getMessage());
            return false;
        }
    }
}