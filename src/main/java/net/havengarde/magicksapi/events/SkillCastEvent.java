package net.havengarde.magicksapi.events;

import net.havengarde.magicksapi.skills.Skill;
import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.enums.CastOutcome;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that is fired AFTER a skill is casted. This is not a cancellable event.
 */
public final class SkillCastEvent extends SkillEvent {
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

    private final CastOutcome outcome;

    public SkillCastEvent(SkillUser skillUser, Skill skill, CastOutcome outcome) {
        super(skillUser, skill);
        this.outcome = outcome;
    }

    public CastOutcome getOutcome() { return outcome; }
}
