// src/main/java/art/ameliah/fabric/autosprintfix/gui/GuiSettings.java
package art.ameliah.fabric.autosprintfix.gui;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Stores and manages GUI settings including keybinds and colors.
 * Settings are persisted to config/AutoSprintFix/gui.json.
 */
public class GuiSettings {

    // Singleton instance
    private static GuiSettings instance;

    // Logger reference
    private final ModLogger logger;

    // Config file
    private final File configFile;

    // Gson for serialization
    private final Gson gson;

    // === Keybind Settings ===
    private int guiOpenKey = GLFW.GLFW_KEY_RIGHT_SHIFT;

    // === Color Theme ===
    private ColorTheme currentTheme = ColorTheme.PURPLE;

    // === Custom Colors (when theme is CUSTOM) ===
    private int customAccent = 0xFFB066FF;
    private int customAccentDark = 0xFF8844DD;
    private int customBackground = 0xFF1A1A1A;
    private int customSidebar = 0xFF141414;
    private int customToggleOn = 0xFF66BB6A;

    /**
     * Available color themes.
     */
    public enum ColorTheme {
        PURPLE("Purple", 0xFFB066FF, 0xFF8844DD),
        BLUE("Blue", 0xFF4A9EFF, 0xFF2D7DD2),
        RED("Red", 0xFFFF6B6B, 0xFFCC4444),
        GREEN("Green", 0xFF66FF66, 0xFF44CC44),
        ORANGE("Orange", 0xFFFFAA44, 0xFFDD8822),
        PINK("Pink", 0xFFFF66B2, 0xFFDD4499),
        CYAN("Cyan", 0xFF44FFFF, 0xFF22CCCC),
        CUSTOM("Custom", 0xFFB066FF, 0xFF8844DD);

        private final String displayName;
        private final int accent;
        private final int accentDark;

        ColorTheme(String displayName, int accent, int accentDark) {
            this.displayName = displayName;
            this.accent = accent;
            this.accentDark = accentDark;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getAccent() {
            return accent;
        }

        public int getAccentDark() {
            return accentDark;
        }
    }

    /**
     * Private constructor for singleton pattern.
     */
    private GuiSettings() {
        this.logger = ModLogger.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Get config directory: config/AutoSprintFix/
        File gameDir = new File(System.getProperty("user.dir"));
        File baseConfigDir = new File(gameDir, "config");
        File modConfigDir = new File(baseConfigDir, "AutoSprintFix");

        // Ensure directory exists
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }

        this.configFile = new File(modConfigDir, "gui.json");

        // Load settings
        load();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return The GuiSettings instance
     */
    public static GuiSettings getInstance() {
        if (instance == null) {
            instance = new GuiSettings();
        }
        return instance;
    }

    /**
     * Loads settings from file.
     */
    public void load() {
        if (!configFile.exists()) {
            logger.debug("GUI settings file not found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            @SuppressWarnings("null")
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            // Gson can return null for empty/invalid JSON
            if (json == null || json.isEmpty() || json.isJsonNull()) {
                logger.warn("GUI settings file is empty or invalid");
                return;
            }

            if (json.has("guiOpenKey")) {
                guiOpenKey = json.get("guiOpenKey").getAsInt();
            }

            if (json.has("colorTheme")) {
                try {
                    currentTheme = ColorTheme.valueOf(json.get("colorTheme").getAsString());
                } catch (IllegalArgumentException e) {
                    currentTheme = ColorTheme.PURPLE;
                }
            }

            if (json.has("customAccent")) {
                customAccent = json.get("customAccent").getAsInt();
            }
            if (json.has("customAccentDark")) {
                customAccentDark = json.get("customAccentDark").getAsInt();
            }
            if (json.has("customBackground")) {
                customBackground = json.get("customBackground").getAsInt();
            }
            if (json.has("customSidebar")) {
                customSidebar = json.get("customSidebar").getAsInt();
            }
            if (json.has("customToggleOn")) {
                customToggleOn = json.get("customToggleOn").getAsInt();
            }

            logger.debug("GUI settings loaded successfully");

        } catch (IOException e) {
            logger.error("Failed to load GUI settings", e);
        }
    }

    /**
     * Saves settings to file.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject json = new JsonObject();

            json.addProperty("guiOpenKey", guiOpenKey);
            json.addProperty("colorTheme", currentTheme.name());
            json.addProperty("customAccent", customAccent);
            json.addProperty("customAccentDark", customAccentDark);
            json.addProperty("customBackground", customBackground);
            json.addProperty("customSidebar", customSidebar);
            json.addProperty("customToggleOn", customToggleOn);

            gson.toJson(json, writer);

            logger.debug("GUI settings saved successfully");

        } catch (IOException e) {
            logger.error("Failed to save GUI settings", e);
        }
    }

    public int getGuiOpenKey() {
        return guiOpenKey;
    }

    public void setGuiOpenKey(int key) {
        this.guiOpenKey = key;
        save();
    }

    public String getGuiOpenKeyName() {
        return Module.getKeyName(guiOpenKey);
    }

    public ColorTheme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(ColorTheme theme) {
        this.currentTheme = theme;
        save();
    }

    public void nextTheme() {
        ColorTheme[] themes = ColorTheme.values();
        int nextIndex = (currentTheme.ordinal() + 1) % themes.length;
        currentTheme = themes[nextIndex];
        save();
    }

    public void previousTheme() {
        ColorTheme[] themes = ColorTheme.values();
        int prevIndex = (currentTheme.ordinal() - 1 + themes.length) % themes.length;
        currentTheme = themes[prevIndex];
        save();
    }

    public int getAccent() {
        return currentTheme == ColorTheme.CUSTOM ? customAccent : currentTheme.getAccent();
    }

    public int getAccentDark() {
        return currentTheme == ColorTheme.CUSTOM ? customAccentDark : currentTheme.getAccentDark();
    }

    public int getBackground() {
        return currentTheme == ColorTheme.CUSTOM ? customBackground : 0xFF1A1A1A;
    }

    public int getSidebar() {
        return currentTheme == ColorTheme.CUSTOM ? customSidebar : 0xFF141414;
    }

    public int getToggleOn() {
        return currentTheme == ColorTheme.CUSTOM ? customToggleOn : 0xFF66BB6A;
    }

    public void setCustomAccent(int color) {
        this.customAccent = color;
        save();
    }

    public void setCustomAccentDark(int color) {
        this.customAccentDark = color;
        save();
    }

    public void setCustomBackground(int color) {
        this.customBackground = color;
        save();
    }

    public void setCustomSidebar(int color) {
        this.customSidebar = color;
        save();
    }

    public void setCustomToggleOn(int color) {
        this.customToggleOn = color;
        save();
    }

    public int getCustomAccent() {
        return customAccent;
    }

    public int getCustomAccentDark() {
        return customAccentDark;
    }

    public int getCustomBackground() {
        return customBackground;
    }

    public int getCustomSidebar() {
        return customSidebar;
    }

    public int getCustomToggleOn() {
        return customToggleOn;
    }
}