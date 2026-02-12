package art.ameliah.fabric.autosprintfix.core.module.modules.misc;

import art.ameliah.fabric.autosprintfix.core.module.Module;
import art.ameliah.fabric.autosprintfix.core.module.ModuleCategory;
import art.ameliah.fabric.autosprintfix.core.module.settings.BooleanSetting;
import art.ameliah.fabric.autosprintfix.core.event.Listener;
import art.ameliah.fabric.autosprintfix.core.event.events.ModuleToggleEvent;
import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;

/**
 * IgnoreList module
 *
 * Allows TriggerBot (or other modules) to ignore friends and team members.
 */
public class IgnoreList extends Module {

    private final ModLogger logger = ModLogger.getInstance();

    private final BooleanSetting friendCheck;
    private final BooleanSetting teamCheck;
    private final BooleanSetting debugMode;

    public IgnoreList() {
        super("IgnoreList",
                "Ignore friends & team from modules like TriggerBot",
                ModuleCategory.MISC);

        this.friendCheck = addSetting(new BooleanSetting("Friends", "Ignore friends", true));
        this.teamCheck = addSetting(new BooleanSetting("Team Check", "Ignore same-team players", true));
        this.debugMode = addSetting(new BooleanSetting("Debug Mode", "Enable debug logging", false));
    }

    /* ================= TOGGLE HANDLER ================= */

    /**
     * Called when the module is toggled.
     * Logs debug info according to its state.
     */
    @Listener
    protected void onToggle(ModuleToggleEvent event) {
        if (debugMode.getValue()) {
            if (event.enabled) {
                logger.debug("IgnoreList enabled");
            } else {
                logger.debug("IgnoreList disabled");
            }
        }
    }

    /* ================= GETTERS ================= */

    public boolean isFriendCheckEnabled() {
        return friendCheck.getValue();
    }

    public boolean isTeamCheckEnabled() {
        return teamCheck.getValue();
    }
}
