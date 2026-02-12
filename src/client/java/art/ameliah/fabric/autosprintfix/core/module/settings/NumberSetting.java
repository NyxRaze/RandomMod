package art.ameliah.fabric.autosprintfix.core.module.settings;

/**
 * A number setting with min/max bounds.
 * Can be used for sliders in the GUI.
 */
public class NumberSetting extends Setting<Double> {

    // Minimum value
    private final double min;

    // Maximum value
    private final double max;

    // Step increment
    private final double step;

    // Whether to display as integer
    private final boolean integer;

    /**
     * Creates a new number setting.
     * 
     * @param name         The setting name
     * @param description  The setting description
     * @param defaultValue The default value
     * @param min          The minimum value
     * @param max          The maximum value
     * @param step         The step increment
     * @param integer      Whether to treat as integer
     */
    public NumberSetting(String name, String description, double defaultValue,
            double min, double max, double step, boolean integer) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.integer = integer;
    }

    /**
     * Creates an integer setting.
     */
    public NumberSetting(String name, String description, int defaultValue, int min, int max) {
        this(name, description, defaultValue, min, max, 1, true);
    }

    /**
     * Creates a decimal setting.
     */
    public NumberSetting(String name, String description, double defaultValue,
            double min, double max, double step) {
        this(name, description, defaultValue, min, max, step, false);
    }

    @Override
    public void setValue(Double value) {
        // Clamp to bounds
        value = Math.max(min, Math.min(max, value));

        // Snap to step
        if (step > 0) {
            value = Math.round(value / step) * step;
        }

        super.setValue(value);
    }

    /**
     * Gets the value as an integer.
     * 
     * @return The integer value
     */
    public int getIntValue() {
        return (int) Math.round(value);
    }

    /**
     * Gets the value as a float.
     * 
     * @return The float value
     */
    public float getFloatValue() {
        return value.floatValue();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public boolean isInteger() {
        return integer;
    }

    /**
     * Gets the progress (0-1) of the current value within the range.
     * 
     * @return The progress
     */
    public double getProgress() {
        return (value - min) / (max - min);
    }

    /**
     * Sets the value from a progress (0-1).
     * 
     * @param progress The progress
     */
    public void setFromProgress(double progress) {
        progress = Math.max(0, Math.min(1, progress));
        setValue(min + (max - min) * progress);
    }

    @Override
    public String getType() {
        return "number";
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }

    @Override
    public void deserialize(String serialized) {
        try {
            value = Double.parseDouble(serialized);
            // Clamp to bounds
            value = Math.max(min, Math.min(max, value));
        } catch (Exception e) {
            value = defaultValue;
        }
    }

    /**
     * Gets the display string for the current value.
     * 
     * @return The formatted value
     */
    public String getDisplayValue() {
        if (integer) {
            return String.valueOf(getIntValue());
        } else {
            return String.format("%.2f", value);
        }
    }
}