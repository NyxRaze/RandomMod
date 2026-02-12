package art.ameliah.fabric.autosprintfix.core.module;

/**
 * Categories for organizing modules in the GUI.
 * Each category has a display name, icon, and color.
 */
public enum ModuleCategory {

    // Combat-related modules (sword icon)
    COMBAT("Combat", "\u2694", 0xFFFF5555),

    // Visual/render modules (eye icon)
    RENDER("Render", "\u25C9", 0xFF55FFFF),

    // Player-related modules (person icon)
    PLAYER("Player", "\u263A", 0xFFFFAA00),

    // Utility modules (gear icon)
    UTILITY("Utility", "\u2699", 0xFFAA55FF),

    // Miscellaneous modules (star icon)
    MISC("Misc", "\u2605", 0xFFAAAAAA);

    // Display name for the category
    private final String displayName;

    // Icon character for sidebar
    private final String icon;

    // Color for the category (ARGB)
    private final int color;

    ModuleCategory(String displayName, String icon, int color) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
    }

    /**
     * Gets the display name.
     * 
     * @return The category display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the icon character.
     * 
     * @return The icon character
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gets the category color.
     * 
     * @return The ARGB color value
     */
    public int getColor() {
        return color;
    }
}