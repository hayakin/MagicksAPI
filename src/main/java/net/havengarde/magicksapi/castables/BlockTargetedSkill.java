package net.havengarde.magicksapi.castables;

import net.havengarde.magicksapi.FailureReason;
import net.havengarde.magicksapi.SkillUser;
import org.bukkit.block.Block;

public interface BlockTargetedSkill {
    FailureReason cast(SkillUser caster, Block target);
}
