package net.havengarde.magicksapi.castables;

import net.havengarde.magicksapi.FailureReason;
import net.havengarde.magicksapi.SkillUser;
import org.bukkit.entity.Entity;

public interface EntityTargetedSkill {
    FailureReason cast(SkillUser caster, Entity target);
}
