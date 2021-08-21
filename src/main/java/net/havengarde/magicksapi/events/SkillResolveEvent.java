package net.havengarde.magicksapi.events;

import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.enums.CastOutcome;
import net.havengarde.magicksapi.enums.ResolveOutcome;
import net.havengarde.magicksapi.skills.Skill;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that is fired AFTER a skill is resolved. This is not a cancellable event.
 */
public class SkillResolveEvent extends SkillEvent {
    // region Base event components
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    // endregion

    private final ResolveOutcome outcome;

    public SkillResolveEvent(SkillUser skillUser, Skill skill, ResolveOutcome outcome) {
        super(skillUser, skill);
        this.outcome = outcome;
    }

    public ResolveOutcome getOutcome() { return outcome; }
}
