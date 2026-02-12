// src/main/java/art/ameliah/fabric/autosprintfix/core/module/ModuleManager.java
package art.ameliah.fabric.autosprintfix.core.module;

import art.ameliah.fabric.autosprintfix.config.ConfigManager;
import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.KeyPressEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all modules in the mod.
 * Handles module registration, keybind processing, and module lookup.
 * Supports automatic module discovery via @AutoRegister annotation.
 */
public class ModuleManager {

    // Singleton instance
    private static ModuleManager instance;

    // List of all registered modules
    private final List<Module> modules;

    // Map of module names to modules for quick lookup
    private final Map<String, Module> moduleMap;

    // Map of categories to modules
    private final Map<ModuleCategory, List<Module>> categoryMap;

    // Logger reference
    private final ModLogger logger;

    // Flag to track if initialized
    private boolean initialized = false;

    /**
     * Private constructor for singleton pattern.
     */
    private ModuleManager() {
        this.modules = new ArrayList<>();
        this.moduleMap = new HashMap<>();
        this.categoryMap = new HashMap<>();
        this.logger = ModLogger.getInstance();

        // Initialize category map with empty lists for all categories
        for (ModuleCategory category : ModuleCategory.values()) {
            categoryMap.put(category, new ArrayList<>());
        }

        // Register this manager as an event listener
        EventBus.getInstance().register(this);
    }

    /**
     * Gets the singleton instance of the module manager.
     * 
     * @return The ModuleManager instance
     */
    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    /**
     * Initializes and registers all modules.
     * Uses automatic discovery for modules with @AutoRegister annotation.
     */
    public void initialize() {
        if (initialized) {
            logger.warn("ModuleManager already initialized!");
            return;
        }

        logger.info("Initializing module manager...");

        // Auto-discover and register modules
        List<Module> discoveredModules = ModuleScanner.scanForModules();

        for (Module module : discoveredModules) {
            registerModule(module);
        }

        initialized = true;

        logger.info("Registered " + modules.size() + " module(s)");

        // Load saved configuration (keybinds, enabled states)
        ConfigManager.getInstance().load();
    }

    /**
     * Registers a module with the manager.
     * Can be used to manually register modules not using @AutoRegister.
     * 
     * @param module The module to register
     */
    public void registerModule(Module module) {
        if (module == null) {
            logger.warn("Attempted to register null module");
            return;
        }

        // Check for duplicate names
        if (moduleMap.containsKey(module.getName().toLowerCase())) {
            logger.warn("Module with name '" + module.getName() + "' already registered");
            return;
        }

        // Add to collections
        modules.add(module);
        moduleMap.put(module.getName().toLowerCase(), module);

        // Add to category map
        List<Module> categoryList = categoryMap.get(module.getCategory());
        if (categoryList != null) {
            categoryList.add(module);
        }

        logger.debug("Registered module: " + module.getName());
    }

    /**
     * Unregisters a module.
     * 
     * @param module The module to unregister
     */
    public void unregisterModule(Module module) {
        if (module == null)
            return;

        // Disable if enabled
        if (module.isEnabled()) {
            module.setEnabled(false);
        }

        // Remove from collections
        modules.remove(module);
        moduleMap.remove(module.getName().toLowerCase());

        List<Module> categoryList = categoryMap.get(module.getCategory());
        if (categoryList != null) {
            categoryList.remove(module);
        }

        logger.debug("Unregistered module: " + module.getName());
    }

    /**
     * Gets a module by name.
     * 
     * @param name The module name (case-insensitive)
     * @return The module, or null if not found
     */
    public Module getModule(String name) {
        return moduleMap.get(name.toLowerCase());
    }

    /**
     * Gets a module by class type.
     * 
     * @param clazz The module class
     * @param <T>   The module type
     * @return The module, or null if not found
     */
    @SuppressWarnings({ "unchecked", "null" })
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }

    /**
     * Gets all registered modules.
     * 
     * @return List of all modules
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    /**
     * Gets all modules in a category.
     * 
     * @param category The category
     * @return List of modules in the category
     */
    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> result = categoryMap.get(category);
        if (result == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(result);
    }

    /**
     * Gets all enabled modules.
     * 
     * @return List of enabled modules
     */
    public List<Module> getEnabledModules() {
        List<Module> enabled = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    /**
     * Resets all module keybinds to their defaults.
     */
    public void resetAllKeybinds() {
        for (Module module : modules) {
            module.resetKeyBind();
        }
        logger.info("All module keybinds reset to defaults");
    }

    /**
     * Handles key press events for module keybinds.
     * Only processes keybinds when no screen is open.
     * 
     * @param event The key press event
     */
    @Listener
    public void onKeyPress(KeyPressEvent event) {
        // Only handle key press events (not release or repeat)
        if (event.action != GLFW.GLFW_PRESS) {
            return;
        }

        // Don't process keybinds if any screen is open
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }

        // Check each module's keybind
        for (Module module : modules) {
            if (module.matchesKey(event.keyCode)) {
                module.toggle();
            }
        }
    }

    /**
     * Checks if the manager has been initialized.
     * 
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}