package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Event fired during rendering.
 * Can be used for HUD overlays and visual effects.
 */
public class RenderEvent extends Event {

    // The graphics context for rendering
    private final GuiGraphics graphics;

    // Delta time for smooth animations
    private final float tickDelta;

    /**
     * Creates a new render event.
     * 
     * @param graphics  The graphics context
     * @param tickDelta The partial tick time
     */
    public RenderEvent(GuiGraphics graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }

    /**
     * Gets the graphics context.
     * 
     * @return The GuiGraphics instance
     */
    public GuiGraphics getGraphics() {
        return graphics;
    }

    /**
     * Gets the tick delta for smooth rendering.
     * 
     * @return The partial tick time
     */
    public float getTickDelta() {
        return tickDelta;
    }
}