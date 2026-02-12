package art.ameliah.fabric.autosprintfix.gui;

import art.ameliah.fabric.autosprintfix.gui.util.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Base class for custom GUI screens.
 * Provides common functionality and styling.
 */
public abstract class GuiScreen extends Screen {

    // Animation progress for screen opening
    protected float openProgress = 0f;

    // Animation speed
    protected static final float ANIMATION_SPEED = 0.1f;

    /**
     * Creates a new GUI screen.
     * 
     * @param title The screen title
     */
    protected GuiScreen(Component title) {
        super(title);
    }

    @Override
    public void render(@SuppressWarnings("null") GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Update opening animation
        if (openProgress < 1f) {
            openProgress = Math.min(openProgress + ANIMATION_SPEED, 1f);
        }

        // Draw semi-transparent background
        int bgAlpha = (int) (150 * openProgress);
        graphics.fill(0, 0, width, height, ColorUtils.withAlpha(0x000000, bgAlpha));

        // Render screen content
        renderContent(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);
    }

    /**
     * Renders the screen content.
     * Override this method to add custom rendering.
     * 
     * @param graphics The graphics context
     * @param mouseX   Mouse X position
     * @param mouseY   Mouse Y position
     * @param delta    Partial tick time
     */
    protected abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Override mouseClicked.
     * Subclasses should override this method.
     */
    @SuppressWarnings("null")
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    /**
     * Override mouseReleased.
     * Subclasses should override this method.
     */
    @SuppressWarnings("null")
    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return super.mouseReleased(mouseButtonEvent);
    }

    /**
     * Override mouseDragged.
     * Subclasses should override this method.
     */
    @SuppressWarnings("null")
    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX,
            double deltaY) {
        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    /**
     * Override keyPressed.
     * Subclasses should override this method.
     */
    @SuppressWarnings("null")
    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return super.keyPressed(keyEvent);
    }

    /**
     * Override charTyped.
     * Subclasses should override this method.
     */
    @SuppressWarnings("null")
    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        return super.charTyped(characterEvent);
    }
}