package net.havengarde.magicksapi.skills;

import net.havengarde.magicksapi.SkillResource;

public interface Skill {
    SkillResource getResource();
    int getResourceCost();

    /**
     * @return the skill's cooldown in seconds
     */
    double getCooldown();
    String getName();
    long getCastTicks();
}
