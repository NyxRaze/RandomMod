package art.ameliah.fabric.autosprintfix.core.module.settings;

import art.ameliah.fabric.autosprintfix.config.ConfigManager;
import art.ameliah.fabric.autosprintfix.core.module.Module;

import java.util.function.Consumer;

/**
 * Base class for module settings.
 * 
 * @param <T> The type of value this setting holds
 */
public abstract class Setting<T> {

    // Setting name
    private final String name;

    // Setting description
    private final String description;

    // Current value
    protected T value;

    // Default value
    protected final T defaultValue;

    // Parent module
    protected Module module;

    // Change listener
    private Consumer<T> onChange;

    /**
     * Creates a new setting.
     * 
     * @param name         The setting name
     * @param description  The setting description
     * @param defaultValue The default value
     */
    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    /**
     * Gets the setting name.
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the setting description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the current value.
     * 
     * @return The value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value The new value
     */
    public void setValue(T value) {
        if (this.value != value) {
            this.value = value;

            // Notify listener
            if (onChange != null) {
                onChange.accept(value);
            }

            // Save config
            if (module != null) {
                ConfigManager.getInstance().saveModule(module);
            }
        }
    }

    /**
     * Gets the default value.
     * 
     * @return The default value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Resets to default value.
     */
    public void reset() {
        setValue(defaultValue);
    }

    /**
     * Sets the parent module.
     * 
     * @param module The parent module
     */
    public void setModule(Module module) {
        this.module = module;
    }

    /**
     * Gets the parent module.
     * 
     * @return The module
     */
    public Module getModule() {
        return module;
    }

    /**
     * Sets a change listener.
     * 
     * @param onChange The listener
     * @return This setting for chaining
     */
    public Setting<T> onChange(Consumer<T> onChange) {
        this.onChange = onChange;
        return this;
    }

    /**
     * Gets the setting type identifier for serialization.
     * 
     * @return The type identifier
     */
    public abstract String getType();

    /**
     * Serializes the value to a string.
     * 
     * @return The serialized value
     */
    public abstract String serialize();

    /**
     * Deserializes the value from a string.
     * 
     * @param serialized The serialized value
     */
    public abstract void deserialize(String serialized);
}