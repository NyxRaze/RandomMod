// src/main/java/art/ameliah/fabric/autosprintfix/AutoSprintFix.java
package art.ameliah.fabric.autosprintfix;

import art.ameliah.fabric.autosprintfix.core.event.EventBus;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.KeyPressEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.ModuleManager;
import art.ameliah.fabric.autosprintfix.gui.GuiSettings;
import art.ameliah.fabric.autosprintfix.gui.ModuleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Main entry point for AutoSprintFix mod.
 * Initializes all systems and manages the mod lifecycle.
 */
public class AutoSprintFix implements ClientModInitializer {

	// Mod instance
	private static AutoSprintFix instance;

	// Logger instance
	private ModLogger logger;

	// Event bus instance
	private EventBus eventBus;

	// Module manager instance
	private ModuleManager moduleManager;

	// Minecraft client reference
	private Minecraft mc;

	/**
	 * Gets the mod instance.
	 * 
	 * @return The AutoSprintFix instance
	 */
	public static AutoSprintFix getInstance() {
		return instance;
	}

	@Override
	public void onInitializeClient() {
		instance = this;

		// Initialize logger
		logger = ModLogger.getInstance();
		logger.info("AutoSprintFix initializing...");

		// Get Minecraft instance
		mc = Minecraft.getInstance();

		// Initialize GUI settings (loads from file)
		GuiSettings.getInstance();

		// Initialize event bus
		eventBus = EventBus.getInstance();
		eventBus.register(this);

		// Initialize module manager
		moduleManager = ModuleManager.getInstance();
		moduleManager.initialize();

		logger.info("AutoSprintFix initialized successfully!");
	}

	/**
	 * Opens the module GUI screen.
	 * Called when the GUI keybind is pressed.
	 */
	public void openModuleScreen() {
		if (mc.screen == null) {
			mc.setScreen(new ModuleScreen());
		}
	}

	/**
	 * Handles the GUI opening keybind.
	 * Uses the keybind from GuiSettings.
	 * Only opens GUI when no other screen is open.
	 */
	@Listener
	public void onKeyPress(KeyPressEvent event) {
		// Only handle key press events (not release or repeat)
		if (event.getAction() != GLFW.GLFW_PRESS) {
			return;
		}

		// Only open GUI if no screen is currently open
		if (mc.screen != null) {
			return;
		}

		// Check for GUI open key (from settings)
		if (event.getKeyCode() == GuiSettings.getInstance().getGuiOpenKey()) {
			openModuleScreen();
		}
	}

	/**
	 * Gets the logger instance.
	 * 
	 * @return The mod logger
	 */
	public ModLogger getLogger() {
		return logger;
	}

	/**
	 * Gets the event bus instance.
	 * 
	 * @return The event bus
	 */
	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * Gets the module manager instance.
	 * 
	 * @return The module manager
	 */
	public ModuleManager getModuleManager() {
		return moduleManager;
	}
}