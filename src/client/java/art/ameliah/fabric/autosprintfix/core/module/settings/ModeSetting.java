package art.ameliah.fabric.autosprintfix.core.module.settings;

import java.util.Arrays;
import java.util.List;

/**
 * A setting that cycles through predefined modes/options.
 */
public class ModeSetting extends Setting<String> {

    // Available modes
    private final List<String> modes;

    /**
     * Creates a new mode setting.
     * 
     * @param name        The setting name
     * @param description The setting description
     * @param defaultMode The default mode
     * @param modes       The available modes
     */
    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description, defaultMode);
        this.modes = Arrays.asList(modes);

        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException("Default mode must be in modes list");
        }
    }

    /**
     * Creates a new mode setting with list.
     */
    public ModeSetting(String name, String description, String defaultMode, List<String> modes) {
        super(name, description, defaultMode);
        this.modes = modes;

        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException("Default mode must be in modes list");
        }
    }

    @Override
    public void setValue(String value) {
        if (modes.contains(value)) {
            super.setValue(value);
        }
    }

    /**
     * Cycles to the next mode.
     */
    public void cycle() {
        int index = modes.indexOf(value);
        int nextIndex = (index + 1) % modes.size();
        setValue(modes.get(nextIndex));
    }

    /**
     * Cycles to the previous mode.
     */
    public void cyclePrevious() {
        int index = modes.indexOf(value);
        int prevIndex = (index - 1 + modes.size()) % modes.size();
        setValue(modes.get(prevIndex));
    }

    /**
     * Gets all available modes.
     * 
     * @return The modes
     */
    public List<String> getModes() {
        return modes;
    }

    /**
     * Gets the current mode index.
     * 
     * @return The index
     */
    public int getIndex() {
        return modes.indexOf(value);
    }

    /**
     * Checks if the current mode matches.
     * 
     * @param mode The mode to check
     * @return true if matches
     */
    public boolean is(String mode) {
        return value.equalsIgnoreCase(mode);
    }

    @Override
    public String getType() {
        return "mode";
    }

    @Override
    public String serialize() {
        return value;
    }

    @Override
    public void deserialize(String serialized) {
        if (modes.contains(serialized)) {
            value = serialized;
        } else {
            value = defaultValue;
        }
    }
}