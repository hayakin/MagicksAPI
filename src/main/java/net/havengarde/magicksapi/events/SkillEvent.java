package net.havengarde.magicksapi.events;

import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.skills.Skill;
import org.bukkit.event.Event;

public abstract class SkillEvent extends Event {
    protected final SkillUser skillUser;
    protected final Skill skill;

    protected SkillEvent(SkillUser skillUser, Skill skill) {
        this.skillUser = skillUser;
        this.skill = skill;
    }

    public final SkillUser getSkillUser() { return skillUser; }
    public final Skill getSkill() { return skill; }
}
