package net.havengarde.magicksapi.enums;

public enum CastOutcome {
    SUCCESS,
    FAILURE_CUSTOM, // CUSTOM FAILURE
    FAILURE_INSUFFICIENT_RESOURCE,
    FAILURE_ON_COOLDOWN,

    /**
     * When the target is null or doesn't match the accepted target of the skill
     */
    FAILURE_INVALID_TARGET
}
