package art.ameliah.fabric.autosprintfix.core.module;

import art.ameliah.fabric.autosprintfix.config.ConfigManager;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.settings.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all modules in the mod.
 * Modules are features that can be toggled on/off and may have settings.
 */
public abstract class Module {

    // Module name
    private final String name;

    // Module description
    private final String description;

    // Module category
    private final ModuleCategory category;

    // Whether the module is enabled
    private boolean enabled;

    // Keybind for toggling (GLFW key code, -1 for none)
    private int keyBind;

    // Default keybind (for reset functionality)
    private final int defaultKeyBind;

    // Module settings
    private final List<Setting<?>> settings;

    // Reference to the Minecraft client
    protected final Minecraft mc;

    // Reference to the logger
    protected final ModLogger logger;

    // Reference to the event bus
    protected final EventBus eventBus;

    /**
     * Creates a new module.
     * 
     * @param name           The module name
     * @param description    The module description
     * @param category       The module category
     * @param defaultKeyBind The default keybind (GLFW key code, -1 for none)
     */
    public Module(String name, String description, ModuleCategory category, int defaultKeyBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = defaultKeyBind;
        this.defaultKeyBind = defaultKeyBind;
        this.enabled = false;
        this.settings = new ArrayList<>();
        this.mc = Minecraft.getInstance();
        this.logger = ModLogger.getInstance();
        this.eventBus = EventBus.getInstance();
    }

    /**
     * Creates a new module with no default keybind.
     * 
     * @param name        The module name
     * @param description The module description
     * @param category    The module category
     */
    public Module(String name, String description, ModuleCategory category) {
        this(name, description, category, -1);
    }

    /**
     * Called when the module is enabled.
     * Override to add enable logic.
     */
    protected void onEnable() {
        eventBus.register(this);
    }

    /**
     * Called when the module is disabled.
     * Override to add disable logic.
     */
    protected void onDisable() {
        eventBus.unregister(this);
    }

    /**
     * Toggles the module on/off.
     */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Sets the enabled state of the module.
     * 
     * @param enabled The new enabled state
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        ModuleToggleEvent event = new ModuleToggleEvent(this, enabled);
        eventBus.post(event);

        if (event.isCancelled()) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
            logger.info("Module enabled: " + name);
        } else {
            onDisable();
            logger.info("Module disabled: " + name);
        }

        ConfigManager.getInstance().saveModule(this);
    }

    /**
     * Adds a setting to this module.
     * Returns the same setting type for method chaining.
     * 
     * @param setting The setting to add
     * @param <T>     The setting type
     * @return The added setting
     */
    @SuppressWarnings("null")
    protected <T extends Setting<?>> T addSetting(T setting) {
        setting.setModule(this);
        settings.add(setting);
        return setting;
    }

    /**
     * Gets all settings for this module.
     * 
     * @return List of settings
     */
    public List<Setting<?>> getSettings() {
        return settings;
    }

    /**
     * Gets a setting by name.
     * 
     * @param name The setting name
     * @return The setting, or null if not found
     */
    public Setting<?> getSetting(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    /**
     * Checks if a key matches this module's keybind.
     * 
     * @param keyCode The pressed key code
     * @return true if the key matches the keybind
     */
    public boolean matchesKey(int keyCode) {
        return keyBind != -1 && keyBind == keyCode;
    }

    /**
     * Resets the keybind to the default value.
     */
    public void resetKeyBind() {
        setKeyBind(defaultKeyBind);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
        ConfigManager.getInstance().saveModule(this);
        logger.debug("Keybind for " + name + " set to: " + getKeyBindName());
    }

    public int getDefaultKeyBind() {
        return defaultKeyBind;
    }

    /**
     * Gets the display name for the current keybind.
     * 
     * @return The keybind name or "None"
     */
    public String getKeyBindName() {
        return getKeyName(keyBind);
    }

    /**
     * Converts a GLFW key code to a display name.
     * 
     * @param keyCode The GLFW key code
     * @return The display name
     */
    public static String getKeyName(int keyCode) {
        if (keyCode == -1) {
            return "None";
        }

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null) {
            return name.toUpperCase();
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "R-WIN";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "L-WIN";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BKSP";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUM_LOCK";
            case GLFW.GLFW_KEY_KP_0 -> "NP0";
            case GLFW.GLFW_KEY_KP_1 -> "NP1";
            case GLFW.GLFW_KEY_KP_2 -> "NP2";
            case GLFW.GLFW_KEY_KP_3 -> "NP3";
            case GLFW.GLFW_KEY_KP_4 -> "NP4";
            case GLFW.GLFW_KEY_KP_5 -> "NP5";
            case GLFW.GLFW_KEY_KP_6 -> "NP6";
            case GLFW.GLFW_KEY_KP_7 -> "NP7";
            case GLFW.GLFW_KEY_KP_8 -> "NP8";
            case GLFW.GLFW_KEY_KP_9 -> "NP9";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "NP.";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "NP/";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "NP*";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "NP-";
            case GLFW.GLFW_KEY_KP_ADD -> "NP+";
            case GLFW.GLFW_KEY_KP_ENTER -> "NP_ENT";
            case GLFW.GLFW_KEY_KP_EQUAL -> "NP=";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRT_SC";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLL_LK";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_MENU -> "MENU";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            default -> "KEY_" + keyCode;
        };
    }
}