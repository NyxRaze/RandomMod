package art.ameliah.fabric.autosprintfix.core.module.settings;

/**
 * A boolean toggle setting.
 */
public class BooleanSetting extends Setting<Boolean> {

    /**
     * Creates a new boolean setting.
     * 
     * @param name         The setting name
     * @param description  The setting description
     * @param defaultValue The default value
     */
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    /**
     * Toggles the value.
     */
    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public String getType() {
        return "boolean";
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }

    @Override
    public void deserialize(String serialized) {
        try {
            value = Boolean.parseBoolean(serialized);
        } catch (Exception e) {
            value = defaultValue;
        }
    }
}