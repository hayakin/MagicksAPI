package net.havengarde.magicksapi.enums;

public enum CastEvent {
    // Movement events
    MOVE,           JUMP,

    // Left-click events
    HIT_AIR,                HIT_BLOCK,                  HIT_ENTITY,
    HIT_AIR_CROUCHING,      HIT_BLOCK_CROUCHING,        HIT_ENTITY_CROUCHING,
    HIT_AIR_JUMPING,        HIT_BLOCK_JUMPING,          HIT_ENTITY_JUMPING,
    HIT_AIR_FALLING,        HIT_BLOCK_FALLING,          HIT_ENTITY_FALLING,
    HIT_AIR_SPRINTING,      HIT_BLOCK_SPRINTING,        HIT_ENTITY_SPRINTING,

    // Right-click events
    INTERACT_AIR,           INTERACT_BLOCK,             INTERACT_ENTITY,
    INTERACT_AIR_CROUCHING, INTERACT_BLOCK_CROUCHING,   INTERACT_ENTITY_CROUCHING,
    INTERACT_AIR_JUMPING, 	INTERACT_BLOCK_JUMPING,     INTERACT_ENTITY_JUMPING,
    INTERACT_AIR_FALLING, 	INTERACT_BLOCK_FALLING,     INTERACT_ENTITY_FALLING,
    INTERACT_AIR_SPRINTING, INTERACT_BLOCK_SPRINTING,   INTERACT_ENTITY_SPRINTING,

    // Bow shooting events
    SHOOT,
    SHOOT_CROUCHING,
    SHOOT_JUMPING,
    SHOOT_FALLING,
    // cant shoot while running

    // On-hit events
    ON_HIT_MELEE,   ON_HIT_RANGED,

    ;

    public static CastEvent[] hitEntityEvents() {
        return new CastEvent[] { HIT_ENTITY, HIT_ENTITY_FALLING, HIT_ENTITY_CROUCHING, HIT_ENTITY_JUMPING, HIT_ENTITY_SPRINTING };
    }

    public static CastEvent[] shootEvents() {
        return new CastEvent[] { SHOOT, SHOOT_CROUCHING, SHOOT_JUMPING, SHOOT_FALLING };
    }

    public static CastEvent[] combatEvents() {
        return new CastEvent[] {
            HIT_ENTITY, HIT_ENTITY_FALLING, HIT_ENTITY_CROUCHING, HIT_ENTITY_JUMPING, HIT_ENTITY_SPRINTING,
            SHOOT, SHOOT_CROUCHING, SHOOT_JUMPING, SHOOT_FALLING
        };
    }
}
