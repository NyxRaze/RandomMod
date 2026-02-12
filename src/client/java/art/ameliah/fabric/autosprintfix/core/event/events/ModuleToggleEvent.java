package art.ameliah.fabric.autosprintfix.core.event.events;

import art.ameliah.fabric.autosprintfix.core.event.Event;
import art.ameliah.fabric.autosprintfix.core.module.Module;

/**
 * Event fired when a module is toggled on or off.
 * Can be cancelled to prevent the toggle.
 */
public class ModuleToggleEvent extends Event {

    // The module being toggled
    public final Module module;

    // The new enabled state
    public final boolean enabled;

    /**
     * Creates a new module toggle event.
     * 
     * @param module  The module being toggled
     * @param enabled The new enabled state
     */
    public ModuleToggleEvent(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }
}
