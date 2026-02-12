package art.ameliah.fabric.autosprintfix.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Utility class for rendering operations.
 * Provides helper methods for drawing shapes and text.
 */
public class RenderUtils {

    // Reference to the Minecraft client
    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * Draws a filled rectangle.
     * 
     * @param graphics The graphics context
     * @param x1       Left edge
     * @param y1       Top edge
     * @param x2       Right edge
     * @param y2       Bottom edge
     * @param color    ARGB color
     */
    public static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    /**
     * Draws a filled rectangle with rounded corners (simplified version).
     * Uses overlapping rectangles to approximate rounded corners.
     * 
     * @param graphics The graphics context
     * @param x        Left edge
     * @param y        Top edge
     * @param width    Width
     * @param height   Height
     * @param radius   Corner radius
     * @param color    ARGB color
     */
    public static void fillRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        // Clamp radius to reasonable values
        radius = Math.min(radius, Math.min(width / 2, height / 2));

        if (radius <= 0) {
            fill(graphics, x, y, x + width, y + height, color);
            return;
        }

        // Main body (horizontal)
        fill(graphics, x + radius, y, x + width - radius, y + height, color);

        // Left strip
        fill(graphics, x, y + radius, x + radius, y + height - radius, color);

        // Right strip
        fill(graphics, x + width - radius, y + radius, x + width, y + height - radius, color);

        // Corners (small squares for now - could be improved with actual circles)
        // Top-left
        fill(graphics, x, y, x + radius, y + radius, color);
        // Top-right
        fill(graphics, x + width - radius, y, x + width, y + radius, color);
        // Bottom-left
        fill(graphics, x, y + height - radius, x + radius, y + height, color);
        // Bottom-right
        fill(graphics, x + width - radius, y + height - radius, x + width, y + height, color);
    }

    /**
     * Draws a rectangle outline.
     * 
     * @param graphics  The graphics context
     * @param x         Left edge
     * @param y         Top edge
     * @param width     Width
     * @param height    Height
     * @param color     ARGB color
     * @param thickness Line thickness
     */
    public static void drawRect(GuiGraphics graphics, int x, int y, int width, int height, int color, int thickness) {
        // Top
        fill(graphics, x, y, x + width, y + thickness, color);
        // Bottom
        fill(graphics, x, y + height - thickness, x + width, y + height, color);
        // Left
        fill(graphics, x, y, x + thickness, y + height, color);
        // Right
        fill(graphics, x + width - thickness, y, x + width, y + height, color);
    }

    /**
     * Draws centered text with shadow.
     * 
     * @param graphics The graphics context
     * @param text     The text to draw
     * @param centerX  Center X position
     * @param y        Y position
     * @param color    Text color
     */
    public static void drawCenteredString(GuiGraphics graphics, String text, int centerX, int y, int color) {
        Font font = mc.font;
        @SuppressWarnings("null")
        int textWidth = font.width(text);
        graphics.drawString(font, text, centerX - textWidth / 2, y, color);
    }

    /**
     * Draws text with shadow.
     * 
     * @param graphics The graphics context
     * @param text     The text to draw
     * @param x        X position
     * @param y        Y position
     * @param color    Text color
     */
    public static void drawString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(mc.font, text, x, y, color);
    }

    /**
     * Draws text without shadow.
     * 
     * @param graphics The graphics context
     * @param text     The text to draw
     * @param x        X position
     * @param y        Y position
     * @param color    Text color
     */
    public static void drawStringNoShadow(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(mc.font, text, x, y, color, false);
    }

    /**
     * Gets the width of a string in pixels.
     * 
     * @param text The text
     * @return Width in pixels
     */
    @SuppressWarnings("null")
    public static int getStringWidth(String text) {
        return mc.font.width(text);
    }

    /**
     * Gets the font height in pixels.
     * 
     * @return Font height
     */
    public static int getFontHeight() {
        return mc.font.lineHeight;
    }

    /**
     * Checks if a point is within a rectangle.
     * 
     * @param mouseX Mouse X
     * @param mouseY Mouse Y
     * @param x      Rectangle X
     * @param y      Rectangle Y
     * @param width  Rectangle width
     * @param height Rectangle height
     * @return true if the point is within the rectangle
     */
    public static boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}