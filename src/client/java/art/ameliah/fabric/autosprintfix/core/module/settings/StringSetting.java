package art.ameliah.fabric.autosprintfix.core.module.settings;

/**
 * A string-based setting.
 */
public class StringSetting extends Setting<String> {

    /**
     * Creates a new string setting.
     * 
     * @param name         The setting name
     * @param description  The setting description
     * @param defaultValue The default string value
     */
    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public String getType() {
        return "string";
    }

    @Override
    public String serialize() {
        return value == null ? "" : value;
    }

    @Override
    public void deserialize(String serialized) {
        if (serialized == null) {
            value = defaultValue;
        } else {
            value = serialized;
        }
    }
}
