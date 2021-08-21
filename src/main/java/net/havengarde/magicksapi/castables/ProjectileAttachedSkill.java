package net.havengarde.magicksapi.castables;

import net.havengarde.magicksapi.FailureReason;
import net.havengarde.magicksapi.SkillUser;
import org.bukkit.entity.Projectile;

/**
 * A skill that is attached to a projectile after casting.
 */
public interface ProjectileAttachedSkill {
    FailureReason cast(SkillUser caster, Projectile projectile);
}
