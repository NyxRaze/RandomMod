// src/main/java/art/ameliah/fabric/autosprintfix/gui/ModuleScreen.java
package art.ameliah.fabric.autosprintfix.gui;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.ModuleManager;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.ModeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.NumberSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.RangeSetting;
import art.ameliah.fabric.autosprintfix.core.module.settings.Setting;
import art.ameliah.fabric.autosprintfix.gui.util.ColorUtils;
import art.ameliah.fabric.autosprintfix.gui.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The main module GUI screen - ClickCrystals style.
 * Features a sidebar with categories, settings panel, and module cards with
 * expandable settings.
 */
public class ModuleScreen extends Screen {

    // GUI dimensions
    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 320;
    private static final int SIDEBAR_WIDTH = 50;
    private static final int HEADER_HEIGHT = 35;
    private static final int MODULE_CARD_HEIGHT = 40;
    private static final int MODULE_CARD_SPACING = 5;
    private static final int SETTING_HEIGHT = 25;
    private static final int PADDING = 10;

    // Preset colors for custom theme picker
    private static final int[] PRESET_COLORS = {
            0xFFFF5555, // Red
            0xFFFF8855, // Orange
            0xFFFFFF55, // Yellow
            0xFF55FF55, // Green
            0xFF55FFFF, // Cyan
            0xFF5555FF, // Blue
            0xFFAA55FF, // Purple
            0xFFFF55FF, // Pink
            0xFFFFFFFF, // White
            0xFFAAAAAA, // Gray
    };

    // Currently selected category
    private ModuleCategory selectedCategory = ModuleCategory.MOVEMENT;

    // Animation progress
    private float openAnimation = 0f;

    // Scroll offset for module list
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;

    // Settings panel scroll
    private float settingsPanelScroll = 0f;
    private float settingsPanelTargetScroll = 0f;

    // Search text
    private String searchText = "";
    private boolean searchFocused = false;

    // Settings panel (main GUI settings)
    private boolean settingsOpen = false;
    private float settingsAnimation = 0f;
    private boolean listeningForGuiKeybind = false;

    // Custom color editing
    private int editingColorIndex = -1; // 0=accent, 1=accentDark, 2=background, 3=sidebar, 4=toggleOn
    private String colorInputText = "";
    private boolean colorInputFocused = false;

    // Module keybind listening
    private Module listeningModule = null;

    // Expanded module for settings
    private Module expandedModule = null;

    // Cached positions
    private int guiX;
    private int guiY;
    private double lastMouseX;
    private double lastMouseY;

    // Hover states
    private int hoveredCategoryIndex = -1;
    private int hoveredModuleIndex = -1;
    private int hoveredModuleKeybindIndex = -1;
    private int hoveredModuleExpandIndex = -1;
    private boolean settingsButtonHovered = false;
    private int hoveredThemeButton = -1;
    private boolean keybindButtonHovered = false;
    private boolean resetKeybindsButtonHovered = false;
    private int hoveredPresetColor = -1;
    private int hoveredColorSlot = -1;

    // Setting interaction
    private Setting<?> draggingSetting = null;

    // Logger
    private static final ModLogger logger = ModLogger.getInstance();

    /**
     * Creates a new module screen.
     */
    public ModuleScreen() {
        super(Component.literal("AutoSprintFix"));
    }

    /**
     * Helper method to get GUI settings.
     */
    private GuiSettings getSettings() {
        return GuiSettings.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        guiX = (width - GUI_WIDTH) / 2;
        guiY = (height - GUI_HEIGHT) / 2;

        scrollOffset = 0;
        targetScrollOffset = 0;
        settingsPanelScroll = 0;
        settingsPanelTargetScroll = 0;
        settingsOpen = false;
        settingsAnimation = 0f;
        listeningForGuiKeybind = false;
        listeningModule = null;
        expandedModule = null;
        editingColorIndex = -1;
        colorInputText = "";
        colorInputFocused = false;

        logger.debug("ModuleScreen.init() called");
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        updateAnimations();

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        graphics.fill(0, 0, width, height, ColorUtils.withAlpha(0x000000, (int) (180 * openAnimation)));

        int animOffset = (int) ((1 - openAnimation) * 30);
        int animatedGuiY = guiY + animOffset;

        RenderUtils.fill(graphics, guiX, animatedGuiY, guiX + GUI_WIDTH, animatedGuiY + GUI_HEIGHT,
                getSettings().getBackground());

        renderSidebar(graphics, mouseX, mouseY, animatedGuiY);
        renderHeader(graphics, mouseX, mouseY, animatedGuiY);

        if (settingsOpen && settingsAnimation > 0.1f) {
            renderSettingsPanel(graphics, mouseX, mouseY, animatedGuiY);
        } else if (!settingsOpen || settingsAnimation < 0.9f) {
            renderModuleList(graphics, mouseX, mouseY, animatedGuiY);
        }

        RenderUtils.drawRect(graphics, guiX, animatedGuiY, GUI_WIDTH, GUI_HEIGHT, ColorUtils.CARD_BORDER, 1);

        if (listeningModule != null) {
            renderKeybindListeningOverlay(graphics);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    /**
     * Renders an overlay when listening for a module keybind.
     */
    private void renderKeybindListeningOverlay(GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, ColorUtils.withAlpha(0x000000, 200));

        int boxWidth = 250;
        int boxHeight = 80;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        RenderUtils.fillRounded(graphics, boxX, boxY, boxWidth, boxHeight, 10, getSettings().getBackground());
        RenderUtils.drawRect(graphics, boxX, boxY, boxWidth, boxHeight, getSettings().getAccent(), 2);

        String title = "Set Keybind: " + listeningModule.getName();
        RenderUtils.drawCenteredString(graphics, title, width / 2, boxY + 15, getSettings().getAccent());
        RenderUtils.drawCenteredString(graphics, "Press any key to bind", width / 2, boxY + 35,
                ColorUtils.TEXT_PRIMARY);
        RenderUtils.drawCenteredString(graphics, "Press ESC to cancel, DELETE to unbind", width / 2, boxY + 55,
                ColorUtils.TEXT_SECONDARY);
    }

    /**
     * Updates all animations.
     */
    private void updateAnimations() {
        if (openAnimation < 1f) {
            openAnimation = Math.min(1f, openAnimation + 0.15f);
        }

        if (Math.abs(scrollOffset - targetScrollOffset) > 0.5f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * 0.2f;
        } else {
            scrollOffset = targetScrollOffset;
        }

        if (Math.abs(settingsPanelScroll - settingsPanelTargetScroll) > 0.5f) {
            settingsPanelScroll += (settingsPanelTargetScroll - settingsPanelScroll) * 0.2f;
        } else {
            settingsPanelScroll = settingsPanelTargetScroll;
        }

        float targetSettings = settingsOpen ? 1f : 0f;
        if (Math.abs(settingsAnimation - targetSettings) > 0.01f) {
            settingsAnimation += (targetSettings - settingsAnimation) * 0.2f;
        } else {
            settingsAnimation = targetSettings;
        }
    }

    /**
     * Renders the sidebar with category icons.
     */
    private void renderSidebar(GuiGraphics graphics, int mouseX, int mouseY, int guiY) {
        RenderUtils.fill(graphics, guiX, guiY, guiX + SIDEBAR_WIDTH, guiY + GUI_HEIGHT, getSettings().getSidebar());
        RenderUtils.fill(graphics, guiX + SIDEBAR_WIDTH - 1, guiY, guiX + SIDEBAR_WIDTH, guiY + GUI_HEIGHT,
                ColorUtils.CARD_BORDER);

        // Settings button
        int settingsBtnY = guiY + 5;
        int settingsBtnSize = 30;
        int settingsBtnX = guiX + (SIDEBAR_WIDTH - settingsBtnSize) / 2;

        settingsButtonHovered = mouseX >= settingsBtnX && mouseX < settingsBtnX + settingsBtnSize &&
                mouseY >= settingsBtnY && mouseY < settingsBtnY + settingsBtnSize;

        int settingsBgColor = settingsOpen ? getSettings().getAccent()
                : settingsButtonHovered ? ColorUtils.adjustBrightness(getSettings().getSidebar(), 1.5f)
                        : getSettings().getSidebar();

        RenderUtils.fillRounded(graphics, settingsBtnX, settingsBtnY, settingsBtnSize, settingsBtnSize, 5,
                settingsBgColor);

        int gearColor = settingsOpen ? ColorUtils.BACKGROUND_DARK : ColorUtils.TEXT_SECONDARY;
        RenderUtils.drawCenteredString(graphics, "\u2699", settingsBtnX + settingsBtnSize / 2,
                settingsBtnY + (settingsBtnSize - 8) / 2, gearColor);

        RenderUtils.fill(graphics, guiX + 5, guiY + 40, guiX + SIDEBAR_WIDTH - 5, guiY + 41, ColorUtils.CARD_BORDER);

        // Category buttons
        ModuleCategory[] categories = ModuleCategory.values();
        int categorySize = 32;
        int startY = guiY + 50;

        hoveredCategoryIndex = -1;

        for (int i = 0; i < categories.length; i++) {
            ModuleCategory category = categories[i];
            int catY = startY + (i * (categorySize + 8));
            int catX = guiX + (SIDEBAR_WIDTH - categorySize) / 2;

            boolean hovered = mouseX >= catX && mouseX < catX + categorySize &&
                    mouseY >= catY && mouseY < catY + categorySize;

            if (hovered) {
                hoveredCategoryIndex = i;
            }

            int bgColor;
            if (category == selectedCategory && !settingsOpen) {
                bgColor = getSettings().getAccent();
            } else if (hovered) {
                bgColor = ColorUtils.adjustBrightness(getSettings().getSidebar(), 1.5f);
            } else {
                bgColor = getSettings().getSidebar();
            }

            RenderUtils.fillRounded(graphics, catX, catY, categorySize, categorySize, 5, bgColor);

            int textColor = (category == selectedCategory && !settingsOpen) ? ColorUtils.BACKGROUND_DARK
                    : category.getColor();
            RenderUtils.drawCenteredString(graphics, category.getIcon(), catX + categorySize / 2,
                    catY + (categorySize - 8) / 2, textColor);

            int moduleCount = ModuleManager.getInstance().getModulesByCategory(category).size();
            if (moduleCount > 0) {
                String countStr = String.valueOf(moduleCount);
                int badgeX = catX + categorySize - 6;
                int badgeY = catY - 2;
                RenderUtils.fill(graphics, badgeX - 2, badgeY, badgeX + 10, badgeY + 10, getSettings().getAccent());
                RenderUtils.drawCenteredString(graphics, countStr, badgeX + 4, badgeY + 1, ColorUtils.TEXT_PRIMARY);
            }
        }

        // Category tooltip
        if (hoveredCategoryIndex >= 0 && !settingsOpen) {
            String tooltip = categories[hoveredCategoryIndex].getDisplayName();
            int tooltipX = guiX + SIDEBAR_WIDTH + 5;
            int tooltipY = startY + (hoveredCategoryIndex * (categorySize + 8)) + categorySize / 2 - 4;
            int tooltipWidth = RenderUtils.getStringWidth(tooltip) + 10;
            RenderUtils.fill(graphics, tooltipX, tooltipY - 3, tooltipX + tooltipWidth, tooltipY + 12,
                    ColorUtils.BACKGROUND_LIGHTER);
            RenderUtils.drawString(graphics, tooltip, tooltipX + 5, tooltipY, ColorUtils.TEXT_PRIMARY);
        }
    }

    /**
     * Renders the header with title and search bar.
     */
    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY, int guiY) {
        int headerX = guiX + SIDEBAR_WIDTH;
        int headerWidth = GUI_WIDTH - SIDEBAR_WIDTH;

        RenderUtils.fill(graphics, headerX, guiY, headerX + headerWidth, guiY + HEADER_HEIGHT,
                ColorUtils.adjustBrightness(getSettings().getBackground(), 1.3f));
        RenderUtils.fill(graphics, headerX, guiY + HEADER_HEIGHT - 1, headerX + headerWidth, guiY + HEADER_HEIGHT,
                ColorUtils.CARD_BORDER);

        String title = settingsOpen ? "Settings" : selectedCategory.getDisplayName();
        int titleColor = settingsOpen ? getSettings().getAccent() : selectedCategory.getColor();
        RenderUtils.drawString(graphics, title, headerX + PADDING, guiY + (HEADER_HEIGHT - 8) / 2, titleColor);

        if (!settingsOpen) {
            int searchWidth = 120;
            int searchX = headerX + headerWidth - searchWidth - PADDING;
            int searchY = guiY + (HEADER_HEIGHT - 18) / 2;

            int searchBgColor = searchFocused ? ColorUtils.BACKGROUND_LIGHTER : getSettings().getBackground();
            RenderUtils.fillRounded(graphics, searchX, searchY, searchWidth, 18, 3, searchBgColor);
            RenderUtils.drawRect(graphics, searchX, searchY, searchWidth, 18, ColorUtils.CARD_BORDER, 1);

            String displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
            int textColor = searchText.isEmpty() && !searchFocused ? ColorUtils.TEXT_TERTIARY : ColorUtils.TEXT_PRIMARY;
            RenderUtils.drawString(graphics, displayText, searchX + 6, searchY + 5, textColor);

            if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
                int cursorX = searchX + 6 + RenderUtils.getStringWidth(searchText);
                RenderUtils.fill(graphics, cursorX, searchY + 4, cursorX + 1, searchY + 14, ColorUtils.TEXT_PRIMARY);
            }
        }
    }

    /**
     * Renders the GUI settings panel.
     */
    private void renderSettingsPanel(GuiGraphics graphics, int mouseX, int mouseY, int guiY) {
        int contentX = guiX + SIDEBAR_WIDTH + PADDING;
        int contentY = guiY + HEADER_HEIGHT + PADDING;
        int contentWidth = GUI_WIDTH - SIDEBAR_WIDTH - (PADDING * 2);
        int contentHeight = GUI_HEIGHT - HEADER_HEIGHT - (PADDING * 2);

        boolean isCustomTheme = getSettings().getCurrentTheme() == GuiSettings.ColorTheme.CUSTOM;

        // Calculate total content height for scrolling
        int totalContentHeight = 200; // Base settings height
        if (isCustomTheme) {
            totalContentHeight += 180; // Extra height for color customization
        }

        float maxScroll = Math.max(0, totalContentHeight - contentHeight);
        settingsPanelTargetScroll = Math.max(0, Math.min(settingsPanelTargetScroll, maxScroll));

        // Enable scissor for scrollable content
        graphics.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);

        int currentY = contentY - (int) settingsPanelScroll;

        // === GUI Open Keybind Setting ===
        RenderUtils.drawString(graphics, "GUI Open Keybind", contentX, currentY + 5, ColorUtils.TEXT_PRIMARY);
        RenderUtils.drawString(graphics, "Press the button to set a new keybind", contentX, currentY + 20,
                ColorUtils.TEXT_TERTIARY);

        int btnWidth = 100;
        int btnHeight = 30;
        int btnX = contentX + contentWidth - btnWidth - 10;
        int btnY = currentY + 5;

        keybindButtonHovered = mouseX >= btnX && mouseX < btnX + btnWidth && mouseY >= btnY
                && mouseY < btnY + btnHeight;

        int btnColor = listeningForGuiKeybind ? getSettings().getAccent()
                : keybindButtonHovered ? ColorUtils.CARD_BG_HOVER : ColorUtils.CARD_BG;

        RenderUtils.fillRounded(graphics, btnX, btnY, btnWidth, btnHeight, 5, btnColor);
        RenderUtils.drawRect(graphics, btnX, btnY, btnWidth, btnHeight, ColorUtils.CARD_BORDER, 1);

        String keyText = listeningForGuiKeybind ? "Press key..." : getSettings().getGuiOpenKeyName();
        int keyTextColor = listeningForGuiKeybind ? ColorUtils.TEXT_PRIMARY : ColorUtils.TEXT_SECONDARY;
        RenderUtils.drawCenteredString(graphics, keyText, btnX + btnWidth / 2, btnY + (btnHeight - 8) / 2,
                keyTextColor);

        currentY += 55;

        // === Color Theme Setting ===
        RenderUtils.fill(graphics, contentX, currentY - 5, contentX + contentWidth, currentY - 4,
                ColorUtils.CARD_BORDER);

        RenderUtils.drawString(graphics, "Color Theme", contentX, currentY + 5, ColorUtils.TEXT_PRIMARY);
        RenderUtils.drawString(graphics, "Choose your preferred color scheme", contentX, currentY + 20,
                ColorUtils.TEXT_TERTIARY);

        int selectorWidth = 150;
        int selectorX = contentX + contentWidth - selectorWidth - 10;
        int selectorY = currentY + 5;
        int arrowSize = 25;

        // Left arrow
        int leftArrowX = selectorX;
        boolean leftHovered = mouseX >= leftArrowX && mouseX < leftArrowX + arrowSize && mouseY >= selectorY
                && mouseY < selectorY + 30;
        hoveredThemeButton = leftHovered ? 0 : (hoveredThemeButton == 0 ? -1 : hoveredThemeButton);

        int leftArrowColor = leftHovered ? getSettings().getAccent() : ColorUtils.CARD_BG;
        RenderUtils.fillRounded(graphics, leftArrowX, selectorY, arrowSize, 30, 5, leftArrowColor);
        RenderUtils.drawCenteredString(graphics, "<", leftArrowX + arrowSize / 2, selectorY + 11,
                ColorUtils.TEXT_PRIMARY);

        // Theme name
        String themeName = getSettings().getCurrentTheme().getDisplayName();
        int themeNameX = selectorX + arrowSize + 5;
        int themeNameWidth = selectorWidth - (arrowSize * 2) - 10;
        RenderUtils.fillRounded(graphics, themeNameX, selectorY, themeNameWidth, 30, 5, ColorUtils.CARD_BG);
        RenderUtils.drawCenteredString(graphics, themeName, themeNameX + themeNameWidth / 2, selectorY + 11,
                getSettings().getAccent());

        // Right arrow
        int rightArrowX = selectorX + selectorWidth - arrowSize;
        boolean rightHovered = mouseX >= rightArrowX && mouseX < rightArrowX + arrowSize && mouseY >= selectorY
                && mouseY < selectorY + 30;
        hoveredThemeButton = rightHovered ? 1 : (hoveredThemeButton == 1 ? -1 : hoveredThemeButton);

        int rightArrowColor = rightHovered ? getSettings().getAccent() : ColorUtils.CARD_BG;
        RenderUtils.fillRounded(graphics, rightArrowX, selectorY, arrowSize, 30, 5, rightArrowColor);
        RenderUtils.drawCenteredString(graphics, ">", rightArrowX + arrowSize / 2, selectorY + 11,
                ColorUtils.TEXT_PRIMARY);

        currentY += 55;

        // === Custom Color Editor (only shown when Custom theme is selected) ===
        if (isCustomTheme) {
            RenderUtils.fill(graphics, contentX, currentY - 5, contentX + contentWidth, currentY - 4,
                    ColorUtils.CARD_BORDER);

            RenderUtils.drawString(graphics, "Custom Colors", contentX, currentY + 5, ColorUtils.TEXT_PRIMARY);
            RenderUtils.drawString(graphics, "Click on a color slot to edit, or pick a preset", contentX, currentY + 20,
                    ColorUtils.TEXT_TERTIARY);

            currentY += 40;

            // Color slots
            String[] colorNames = { "Accent", "Accent Dark", "Background", "Sidebar", "Toggle On" };
            int[] colors = {
                    getSettings().getCustomAccent(),
                    getSettings().getCustomAccentDark(),
                    getSettings().getCustomBackground(),
                    getSettings().getCustomSidebar(),
                    getSettings().getCustomToggleOn()
            };

            hoveredColorSlot = -1;

            for (int i = 0; i < colorNames.length; i++) {
                int slotX = contentX;
                int slotY = currentY + (i * 25);
                int colorBoxSize = 20;

                // Color box
                boolean slotHovered = mouseX >= slotX && mouseX < slotX + colorBoxSize &&
                        mouseY >= slotY && mouseY < slotY + colorBoxSize;
                if (slotHovered) {
                    hoveredColorSlot = i;
                }

                RenderUtils.fillRounded(graphics, slotX, slotY, colorBoxSize, colorBoxSize, 3, colors[i]);

                int borderColor = (editingColorIndex == i) ? getSettings().getAccent()
                        : slotHovered ? ColorUtils.TEXT_PRIMARY : ColorUtils.CARD_BORDER;
                RenderUtils.drawRect(graphics, slotX, slotY, colorBoxSize, colorBoxSize, borderColor, 1);

                // Color name
                RenderUtils.drawString(graphics, colorNames[i], slotX + colorBoxSize + 8, slotY + 6,
                        ColorUtils.TEXT_SECONDARY);

                // Hex value
                String hexValue = String.format("#%06X", colors[i] & 0xFFFFFF);
                RenderUtils.drawString(graphics, hexValue, slotX + 110, slotY + 6, ColorUtils.TEXT_TERTIARY);
            }

            currentY += 135;

            // Preset colors
            RenderUtils.drawString(graphics, "Presets:", contentX, currentY, ColorUtils.TEXT_SECONDARY);

            int presetY = currentY + 15;
            int presetSize = 22;
            int presetSpacing = 5;

            hoveredPresetColor = -1;

            for (int i = 0; i < PRESET_COLORS.length; i++) {
                int presetX = contentX + (i * (presetSize + presetSpacing));

                boolean presetHovered = mouseX >= presetX && mouseX < presetX + presetSize &&
                        mouseY >= presetY && mouseY < presetY + presetSize;

                if (presetHovered) {
                    hoveredPresetColor = i;
                }

                RenderUtils.fillRounded(graphics, presetX, presetY, presetSize, presetSize, 3, PRESET_COLORS[i]);

                if (presetHovered) {
                    RenderUtils.drawRect(graphics, presetX, presetY, presetSize, presetSize, ColorUtils.TEXT_PRIMARY,
                            2);
                }
            }

            currentY += 50;

            // Hex input (shown when editing a color)
            if (editingColorIndex >= 0) {
                RenderUtils.drawString(graphics, "Enter hex color for " + colorNames[editingColorIndex] + ":",
                        contentX, currentY, ColorUtils.TEXT_SECONDARY);

                int inputX = contentX;
                int inputY = currentY + 15;
                int inputWidth = 100;
                int inputHeight = 22;

                int inputBgColor = colorInputFocused ? ColorUtils.BACKGROUND_LIGHTER : ColorUtils.CARD_BG;
                RenderUtils.fillRounded(graphics, inputX, inputY, inputWidth, inputHeight, 3, inputBgColor);
                RenderUtils.drawRect(graphics, inputX, inputY, inputWidth, inputHeight, getSettings().getAccent(), 1);

                String displayInput = "#" + colorInputText;
                RenderUtils.drawString(graphics, displayInput, inputX + 6, inputY + 7, ColorUtils.TEXT_PRIMARY);

                if (colorInputFocused && System.currentTimeMillis() % 1000 < 500) {
                    int cursorX = inputX + 6 + RenderUtils.getStringWidth(displayInput);
                    RenderUtils.fill(graphics, cursorX, inputY + 5, cursorX + 1, inputY + 17, ColorUtils.TEXT_PRIMARY);
                }

                // Apply button
                int applyBtnX = inputX + inputWidth + 10;
                boolean applyHovered = mouseX >= applyBtnX && mouseX < applyBtnX + 50 &&
                        mouseY >= inputY && mouseY < inputY + inputHeight;

                int applyColor = applyHovered ? getSettings().getAccent() : ColorUtils.CARD_BG;
                RenderUtils.fillRounded(graphics, applyBtnX, inputY, 50, inputHeight, 3, applyColor);
                RenderUtils.drawCenteredString(graphics, "Apply", applyBtnX + 25, inputY + 7, ColorUtils.TEXT_PRIMARY);

                // Cancel button
                int cancelBtnX = applyBtnX + 60;
                boolean cancelHovered = mouseX >= cancelBtnX && mouseX < cancelBtnX + 50 &&
                        mouseY >= inputY && mouseY < inputY + inputHeight;

                int cancelColor = cancelHovered ? 0xFFCC4444 : ColorUtils.CARD_BG;
                RenderUtils.fillRounded(graphics, cancelBtnX, inputY, 50, inputHeight, 3, cancelColor);
                RenderUtils.drawCenteredString(graphics, "Cancel", cancelBtnX + 25, inputY + 7,
                        ColorUtils.TEXT_PRIMARY);
            }
        } else {
            // === Reset Module Keybinds (when not showing custom colors) ===
            RenderUtils.fill(graphics, contentX, currentY - 5, contentX + contentWidth, currentY - 4,
                    ColorUtils.CARD_BORDER);

            RenderUtils.drawString(graphics, "Module Keybinds", contentX, currentY + 5, ColorUtils.TEXT_PRIMARY);
            RenderUtils.drawString(graphics, "Click on keybind in module list to change", contentX, currentY + 20,
                    ColorUtils.TEXT_TERTIARY);

            int resetBtnWidth = 120;
            int resetBtnX = contentX + contentWidth - resetBtnWidth - 10;
            int resetBtnY = currentY + 5;

            resetKeybindsButtonHovered = mouseX >= resetBtnX && mouseX < resetBtnX + resetBtnWidth
                    && mouseY >= resetBtnY && mouseY < resetBtnY + btnHeight;

            int resetBtnColor = resetKeybindsButtonHovered ? 0xFFCC4444 : ColorUtils.CARD_BG;
            RenderUtils.fillRounded(graphics, resetBtnX, resetBtnY, resetBtnWidth, btnHeight, 5, resetBtnColor);
            RenderUtils.drawRect(graphics, resetBtnX, resetBtnY, resetBtnWidth, btnHeight, ColorUtils.CARD_BORDER, 1);
            RenderUtils.drawCenteredString(graphics, "Reset All", resetBtnX + resetBtnWidth / 2,
                    resetBtnY + (btnHeight - 8) / 2, ColorUtils.TEXT_PRIMARY);
        }

        graphics.disableScissor();

        // Footer (always visible at bottom)
        int footerY = guiY + GUI_HEIGHT - PADDING - 15;
        RenderUtils.drawCenteredString(graphics, "AutoSprintFix v1.0 - Press ESC to close",
                contentX + contentWidth / 2, footerY, ColorUtils.TEXT_TERTIARY);

        // Scrollbar for settings panel
        if (maxScroll > 0) {
            int scrollbarX = contentX + contentWidth - 4;
            int scrollbarHeight = Math.max(20, (int) (contentHeight * (contentHeight / (float) totalContentHeight)));
            int scrollbarY = contentY + (int) ((contentHeight - scrollbarHeight) * (settingsPanelScroll / maxScroll));

            RenderUtils.fill(graphics, scrollbarX, contentY, scrollbarX + 3, contentY + contentHeight,
                    ColorUtils.adjustBrightness(getSettings().getBackground(), 1.3f));
            RenderUtils.fillRounded(graphics, scrollbarX, scrollbarY, 3, scrollbarHeight, 1, getSettings().getAccent());
        }
    }

    /**
     * Renders the module list for the selected category.
     */
    private void renderModuleList(GuiGraphics graphics, int mouseX, int mouseY, int guiY) {
        int contentX = guiX + SIDEBAR_WIDTH + PADDING;
        int contentY = guiY + HEADER_HEIGHT + PADDING;
        int contentWidth = GUI_WIDTH - SIDEBAR_WIDTH - (PADDING * 2);
        int contentHeight = GUI_HEIGHT - HEADER_HEIGHT - (PADDING * 2);

        List<Module> modules = getFilteredModules();

        int totalHeight = calculateTotalHeight(modules);
        float maxScroll = Math.max(0, totalHeight - contentHeight);
        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));

        graphics.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);

        hoveredModuleIndex = -1;
        hoveredModuleKeybindIndex = -1;
        hoveredModuleExpandIndex = -1;

        int currentY = contentY - (int) scrollOffset;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            boolean isExpanded = expandedModule == module;
            int cardHeight = getModuleCardHeight(module, isExpanded);

            if (currentY + cardHeight >= contentY && currentY <= contentY + contentHeight) {
                boolean cardHovered = mouseX >= contentX && mouseX < contentX + contentWidth &&
                        mouseY >= currentY && mouseY < currentY + MODULE_CARD_HEIGHT &&
                        mouseY >= contentY && mouseY < contentY + contentHeight;

                int keybindBtnX = contentX + contentWidth - 100;
                int keybindBtnWidth = 50;
                int keybindBtnY = currentY + 5;
                int keybindBtnHeight = 20;

                boolean keybindHovered = mouseX >= keybindBtnX && mouseX < keybindBtnX + keybindBtnWidth &&
                        mouseY >= keybindBtnY && mouseY < keybindBtnY + keybindBtnHeight &&
                        mouseY >= contentY && mouseY < contentY + contentHeight;

                boolean expandHovered = !module.getSettings().isEmpty() && cardHovered && !keybindHovered &&
                        mouseX < contentX + contentWidth - 110;

                if (cardHovered)
                    hoveredModuleIndex = i;
                if (keybindHovered)
                    hoveredModuleKeybindIndex = i;
                if (expandHovered)
                    hoveredModuleExpandIndex = i;

                renderModuleCard(graphics, module, contentX, currentY, contentWidth, cardHovered, keybindHovered,
                        isExpanded, mouseX, mouseY, contentY, contentHeight);
            }

            currentY += cardHeight + MODULE_CARD_SPACING;
        }

        graphics.disableScissor();

        if (modules.isEmpty()) {
            String emptyText = searchText.isEmpty() ? "No modules in this category" : "No matching modules";
            RenderUtils.drawCenteredString(graphics, emptyText, contentX + contentWidth / 2,
                    contentY + contentHeight / 2, ColorUtils.TEXT_TERTIARY);
        }

        // Scrollbar
        if (maxScroll > 0) {
            int scrollbarX = contentX + contentWidth - 4;
            int scrollbarHeight = Math.max(20, (int) (contentHeight * (contentHeight / (float) totalHeight)));
            int scrollbarY = contentY + (int) ((contentHeight - scrollbarHeight) * (scrollOffset / maxScroll));

            RenderUtils.fill(graphics, scrollbarX, contentY, scrollbarX + 3, contentY + contentHeight,
                    ColorUtils.adjustBrightness(getSettings().getBackground(), 1.3f));
            RenderUtils.fillRounded(graphics, scrollbarX, scrollbarY, 3, scrollbarHeight, 1, getSettings().getAccent());
        }
    }

    /**
     * Calculates total height of module list including expanded settings.
     */
    private int calculateTotalHeight(List<Module> modules) {
        int total = 0;
        for (Module module : modules) {
            boolean isExpanded = expandedModule == module;
            total += getModuleCardHeight(module, isExpanded) + MODULE_CARD_SPACING;
        }
        return total;
    }

    /**
     * Gets the height of a module card.
     */
    private int getModuleCardHeight(Module module, boolean isExpanded) {
        if (!isExpanded || module.getSettings().isEmpty()) {
            return MODULE_CARD_HEIGHT;
        }
        return MODULE_CARD_HEIGHT + (module.getSettings().size() * (SETTING_HEIGHT + 3)) + 10;
    }

    /**
     * Renders a single module card with expandable settings.
     */
    private void renderModuleCard(GuiGraphics graphics, Module module, int x, int y, int width,
            boolean cardHovered, boolean keybindHovered, boolean isExpanded,
            int mouseX, int mouseY, int contentY, int contentHeight) {
        int totalHeight = getModuleCardHeight(module, isExpanded);

        int bgColor = cardHovered ? ColorUtils.CARD_BG_HOVER : ColorUtils.CARD_BG;
        RenderUtils.fillRounded(graphics, x, y, width, totalHeight, 5, bgColor);

        int accentColor = module.isEnabled() ? getSettings().getToggleOn() : getSettings().getAccent();
        RenderUtils.fill(graphics, x, y + 5, x + 3, y + MODULE_CARD_HEIGHT - 5, accentColor);

        // Module name
        RenderUtils.drawString(graphics, module.getName(), x + 12, y + 8, ColorUtils.TEXT_PRIMARY);

        // Expand arrow
        if (!module.getSettings().isEmpty()) {
            String arrow = isExpanded ? "\u25BC" : "\u25B6";
            int arrowX = x + 12 + RenderUtils.getStringWidth(module.getName()) + 5;
            RenderUtils.drawString(graphics, arrow, arrowX, y + 8, ColorUtils.TEXT_TERTIARY);
        }

        // Description
        String desc = module.getDescription();
        if (desc.length() > 28) {
            desc = desc.substring(0, 25) + "...";
        }
        RenderUtils.drawString(graphics, desc, x + 12, y + 22, ColorUtils.TEXT_SECONDARY);

        // Keybind button
        int keybindBtnX = x + width - 100;
        int keybindBtnWidth = 50;
        int keybindBtnY = y + 5;
        int keybindBtnHeight = 20;

        int keybindBgColor = (listeningModule == module) ? getSettings().getAccent()
                : keybindHovered ? ColorUtils.CARD_BG_HOVER : ColorUtils.adjustBrightness(ColorUtils.CARD_BG, 0.8f);

        RenderUtils.fillRounded(graphics, keybindBtnX, keybindBtnY, keybindBtnWidth, keybindBtnHeight, 3,
                keybindBgColor);
        RenderUtils.drawRect(graphics, keybindBtnX, keybindBtnY, keybindBtnWidth, keybindBtnHeight,
                ColorUtils.CARD_BORDER, 1);

        String keybindText = (listeningModule == module) ? "..." : module.getKeyBindName();
        int keybindTextColor = (listeningModule == module) ? ColorUtils.TEXT_PRIMARY
                : keybindHovered ? ColorUtils.TEXT_PRIMARY : ColorUtils.TEXT_TERTIARY;
        RenderUtils.drawCenteredString(graphics, keybindText, keybindBtnX + keybindBtnWidth / 2,
                keybindBtnY + (keybindBtnHeight - 8) / 2, keybindTextColor);

        // Toggle switch
        int toggleX = x + width - 45;
        int toggleY = y + (MODULE_CARD_HEIGHT - 16) / 2;
        renderToggle(graphics, toggleX, toggleY, module.isEnabled());

        // Expanded settings
        if (isExpanded && !module.getSettings().isEmpty()) {
            RenderUtils.fill(graphics, x + 10, y + MODULE_CARD_HEIGHT - 2, x + width - 10, y + MODULE_CARD_HEIGHT - 1,
                    ColorUtils.CARD_BORDER);
            renderModuleSettings(graphics, module, x, y + MODULE_CARD_HEIGHT, width, mouseX, mouseY, contentY,
                    contentHeight);
        }
    }

    /**
     * Renders module settings.
     */
    private void renderModuleSettings(GuiGraphics graphics, Module module, int x, int y, int width,
            int mouseX, int mouseY, int contentY, int contentHeight) {
        int settingY = y + 5;

        for (Setting<?> setting : module.getSettings()) {
            if (settingY + SETTING_HEIGHT < contentY || settingY > contentY + contentHeight) {
                settingY += SETTING_HEIGHT + 3;
                continue;
            }

            RenderUtils.fillRounded(graphics, x + 8, settingY, width - 16, SETTING_HEIGHT, 3,
                    ColorUtils.adjustBrightness(ColorUtils.CARD_BG, 0.9f));
            RenderUtils.drawString(graphics, setting.getName(), x + 15, settingY + 8, ColorUtils.TEXT_SECONDARY);

            int controlX = x + width - 80;
            int controlY = settingY + 4;

            if (setting instanceof BooleanSetting boolSetting) {
                renderBooleanControl(graphics, boolSetting, controlX + 30, controlY);
            } else if (setting instanceof ModeSetting modeSetting) {
                renderModeControl(graphics, modeSetting, controlX - 20, controlY, 90);
            } else if (setting instanceof NumberSetting numSetting) {
                renderNumberControl(graphics, numSetting, controlX - 20, controlY, 90);
            } else if (setting instanceof RangeSetting rangeSetting) {
                renderRangeControl(graphics, rangeSetting, controlX - 30, controlY, 100);
            }

            settingY += SETTING_HEIGHT + 3;
        }
    }

    /**
     * Renders a range slider control with min and max handles.
     */
    private void renderRangeControl(GuiGraphics graphics, RangeSetting setting, int x, int y, int width) {
        int height = 12;
        int handleWidth = 4;

        // Background track
        RenderUtils.fillRounded(graphics, x, y + 2, width - 40, height, 3, ColorUtils.TOGGLE_OFF);

        // Calculate handle positions
        double minProgress = setting.getMinProgress();
        double maxProgress = setting.getMaxProgress();

        int trackWidth = width - 40 - handleWidth;
        int minHandleX = x + (int) (trackWidth * minProgress);
        int maxHandleX = x + (int) (trackWidth * maxProgress);

        // Filled portion between handles
        RenderUtils.fill(graphics, minHandleX + handleWidth / 2, y + 4,
                maxHandleX + handleWidth / 2, y + height, getSettings().getAccent());

        // Min handle
        RenderUtils.fillRounded(graphics, minHandleX, y, handleWidth, height + 4, 2, ColorUtils.TEXT_PRIMARY);

        // Max handle
        RenderUtils.fillRounded(graphics, maxHandleX, y, handleWidth, height + 4, 2, ColorUtils.TEXT_PRIMARY);

        // Value text
        String valueText = setting.getDisplayValue();
        RenderUtils.drawString(graphics, valueText, x + width - 38, y + 2, ColorUtils.TEXT_SECONDARY);
    }

    /**
     * Renders a boolean toggle control.
     */
    private void renderBooleanControl(GuiGraphics graphics, BooleanSetting setting, int x, int y) {
        int width = 36;
        int height = 16;

        boolean enabled = setting.getValue();
        int bgColor = enabled ? getSettings().getToggleOn() : ColorUtils.TOGGLE_OFF;
        RenderUtils.fillRounded(graphics, x, y, width, height, height / 2, bgColor);

        int knobSize = height - 4;
        int knobX = enabled ? x + width - knobSize - 2 : x + 2;
        RenderUtils.fillRounded(graphics, knobX, y + 2, knobSize, knobSize, knobSize / 2, ColorUtils.TOGGLE_KNOB);
    }

    /**
     * Renders a mode selector control.
     */
    private void renderModeControl(GuiGraphics graphics, ModeSetting setting, int x, int y, int width) {
        int height = 16;

        RenderUtils.fillRounded(graphics, x, y, width, height, 3,
                ColorUtils.adjustBrightness(ColorUtils.CARD_BG, 0.7f));
        RenderUtils.drawRect(graphics, x, y, width, height, ColorUtils.CARD_BORDER, 1);

        String modeText = setting.getValue();
        if (modeText.length() > 10) {
            modeText = modeText.substring(0, 8) + "..";
        }
        RenderUtils.drawCenteredString(graphics, modeText, x + width / 2, y + 4, ColorUtils.TEXT_PRIMARY);
    }

    /**
     * Renders a number slider control.
     */
    private void renderNumberControl(GuiGraphics graphics, NumberSetting setting, int x, int y, int width) {
        int height = 12;

        RenderUtils.fillRounded(graphics, x, y + 2, width - 30, height, 3, ColorUtils.TOGGLE_OFF);

        double progress = setting.getProgress();
        int filledWidth = (int) ((width - 34) * progress);
        if (filledWidth > 0) {
            RenderUtils.fillRounded(graphics, x + 2, y + 4, filledWidth, height - 4, 2, getSettings().getAccent());
        }

        String valueText = setting.getDisplayValue();
        RenderUtils.drawString(graphics, valueText, x + width - 25, y + 2, ColorUtils.TEXT_SECONDARY);
    }

    /**
     * Renders a toggle switch.
     */
    private void renderToggle(GuiGraphics graphics, int x, int y, boolean enabled) {
        int width = 36;
        int height = 18;

        int bgColor = enabled ? getSettings().getToggleOn() : ColorUtils.TOGGLE_OFF;
        RenderUtils.fillRounded(graphics, x, y, width, height, height / 2, bgColor);

        int knobSize = height - 4;
        int knobX = enabled ? x + width - knobSize - 2 : x + 2;
        RenderUtils.fillRounded(graphics, knobX, y + 2, knobSize, knobSize, knobSize / 2, ColorUtils.TOGGLE_KNOB);
    }

    /**
     * Gets modules filtered by selected category and search text.
     */
    private List<Module> getFilteredModules() {
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);

        if (searchText.isEmpty()) {
            return modules;
        }

        String query = searchText.toLowerCase();
        List<Module> filtered = new ArrayList<>();

        for (Module module : modules) {
            if (module.getName().toLowerCase().contains(query)
                    || module.getDescription().toLowerCase().contains(query)) {
                filtered.add(module);
            }
        }

        return filtered;
    }

    /**
     * Applies the custom color from hex input.
     */
    private void applyCustomColor() {
        if (editingColorIndex < 0 || colorInputText.isEmpty())
            return;

        try {
            // Parse hex color
            String hex = colorInputText.toUpperCase();
            if (hex.length() == 6) {
                int color = 0xFF000000 | Integer.parseInt(hex, 16);

                switch (editingColorIndex) {
                    case 0 -> getSettings().setCustomAccent(color);
                    case 1 -> getSettings().setCustomAccentDark(color);
                    case 2 -> getSettings().setCustomBackground(color);
                    case 3 -> getSettings().setCustomSidebar(color);
                    case 4 -> getSettings().setCustomToggleOn(color);
                }

                editingColorIndex = -1;
                colorInputText = "";
                colorInputFocused = false;
            }
        } catch (NumberFormatException e) {
            // Invalid hex, ignore
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl) {
        int mouseX = (int) lastMouseX;
        int mouseY = (int) lastMouseY;
        int button = mouseButtonEvent.button();

        if (button != 0) {
            return super.mouseClicked(mouseButtonEvent, bl);
        }

        // Cancel module keybind listening
        if (listeningModule != null) {
            listeningModule = null;
            return true;
        }

        // Cancel GUI keybind listening
        if (listeningForGuiKeybind && !keybindButtonHovered) {
            listeningForGuiKeybind = false;
        }

        // Settings button
        if (settingsButtonHovered) {
            settingsOpen = !settingsOpen;
            listeningForGuiKeybind = false;
            expandedModule = null;
            editingColorIndex = -1;
            colorInputFocused = false;
            settingsPanelScroll = 0;
            settingsPanelTargetScroll = 0;
            return true;
        }

        // GUI Settings panel clicks
        if (settingsOpen) {
            if (keybindButtonHovered) {
                listeningForGuiKeybind = !listeningForGuiKeybind;
                return true;
            }

            if (resetKeybindsButtonHovered) {
                ModuleManager.getInstance().resetAllKeybinds();
                return true;
            }

            if (hoveredThemeButton == 0) {
                getSettings().previousTheme();
                editingColorIndex = -1;
                colorInputFocused = false;
                return true;
            } else if (hoveredThemeButton == 1) {
                getSettings().nextTheme();
                editingColorIndex = -1;
                colorInputFocused = false;
                return true;
            }

            // Custom color editing
            if (getSettings().getCurrentTheme() == GuiSettings.ColorTheme.CUSTOM) {
                // Color slot clicked
                if (hoveredColorSlot >= 0) {
                    editingColorIndex = hoveredColorSlot;
                    colorInputText = String.format("%06X", getCurrentColorValue(hoveredColorSlot) & 0xFFFFFF);
                    colorInputFocused = true;
                    return true;
                }

                // Preset color clicked
                if (hoveredPresetColor >= 0 && editingColorIndex >= 0) {
                    int presetColor = PRESET_COLORS[hoveredPresetColor];
                    applyPresetColor(editingColorIndex, presetColor);
                    return true;
                }

                // Check for Apply/Cancel button clicks
                if (editingColorIndex >= 0) {
                    int animOffset = (int) ((1 - openAnimation) * 30);
                    int contentX = guiX + SIDEBAR_WIDTH + PADDING;
                    int contentY = guiY + animOffset + HEADER_HEIGHT + PADDING;

                    int inputY = contentY - (int) settingsPanelScroll + 40 + 40 + (5 * 25) + 50 + 15;
                    int inputHeight = 22;

                    // Apply button
                    int applyBtnX = contentX + 110;
                    if (mouseX >= applyBtnX && mouseX < applyBtnX + 50 && mouseY >= inputY
                            && mouseY < inputY + inputHeight) {
                        applyCustomColor();
                        return true;
                    }

                    // Cancel button
                    int cancelBtnX = applyBtnX + 60;
                    if (mouseX >= cancelBtnX && mouseX < cancelBtnX + 50 && mouseY >= inputY
                            && mouseY < inputY + inputHeight) {
                        editingColorIndex = -1;
                        colorInputText = "";
                        colorInputFocused = false;
                        return true;
                    }

                    // Color input field click
                    int inputX = contentX;
                    int inputWidth = 100;
                    if (mouseX >= inputX && mouseX < inputX + inputWidth && mouseY >= inputY
                            && mouseY < inputY + inputHeight) {
                        colorInputFocused = true;
                        return true;
                    } else {
                        colorInputFocused = false;
                    }
                }
            }

            return true;
        }

        // Category clicks
        if (hoveredCategoryIndex >= 0) {
            selectedCategory = ModuleCategory.values()[hoveredCategoryIndex];
            scrollOffset = 0;
            targetScrollOffset = 0;
            expandedModule = null;
            return true;
        }

        // Search bar click
        int headerX = guiX + SIDEBAR_WIDTH;
        int headerWidth = GUI_WIDTH - SIDEBAR_WIDTH;
        int searchWidth = 120;
        int searchX = headerX + headerWidth - searchWidth - PADDING;
        int animOffset = (int) ((1 - openAnimation) * 30);
        int animatedGuiY = guiY + animOffset;
        int searchY = animatedGuiY + (HEADER_HEIGHT - 18) / 2;

        if (mouseX >= searchX && mouseX < searchX + searchWidth && mouseY >= searchY && mouseY < searchY + 18) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Module keybind clicks
        if (hoveredModuleKeybindIndex >= 0) {
            List<Module> modules = getFilteredModules();
            if (hoveredModuleKeybindIndex < modules.size()) {
                listeningModule = modules.get(hoveredModuleKeybindIndex);
                return true;
            }
        }

        // Module expand clicks
        if (hoveredModuleExpandIndex >= 0) {
            List<Module> modules = getFilteredModules();
            if (hoveredModuleExpandIndex < modules.size()) {
                Module module = modules.get(hoveredModuleExpandIndex);
                if (!module.getSettings().isEmpty()) {
                    expandedModule = (expandedModule == module) ? null : module;
                    return true;
                }
            }
        }

        // Module toggle clicks
        if (hoveredModuleIndex >= 0 && hoveredModuleExpandIndex < 0 && hoveredModuleKeybindIndex < 0) {
            List<Module> modules = getFilteredModules();
            if (hoveredModuleIndex < modules.size()) {
                Module module = modules.get(hoveredModuleIndex);
                module.toggle();
                return true;
            }
        }

        // Module setting clicks
        if (expandedModule != null) {
            if (handleSettingClick(mouseX, mouseY)) {
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    /**
     * Gets the current color value for a color slot index.
     */
    private int getCurrentColorValue(int index) {
        return switch (index) {
            case 0 -> getSettings().getCustomAccent();
            case 1 -> getSettings().getCustomAccentDark();
            case 2 -> getSettings().getCustomBackground();
            case 3 -> getSettings().getCustomSidebar();
            case 4 -> getSettings().getCustomToggleOn();
            default -> 0xFFFFFFFF;
        };
    }

    /**
     * Applies a preset color to the currently editing color slot.
     */
    private void applyPresetColor(int slotIndex, int color) {
        switch (slotIndex) {
            case 0 -> getSettings().setCustomAccent(color);
            case 1 -> getSettings().setCustomAccentDark(color);
            case 2 -> getSettings().setCustomBackground(color);
            case 3 -> getSettings().setCustomSidebar(color);
            case 4 -> getSettings().setCustomToggleOn(color);
        }
        colorInputText = String.format("%06X", color & 0xFFFFFF);
    }

    /**
     * Handles clicks on module settings.
     */
    private boolean handleSettingClick(int mouseX, int mouseY) {
        int contentX = guiX + SIDEBAR_WIDTH + PADDING;
        int animOffset = (int) ((1 - openAnimation) * 30);
        int contentY = guiY + animOffset + HEADER_HEIGHT + PADDING;
        int contentWidth = GUI_WIDTH - SIDEBAR_WIDTH - (PADDING * 2);

        List<Module> modules = getFilteredModules();
        int currentY = contentY - (int) scrollOffset;

        for (Module module : modules) {
            boolean isExpanded = expandedModule == module;
            int cardHeight = getModuleCardHeight(module, isExpanded);

            if (isExpanded && module == expandedModule) {
                int settingY = currentY + MODULE_CARD_HEIGHT + 5;

                for (Setting<?> setting : module.getSettings()) {
                    int controlX = contentX + contentWidth - 80;
                    int controlY = settingY + 4;

                    if (setting instanceof BooleanSetting boolSetting) {
                        if (mouseX >= controlX + 30 && mouseX < controlX + 66 && mouseY >= controlY
                                && mouseY < controlY + 16) {
                            boolSetting.toggle();
                            return true;
                        }
                    } else if (setting instanceof ModeSetting modeSetting) {
                        if (mouseX >= controlX - 20 && mouseX < controlX + 70 && mouseY >= controlY
                                && mouseY < controlY + 16) {
                            modeSetting.cycle();
                            return true;
                        }
                    } else if (setting instanceof NumberSetting numSetting) {
                        int sliderX = controlX - 20;
                        int sliderWidth = 60;
                        if (mouseX >= sliderX && mouseX < sliderX + sliderWidth && mouseY >= controlY
                                && mouseY < controlY + 16) {
                            double progress = (double) (mouseX - sliderX) / sliderWidth;
                            numSetting.setFromProgress(progress);
                            draggingSetting = numSetting;
                            return true;
                        }
                    }

                    settingY += SETTING_HEIGHT + 3;
                }
            }

            currentY += cardHeight + MODULE_CARD_SPACING;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent) {
        draggingSetting = null;
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (draggingSetting instanceof NumberSetting numSetting) {
            int contentX = guiX + SIDEBAR_WIDTH + PADDING;
            int contentWidth = GUI_WIDTH - SIDEBAR_WIDTH - (PADDING * 2);
            int controlX = contentX + contentWidth - 100;
            int sliderWidth = 60;

            double progress = (lastMouseX - controlX) / sliderWidth;
            numSetting.setFromProgress(progress);
            return true;
        }

        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingsOpen) {
            settingsPanelTargetScroll -= (float) (verticalAmount * 20);
        } else {
            targetScrollOffset -= (float) (verticalAmount * 20);
        }
        return true;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        int keyCode = keyEvent.key();

        // Handle module keybind listening
        if (listeningModule != null) {
            if (keyEvent.isEscape()) {
                listeningModule = null;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                listeningModule.setKeyBind(-1);
                listeningModule = null;
            } else {
                listeningModule.setKeyBind(keyCode);
                listeningModule = null;
            }
            return true;
        }

        // Handle GUI keybind listening
        if (listeningForGuiKeybind) {
            if (keyEvent.isEscape()) {
                listeningForGuiKeybind = false;
            } else {
                getSettings().setGuiOpenKey(keyCode);
                listeningForGuiKeybind = false;
            }
            return true;
        }

        // Handle color input
        if (colorInputFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !colorInputText.isEmpty()) {
                colorInputText = colorInputText.substring(0, colorInputText.length() - 1);
                return true;
            } else if (keyEvent.isEscape()) {
                colorInputFocused = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                applyCustomColor();
                return true;
            }
            return true;
        }

        // Handle search input
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            } else if (keyEvent.isEscape()) {
                if (!searchText.isEmpty()) {
                    searchText = "";
                } else {
                    searchFocused = false;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent characterEvent) {
        if (listeningModule != null || listeningForGuiKeybind) {
            return true;
        }

        int codePoint = characterEvent.codepoint();

        // Handle color input
        if (colorInputFocused) {
            char chr = (char) codePoint;
            // Only allow hex characters (0-9, A-F, a-f)
            if ((chr >= '0' && chr <= '9') || (chr >= 'A' && chr <= 'F') || (chr >= 'a' && chr <= 'f')) {
                if (colorInputText.length() < 6) {
                    colorInputText += Character.toUpperCase(chr);
                }
            }
            return true;
        }

        // Handle search input
        if (searchFocused) {
            if (Character.isLetterOrDigit(codePoint) || Character.isSpaceChar(codePoint) || codePoint == '_'
                    || codePoint == '-') {
                searchText += Character.toString(codePoint);
                return true;
            }
        }

        return super.charTyped(characterEvent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}