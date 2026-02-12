package art.ameliah.fabric.autosprintfix.config;

/**
 * Configuration data for a module.
 * Used for serialization/deserialization of module settings.
 */
public class ModuleConfig {

    // Whether the module is enabled
    public boolean enabled = false;

    // The module's keybind
    public int keyBind = -1;

    /**
     * Default constructor.
     */
    public ModuleConfig() {
    }

    /**
     * Creates a module config with specified values.
     * 
     * @param enabled Whether the module is enabled
     * @param keyBind The keybind
     */
    public ModuleConfig(boolean enabled, int keyBind) {
        this.enabled = enabled;
        this.keyBind = keyBind;
    }
}