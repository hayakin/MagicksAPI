package net.havengarde.magicksapi.resolvables;

import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.FailureReason;
import org.bukkit.entity.Entity;

public interface EntityResolvedSkill {
    FailureReason resolve(SkillUser source, Entity target);
}
