package art.ameliah.fabric.autosprintfix.core.module.settings;

import java.util.Random;

/**
 * A setting that stores a range with minimum and maximum values.
 * Useful for randomization, delays, distances, etc.
 */
public class RangeSetting extends Setting<double[]> {

    // Absolute bounds (the limits for min/max values)
    private final double absoluteMin;
    private final double absoluteMax;

    // Step increment
    private final double step;

    // Whether to display as integers
    private final boolean integer;

    // Random instance for randomization
    private static final Random random = new Random();

    /**
     * Creates a new range setting.
     * 
     * @param name        The setting name
     * @param description The setting description
     * @param defaultMin  The default minimum value
     * @param defaultMax  The default maximum value
     * @param absoluteMin The absolute minimum bound
     * @param absoluteMax The absolute maximum bound
     * @param step        The step increment
     * @param integer     Whether to treat values as integers
     */
    public RangeSetting(String name, String description, double defaultMin, double defaultMax,
            double absoluteMin, double absoluteMax, double step, boolean integer) {
        super(name, description, new double[] { defaultMin, defaultMax });
        this.absoluteMin = absoluteMin;
        this.absoluteMax = absoluteMax;
        this.step = step;
        this.integer = integer;

        // Initialize with clamped values
        this.value = new double[] {
                clamp(defaultMin, absoluteMin, absoluteMax),
                clamp(defaultMax, absoluteMin, absoluteMax)
        };
    }

    /**
     * Creates an integer range setting.
     * 
     * @param name        The setting name
     * @param description The setting description
     * @param defaultMin  The default minimum value
     * @param defaultMax  The default maximum value
     * @param absoluteMin The absolute minimum bound
     * @param absoluteMax The absolute maximum bound
     */
    public RangeSetting(String name, String description, int defaultMin, int defaultMax,
            int absoluteMin, int absoluteMax) {
        this(name, description, defaultMin, defaultMax, absoluteMin, absoluteMax, 1, true);
    }

    /**
     * Creates a decimal range setting.
     * 
     * @param name        The setting name
     * @param description The setting description
     * @param defaultMin  The default minimum value
     * @param defaultMax  The default maximum value
     * @param absoluteMin The absolute minimum bound
     * @param absoluteMax The absolute maximum bound
     * @param step        The step increment
     */
    public RangeSetting(String name, String description, double defaultMin, double defaultMax,
            double absoluteMin, double absoluteMax, double step) {
        this(name, description, defaultMin, defaultMax, absoluteMin, absoluteMax, step, false);
    }

    /**
     * Gets the minimum value of the range.
     * 
     * @return The minimum value
     */
    public double getMinValue() {
        return value[0];
    }

    /**
     * Gets the maximum value of the range.
     * 
     * @return The maximum value
     */
    public double getMaxValue() {
        return value[1];
    }

    /**
     * Gets the minimum value as an integer.
     * 
     * @return The minimum value rounded to int
     */
    public int getMinValueInt() {
        return (int) Math.round(value[0]);
    }

    /**
     * Gets the maximum value as an integer.
     * 
     * @return The maximum value rounded to int
     */
    public int getMaxValueInt() {
        return (int) Math.round(value[1]);
    }

    /**
     * Gets the minimum value as a float.
     * 
     * @return The minimum value as float
     */
    public float getMinValueFloat() {
        return (float) value[0];
    }

    /**
     * Gets the maximum value as a float.
     * 
     * @return The maximum value as float
     */
    public float getMaxValueFloat() {
        return (float) value[1];
    }

    /**
     * Gets a random value within the range (inclusive).
     * 
     * @return A random value between min and max
     */
    public double getRandomValue() {
        double min = value[0];
        double max = value[1];

        if (min >= max) {
            return min;
        }

        double randomValue = min + (max - min) * random.nextDouble();

        // Snap to step
        if (step > 0) {
            randomValue = Math.round(randomValue / step) * step;
        }

        return randomValue;
    }

    /**
     * Gets a random integer value within the range (inclusive).
     * 
     * @return A random integer between min and max
     */
    public int getRandomValueInt() {
        int min = getMinValueInt();
        int max = getMaxValueInt();

        if (min >= max) {
            return min;
        }

        return min + random.nextInt(max - min + 1);
    }

    /**
     * Gets a random float value within the range.
     * 
     * @return A random float between min and max
     */
    public float getRandomValueFloat() {
        return (float) getRandomValue();
    }

    /**
     * Gets a random long value within the range (useful for delays in ms).
     * 
     * @return A random long between min and max
     */
    public long getRandomValueLong() {
        return Math.round(getRandomValue());
    }

    /**
     * Sets the minimum value.
     * 
     * @param min The new minimum value
     */
    public void setMinValue(double min) {
        min = clamp(min, absoluteMin, absoluteMax);
        min = snapToStep(min);

        // Ensure min doesn't exceed max
        if (min > value[1]) {
            min = value[1];
        }

        if (value[0] != min) {
            value[0] = min;
            notifyChange();
        }
    }

    /**
     * Sets the maximum value.
     * 
     * @param max The new maximum value
     */
    public void setMaxValue(double max) {
        max = clamp(max, absoluteMin, absoluteMax);
        max = snapToStep(max);

        // Ensure max doesn't go below min
        if (max < value[0]) {
            max = value[0];
        }

        if (value[1] != max) {
            value[1] = max;
            notifyChange();
        }
    }

    /**
     * Sets both min and max values.
     * 
     * @param min The new minimum value
     * @param max The new maximum value
     */
    public void setRange(double min, double max) {
        min = clamp(min, absoluteMin, absoluteMax);
        max = clamp(max, absoluteMin, absoluteMax);
        min = snapToStep(min);
        max = snapToStep(max);

        // Ensure min <= max
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        if (value[0] != min || value[1] != max) {
            value[0] = min;
            value[1] = max;
            notifyChange();
        }
    }

    /**
     * Sets the minimum value from a progress (0-1).
     * 
     * @param progress The progress value
     */
    public void setMinFromProgress(double progress) {
        progress = clamp(progress, 0, 1);
        double newMin = absoluteMin + (absoluteMax - absoluteMin) * progress;
        setMinValue(newMin);
    }

    /**
     * Sets the maximum value from a progress (0-1).
     * 
     * @param progress The progress value
     */
    public void setMaxFromProgress(double progress) {
        progress = clamp(progress, 0, 1);
        double newMax = absoluteMin + (absoluteMax - absoluteMin) * progress;
        setMaxValue(newMax);
    }

    /**
     * Gets the progress (0-1) of the minimum value.
     * 
     * @return The progress of min value
     */
    public double getMinProgress() {
        if (absoluteMax == absoluteMin)
            return 0;
        return (value[0] - absoluteMin) / (absoluteMax - absoluteMin);
    }

    /**
     * Gets the progress (0-1) of the maximum value.
     * 
     * @return The progress of max value
     */
    public double getMaxProgress() {
        if (absoluteMax == absoluteMin)
            return 1;
        return (value[1] - absoluteMin) / (absoluteMax - absoluteMin);
    }

    /**
     * Gets the range span (max - min).
     * 
     * @return The difference between max and min
     */
    public double getSpan() {
        return value[1] - value[0];
    }

    /**
     * Gets the midpoint of the range.
     * 
     * @return The middle value of the range
     */
    public double getMidpoint() {
        return (value[0] + value[1]) / 2.0;
    }

    /**
     * Checks if a value is within the range.
     * 
     * @param val The value to check
     * @return true if val is between min and max (inclusive)
     */
    public boolean isInRange(double val) {
        return val >= value[0] && val <= value[1];
    }

    /**
     * Notifies that the value changed and saves config.
     */
    private void notifyChange() {
        if (module != null) {
            art.ameliah.fabric.autosprintfix.config.ConfigManager.getInstance().saveModule(module);
        }
    }

    /**
     * Clamps a value between min and max.
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Snaps a value to the step increment.
     */
    private double snapToStep(double val) {
        if (step > 0) {
            return Math.round(val / step) * step;
        }
        return val;
    }

    // Getters for bounds

    public double getAbsoluteMin() {
        return absoluteMin;
    }

    public double getAbsoluteMax() {
        return absoluteMax;
    }

    public double getStep() {
        return step;
    }

    public boolean isInteger() {
        return integer;
    }

    /**
     * Gets the display string for the range.
     * 
     * @return Formatted string like "5 - 10" or "1.5 - 3.0"
     */
    public String getDisplayValue() {
        if (integer) {
            return getMinValueInt() + " - " + getMaxValueInt();
        } else {
            return String.format("%.2f - %.2f", value[0], value[1]);
        }
    }

    @Override
    public String getType() {
        return "range";
    }

    @Override
    public String serialize() {
        return value[0] + "," + value[1];
    }

    @Override
    public void deserialize(String serialized) {
        try {
            String[] parts = serialized.split(",");
            if (parts.length == 2) {
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);

                min = clamp(min, absoluteMin, absoluteMax);
                max = clamp(max, absoluteMin, absoluteMax);

                if (min > max) {
                    double temp = min;
                    min = max;
                    max = temp;
                }

                value[0] = min;
                value[1] = max;
            }
        } catch (Exception e) {
            value = new double[] { defaultValue[0], defaultValue[1] };
        }
    }
}