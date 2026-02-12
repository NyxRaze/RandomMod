// src/main/java/art/ameliah/fabric/autosprintfix/config/ConfigManager.java
package art.ameliah.fabric.autosprintfix.config;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleManager;
import art.ameliah.fabric.autosprintfix.core.module.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages configuration loading and saving for the mod.
 * Stores module states, keybinds, and settings in JSON format.
 * All configs are saved to config/AutoSprintFix/ folder.
 */
public class ConfigManager {

    // Singleton instance
    private static ConfigManager instance;

    // Config directory
    private final File configDir;

    // Modules config file
    private final File modulesFile;

    // Gson instance for JSON serialization
    private final Gson gson;

    // Logger reference
    private final ModLogger logger;

    /**
     * Private constructor for singleton pattern.
     */
    private ConfigManager() {
        // Get config directory: config/AutoSprintFix/
        File gameDir = new File(System.getProperty("user.dir"));
        File baseConfigDir = new File(gameDir, "config");
        this.configDir = new File(baseConfigDir, "AutoSprintFix");

        // Ensure config directory exists
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Create config file references
        this.modulesFile = new File(configDir, "modules.json");

        // Initialize Gson with pretty printing
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        this.logger = ModLogger.getInstance();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return The ConfigManager instance
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Gets the config directory.
     * 
     * @return The config directory
     */
    public File getConfigDir() {
        return configDir;
    }

    /**
     * Loads the configuration from file.
     * Should be called after modules are registered.
     */
    public void load() {
        if (!modulesFile.exists()) {
            logger.info("Module config file not found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(modulesFile)) {
            @SuppressWarnings("null")
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            // Gson can return null for empty/invalid JSON
            if (json == null || json.isEmpty() || json.isJsonNull()) {
                logger.warn("GUI settings file is empty or invalid");
                return;
            }

            // Load module configurations
            if (json.has("modules")) {
                JsonObject modulesJson = json.getAsJsonObject("modules");

                for (Module module : ModuleManager.getInstance().getModules()) {
                    if (modulesJson.has(module.getName())) {
                        JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());
                        loadModuleConfig(module, moduleJson);
                    }
                }
            }

            logger.info("Module configuration loaded successfully");

        } catch (IOException e) {
            logger.error("Failed to load module configuration", e);
        } catch (Exception e) {
            logger.error("Error parsing module configuration", e);
        }
    }

    /**
     * Loads configuration for a single module.
     */
    private void loadModuleConfig(Module module, JsonObject json) {
        // Load enabled state
        if (json.has("enabled")) {
            boolean enabled = json.get("enabled").getAsBoolean();
            if (enabled) {
                module.setEnabled(true);
            }
        }

        // Load keybind
        if (json.has("keybind")) {
            int keybind = json.get("keybind").getAsInt();
            module.setKeyBind(keybind);
        }

        // Load settings
        if (json.has("settings")) {
            JsonObject settingsJson = json.getAsJsonObject("settings");

            for (Setting<?> setting : module.getSettings()) {
                if (settingsJson.has(setting.getName())) {
                    String serialized = settingsJson.get(setting.getName()).getAsString();
                    setting.deserialize(serialized);
                }
            }
        }
    }

    /**
     * Saves the current configuration to file.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(modulesFile)) {
            JsonObject json = new JsonObject();

            // Save module configurations
            JsonObject modulesJson = new JsonObject();

            for (Module module : ModuleManager.getInstance().getModules()) {
                modulesJson.add(module.getName(), createModuleConfig(module));
            }

            json.add("modules", modulesJson);

            // Write to file
            gson.toJson(json, writer);

            logger.debug("Module configuration saved successfully");

        } catch (IOException e) {
            logger.error("Failed to save module configuration", e);
        }
    }

    /**
     * Creates JSON config for a single module.
     */
    private JsonObject createModuleConfig(Module module) {
        JsonObject json = new JsonObject();

        json.addProperty("enabled", module.isEnabled());
        json.addProperty("keybind", module.getKeyBind());

        // Save settings
        if (!module.getSettings().isEmpty()) {
            JsonObject settingsJson = new JsonObject();

            for (Setting<?> setting : module.getSettings()) {
                settingsJson.addProperty(setting.getName(), setting.serialize());
            }

            json.add("settings", settingsJson);
        }

        return json;
    }

    /**
     * Saves a single module's configuration.
     * 
     * @param module The module to save
     */
    public void saveModule(Module module) {
        save();
    }
}