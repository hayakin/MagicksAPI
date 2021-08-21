package net.havengarde.magicksapi;

import net.havengarde.magicksapi.enums.CastEvent;
import net.havengarde.magicksapi.skills.Skill;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SkillUser {
    protected transient final ConcurrentHashMap<CastEvent, Set<Skill>> eventSkills = new ConcurrentHashMap<>();
    protected transient final ConcurrentHashMap<Skill, Long> skillCooldownTicks = new ConcurrentHashMap<>();
    protected transient final ConcurrentHashMap<SkillResource, Double> resourceAmounts = new ConcurrentHashMap<>();
    protected transient final ConcurrentHashMap<SkillResource, Double> resourceMaximums = new ConcurrentHashMap<>();
    protected transient final ConcurrentHashMap<SkillResource, Double> resourceRegenRates = new ConcurrentHashMap<>();

    public final long getCooldownTicksForSkill(Skill skill) {
        return skillCooldownTicks.getOrDefault(skill, 0L);
    }
    public final double getResourceAmount(SkillResource resource) {
        return resourceAmounts.getOrDefault(resource, (double) 0);
    }
    public final double getResourceMaximum(SkillResource resource) {
        return resourceMaximums.getOrDefault(resource, (double) 0);
    }
    public final double getResourceRegenRate(SkillResource resource) {
        return resourceRegenRates.getOrDefault(resource, (double) 0);
    }
    public final void setResourceAmount(SkillResource resource, double value) {
        resourceAmounts.put(resource, value);
    }
    public final void setResourceMaximum(SkillResource resource, double value) {
        resourceMaximums.put(resource, value);
    }
    public final void setResourceRegenRate(SkillResource resource, double value) {
        resourceRegenRates.put(resource, value);
    }
    public void assignSkill(Skill skill, CastEvent... castEvents) {
        if (castEvents != null && castEvents.length > 0) {
            for (CastEvent castEvent : castEvents)
                eventSkills.computeIfAbsent(castEvent, v -> new HashSet<>()).add(skill);
        } else {
            // TODO: support non-cast events
        }
    }
    public void unassignSkill(Skill skill) {
        for (CastEvent ce : eventSkills.keySet()) {
            eventSkills.computeIfPresent(ce, (castEvent, skills) -> {
                skills.remove(skill);
                return skills;
            });
            if (eventSkills.get(ce).isEmpty())
                eventSkills.remove(ce);
        }
    }

    public abstract String getName();
    public abstract UUID getId();
}
