package net.havengarde.magicksapi.events;

import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.skills.Skill;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that is fired EVERY TICK that a skill is channeling. This is not a cancellable event.
 */
public final class SkillChannelEvent extends SkillEvent {
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

    public SkillChannelEvent(SkillUser skillUser, Skill skill) {
        super(skillUser, skill);
    }
}
