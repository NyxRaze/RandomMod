package art.ameliah.fabric.autosprintfix.gui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
public class RenderUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    public static void fillRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        radius = Math.min(radius, Math.min(width / 2, height / 2));
        if (radius <= 0) {
            fill(graphics, x, y, x + width, y + height, color);
        } else {
            fill(graphics, x + radius, y, x + width - radius, y + height, color);
            fill(graphics, x, y + radius, x + radius, y + height - radius, color);
            fill(graphics, x + width - radius, y + radius, x + width, y + height - radius, color);

            // Corners
            fill(graphics, x, y, x + radius, y + radius, color);
            fill(graphics, x + width - radius, y, x + width, y + radius, color);
            fill(graphics, x, y + height - radius, x + radius, y + height, color);
            fill(graphics, x + width - radius, y + height - radius, x + width, y + height, color);
        }
    }

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

    public static void drawCenteredString(GuiGraphics graphics, String text, int centerX, int y, int color) {
        Font font = mc.font;
        @SuppressWarnings("null")
        int textWidth = font.width(text);
        graphics.drawString(font, text, centerX - textWidth / 2, y, color);
    }

    public static void drawString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(mc.font, text, x, y, color);
    }

    public static void drawStringNoShadow(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(mc.font, text, x, y, color, false);
    }

    @SuppressWarnings("null")
    public static int getStringWidth(String text) {
        return mc.font.width(text);
    }

    public static int getFontHeight() {
        return 9; // Matches original Minecraft font height
    }

    @SuppressWarnings("null")
    public static int drawWrappedString(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        Font font = mc.font;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;
        int lineHeight = 9 + 1; // original + 1 padding

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int testWidth = font.width(testLine);
            if (testWidth <= maxWidth) {
                if (currentLine.length() > 0)
                    currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    graphics.drawString(font, currentLine.toString(), x, currentY, color);
                    currentY += lineHeight;
                }
                if (font.width(word) > maxWidth) {
                    String truncated = truncateString(word, maxWidth - font.width("...")) + "...";
                    graphics.drawString(font, truncated, x, currentY, color);
                    currentY += lineHeight;
                    currentLine.setLength(0);
                } else {
                    currentLine = new StringBuilder(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            graphics.drawString(font, currentLine.toString(), x, currentY, color);
            currentY += lineHeight;
        }

        return currentY - y;
    }

    @SuppressWarnings("null")
    public static String truncateString(String text, int maxWidth) {
        Font font = mc.font;
        if (font.width(text) <= maxWidth)
            return text;

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);

        for (int i = text.length() - 1; i >= 0; i--) {
            String truncated = text.substring(0, i);
            if (font.width(truncated) + ellipsisWidth <= maxWidth)
                return truncated + ellipsis;
        }

        return ellipsis;
    }

    @SuppressWarnings("null")
    public static int getWrappedStringHeight(String text, int maxWidth) {
        Font font = mc.font;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int totalHeight = 0;
        int lineHeight = 9 + 1;

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0)
                    currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0)
                    totalHeight += lineHeight;
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0)
            totalHeight += lineHeight;
        return totalHeight;
    }

    public static void drawTooltip(GuiGraphics graphics, String text, int mouseX, int mouseY, int maxWidth) {
        int textHeight = getWrappedStringHeight(text, maxWidth);
        String[] lines = text.split("\n");
        int tooltipWidth = Math.min(maxWidth, getStringWidth(lines[0]) + 12);

        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, Math.min(maxWidth, getStringWidth(line) + 12));
        }

        tooltipWidth = Math.min(maxWidth, tooltipWidth);
        int tooltipHeight = textHeight + 8;
        int tooltipX = mouseX + 8;
        int tooltipY = mouseY - tooltipHeight - 8;

        if (tooltipX + tooltipWidth > mc.getWindow().getGuiScaledWidth()) {
            tooltipX = mouseX - tooltipWidth - 8;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 8;
        }

        int bgColor = 0xFF000000; // black
        int borderColor = 0xFFAAAAAA; // gray

        fillRounded(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, bgColor);
        drawRect(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight, borderColor, 1);
        drawWrappedString(graphics, text, tooltipX + 6, tooltipY + 4, tooltipWidth - 12, 0xFFFFFFFF);
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
