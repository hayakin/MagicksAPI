package net.havengarde.magicksapi.resolvables;

import net.havengarde.magicksapi.SkillUser;
import net.havengarde.magicksapi.FailureReason;
import org.bukkit.entity.Entity;

import java.util.List;

public interface EntityGroupResolvedSkill extends EntityResolvedSkill {
    List<Entity> getTargets(SkillUser caster, Entity mainTarget);
    FailureReason resolve(SkillUser caster, Entity mainTarget, List<Entity> targets);
    @Override
    default FailureReason resolve(SkillUser caster, Entity target) {
        return resolve(caster, target, getTargets(caster, target));
    }
}
