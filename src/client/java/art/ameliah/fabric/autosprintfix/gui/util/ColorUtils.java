package art.ameliah.fabric.autosprintfix.gui.util;

import art.ameliah.fabric.autosprintfix.gui.GuiSettings;

/**
 * Utility class for color manipulation and constants.
 * Colors are in ARGB format.
 * Theme colors are fetched from GuiSettings.
 */
public class ColorUtils {

    // === Static Base Colors (don't change with theme) ===
    public static final int BACKGROUND_DARK = 0xFF0D0D0D;
    public static final int BACKGROUND_LIGHTER = 0xFF3A3A3A;
    public static final int CARD_BG = 0xFF1E1E1E;
    public static final int CARD_BG_HOVER = 0xFF282828;
    public static final int CARD_BORDER = 0xFF333333;
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF888888;
    public static final int TEXT_TERTIARY = 0xFF555555;
    public static final int TOGGLE_OFF = 0xFF444444;
    public static final int TOGGLE_KNOB = 0xFFFFFFFF;

    // Category colors (static)
    public static final int CATEGORY_COMBAT = 0xFFFF5555;
    public static final int CATEGORY_MOVEMENT = 0xFF55FF55;
    public static final int CATEGORY_RENDER = 0xFF55FFFF;
    public static final int CATEGORY_PLAYER = 0xFFFFAA00;
    public static final int CATEGORY_UTILITY = 0xFFAA55FF;
    public static final int CATEGORY_MISC = 0xFFAAAAAA;

    // === Dynamic Colors (from GuiSettings) ===

    public static int getAccent() {
        return GuiSettings.getInstance().getAccent();
    }

    public static int getAccentDark() {
        return GuiSettings.getInstance().getAccentDark();
    }

    public static int getBackground() {
        return GuiSettings.getInstance().getBackground();
    }

    public static int getBackgroundLight() {
        return adjustBrightness(getBackground(), 1.3f);
    }

    public static int getSidebar() {
        return GuiSettings.getInstance().getSidebar();
    }

    public static int getSidebarSelected() {
        return adjustBrightness(getSidebar(), 1.5f);
    }

    public static int getToggleOn() {
        return GuiSettings.getInstance().getToggleOn();
    }

    // === Compatibility aliases ===

    public static final int BACKGROUND = 0xFF1A1A1A; // Default fallback
    public static final int BACKGROUND_LIGHT = 0xFF2A2A2A;
    public static final int SIDEBAR_BG = 0xFF141414;
    public static final int SIDEBAR_SELECTED = 0xFF252525;
    public static final int ACCENT = 0xFFB066FF;
    public static final int ACCENT_DARK = 0xFF8844DD;
    public static final int ACCENT_LIGHT = 0xFFCC99FF;
    public static final int TOGGLE_ON = 0xFF66BB6A;

    /**
     * Extracts the alpha component from an ARGB color.
     * 
     * @param color The ARGB color
     * @return The alpha value (0-255)
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Extracts the red component from an ARGB color.
     * 
     * @param color The ARGB color
     * @return The red value (0-255)
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from an ARGB color.
     * 
     * @param color The ARGB color
     * @return The green value (0-255)
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from an ARGB color.
     * 
     * @param color The ARGB color
     * @return The blue value (0-255)
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    /**
     * Creates an ARGB color from components.
     * 
     * @param red   Red (0-255)
     * @param green Green (0-255)
     * @param blue  Blue (0-255)
     * @param alpha Alpha (0-255)
     * @return The ARGB color
     */
    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Interpolates between two colors.
     * 
     * @param color1   The first color
     * @param color2   The second color
     * @param progress The interpolation progress (0-1)
     * @return The interpolated color
     */
    public static int lerp(int color1, int color2, float progress) {
        progress = Math.max(0, Math.min(1, progress));

        int a = (int) (getAlpha(color1) + (getAlpha(color2) - getAlpha(color1)) * progress);
        int r = (int) (getRed(color1) + (getRed(color2) - getRed(color1)) * progress);
        int g = (int) (getGreen(color1) + (getGreen(color2) - getGreen(color1)) * progress);
        int b = (int) (getBlue(color1) + (getBlue(color2) - getBlue(color1)) * progress);

        return rgba(r, g, b, a);
    }

    /**
     * Adjusts the brightness of a color.
     * 
     * @param color  The color to adjust
     * @param factor Brightness factor (>1 = brighter, <1 = darker)
     * @return The adjusted color
     */
    public static int adjustBrightness(int color, float factor) {
        int a = getAlpha(color);
        int r = Math.min(255, (int) (getRed(color) * factor));
        int g = Math.min(255, (int) (getGreen(color) * factor));
        int b = Math.min(255, (int) (getBlue(color) * factor));

        return rgba(r, g, b, a);
    }

    /**
     * Creates a color with modified alpha.
     * 
     * @param color The base color
     * @param alpha The new alpha value (0-255)
     * @return The color with new alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /**
     * Converts a hex string to ARGB color.
     * 
     * @param hex Hex string (e.g., "#FF0000" or "FF0000")
     * @return ARGB color with full alpha
     */
    public static int fromHex(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            int rgb = Integer.parseInt(hex, 16);
            return 0xFF000000 | rgb;
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF; // Default to white on error
        }
    }

    /**
     * Converts an ARGB color to hex string.
     * 
     * @param color ARGB color
     * @return Hex string (without alpha, e.g., "FF0000")
     */
    public static String toHex(int color) {
        return String.format("%06X", color & 0xFFFFFF);
    }
}