package art.ameliah.fabric.autosprintfix.core.module.settings;

/**
 * A color setting that stores an ARGB color value.
 */
public class ColorSetting extends Setting<Integer> {

    // Whether alpha is enabled
    private final boolean hasAlpha;

    /**
     * Creates a new color setting.
     * 
     * @param name         The setting name
     * @param description  The setting description
     * @param defaultColor The default color (ARGB)
     * @param hasAlpha     Whether to allow alpha editing
     */
    public ColorSetting(String name, String description, int defaultColor, boolean hasAlpha) {
        super(name, description, defaultColor);
        this.hasAlpha = hasAlpha;
    }

    /**
     * Creates a color setting without alpha.
     */
    public ColorSetting(String name, String description, int defaultColor) {
        this(name, description, defaultColor, false);
    }

    /**
     * Gets the red component (0-255).
     */
    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    /**
     * Gets the green component (0-255).
     */
    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    /**
     * Gets the blue component (0-255).
     */
    public int getBlue() {
        return value & 0xFF;
    }

    /**
     * Gets the alpha component (0-255).
     */
    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    /**
     * Sets color from RGB components.
     */
    public void setRGB(int r, int g, int b) {
        setValue(0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    /**
     * Sets color from RGBA components.
     */
    public void setRGBA(int r, int g, int b, int a) {
        setValue(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    /**
     * Gets the hex string representation.
     * 
     * @return Hex string (e.g., "FF0000")
     */
    public String getHex() {
        if (hasAlpha) {
            return String.format("%08X", value);
        } else {
            return String.format("%06X", value & 0xFFFFFF);
        }
    }

    @Override
    public String getType() {
        return "color";
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }

    @Override
    public void deserialize(String serialized) {
        try {
            value = Integer.parseInt(serialized);
        } catch (Exception e) {
            value = defaultValue;
        }
    }
}