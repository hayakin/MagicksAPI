package net.havengarde.magicksapi;

import net.havengarde.aureycore.common.playerstate.PlayerStateManager;
import net.havengarde.magicksapi.castables.ProjectileAttachedSkill;
import net.havengarde.magicksapi.enums.CastEvent;
import net.havengarde.magicksapi.enums.CastOutcome;
import net.havengarde.magicksapi.enums.ResolveOutcome;
import net.havengarde.magicksapi.events.SkillCastEvent;
import net.havengarde.magicksapi.events.SkillResolveEvent;
import net.havengarde.magicksapi.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

import static net.havengarde.magicksapi.MagicksAPI.*;
import static net.havengarde.magicksapi.MagicksAPI.getRegisteredSkillUser;

final class MagicksAPIListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent e) {
        if (unregisterSkillUser(e.getPlayer()))
            log(Level.INFO, "Unregistered SkillUser for player " + e.getPlayer().getPlayerListName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player) && unregisterSkillUser(entity)) // unregister if not player
            log(Level.INFO, "Unregistered SkillUser for entity " + e.getEntity().getEntityId());
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = damaged = event.getEntity();
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            castSkills(damaged, shooter, () -> CastEvent.ON_HIT_RANGED);
        } else {
            Entity damager = event.getDamager();
            castSkills(damager, damaged, () -> {
                if (damager instanceof Player player)
                    return switch (PlayerState.forPlayer(player)) {
                        case CROUCHING -> CastEvent.HIT_ENTITY_CROUCHING;
                        case SPRINTING -> CastEvent.HIT_ENTITY_SPRINTING;
                        case JUMPING -> CastEvent.HIT_ENTITY_JUMPING;
                        case FALLING -> CastEvent.HIT_ENTITY_FALLING;
                        default -> CastEvent.HIT_ENTITY;
                    };
                else
                    return CastEvent.HIT_ENTITY;
            });
            castSkills(damaged, damager, () -> CastEvent.ON_HIT_MELEE);
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        final Entity target = event.getRightClicked();
        castSkills(player, target, () -> switch (PlayerState.forPlayer(player)) {
            case CROUCHING -> CastEvent.INTERACT_ENTITY_CROUCHING;
            case SPRINTING -> CastEvent.INTERACT_ENTITY_SPRINTING;
            case JUMPING -> CastEvent.INTERACT_ENTITY_JUMPING;
            case FALLING -> CastEvent.INTERACT_ENTITY_FALLING;
            default -> CastEvent.INTERACT_ENTITY;
        });
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        final Block target = event.getClickedBlock();
        castSkills(player, target, () -> {
            Action action = event.getAction();
            boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;

            return switch (PlayerState.forPlayer(player)) {
                case CROUCHING ->
                        isLeftClick ? (target == null ? CastEvent.HIT_AIR_CROUCHING : CastEvent.HIT_BLOCK_CROUCHING)
                                : (target == null ? CastEvent.INTERACT_AIR_CROUCHING : CastEvent.INTERACT_BLOCK_CROUCHING);
                case SPRINTING ->
                        isLeftClick ? (target == null ? CastEvent.HIT_AIR_SPRINTING : CastEvent.HIT_BLOCK_SPRINTING)
                                : (target == null ? CastEvent.INTERACT_AIR_SPRINTING : CastEvent.INTERACT_BLOCK_SPRINTING);
                case JUMPING ->
                        isLeftClick ? (target == null ? CastEvent.HIT_AIR_JUMPING : CastEvent.HIT_BLOCK_JUMPING)
                                : (target == null ? CastEvent.INTERACT_AIR_JUMPING : CastEvent.INTERACT_BLOCK_JUMPING);
                case FALLING ->
                        isLeftClick ? (target == null ? CastEvent.HIT_AIR_FALLING : CastEvent.HIT_BLOCK_FALLING)
                                : (target == null ? CastEvent.INTERACT_AIR_FALLING : CastEvent.INTERACT_BLOCK_FALLING);
                default ->
                        isLeftClick ? (target == null ? CastEvent.HIT_AIR: CastEvent.HIT_BLOCK)
                                : (target == null ? CastEvent.INTERACT_AIR : CastEvent.INTERACT_BLOCK);
            };
        });
    }

    @EventHandler
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Entity shooter) {
            castSkills(shooter, projectile, () -> {
                if (source instanceof Player player)
                    return switch (PlayerState.forPlayer(player)) {
                        case CROUCHING -> CastEvent.SHOOT_CROUCHING;
                        case JUMPING -> CastEvent.SHOOT_JUMPING;
                        case FALLING -> CastEvent.SHOOT_FALLING;
                        default -> CastEvent.SHOOT;
                    };
                else
                    return CastEvent.SHOOT;
            });
        }
    }

    @EventHandler
    private void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (MagicksAPI.projectileHasAttachedSkill(projectile) && projectile.getShooter() instanceof Entity shooter) {
            SkillUser su = getRegisteredSkillUser(shooter);
            if (su != null) {
                Object target = event.getHitBlock();
                if (target == null)
                    target = event.getHitEntity();
                for (MetadataValue mv : projectile.getMetadata(PROJECTILE_ATTACHMENT_METADATA_KEY)) {
                    if (mv.value() instanceof Skill skill) {
                        ResolveOutcome resolveOutcome = resolveSkill(su, skill, target);
                        Bukkit.getPluginManager().callEvent(new SkillResolveEvent(su, skill, resolveOutcome));
                        log(Level.INFO, "SkillUser " + su.getName() + " fired SkillResolveEvent with outcome " + resolveOutcome);
                    }
                }
            }
        }
    }

    private void castSkills(Entity caster, Object target, Supplier<CastEvent> ceSupplier) {
        SkillUser su = getRegisteredSkillUser(caster);
        if (su != null && !su.eventSkills.isEmpty()) {
            CastEvent castEvent = ceSupplier.get();
            log(Level.INFO, "SkillUser " + su.getName() + " triggered cast event " + castEvent);

            Set<Skill> skills = su.eventSkills.get(castEvent);
            if (skills == null || skills.isEmpty()) return;

            for (Skill skill : skills) {
                CastOutcome castOutcome = castSkill(su, skill, target);
                Bukkit.getPluginManager().callEvent(new SkillCastEvent(su, skill, castOutcome));
                log(Level.INFO, "SkillUser " + su.getName() + " fired SkillCastEvent with outcome " + castOutcome);
                if (castOutcome == CastOutcome.SUCCESS) {
                    if (skill instanceof ProjectileAttachedSkill &&
                        target instanceof Projectile projectile) { // Attach to projectile
                        projectile.setMetadata(MagicksAPI.PROJECTILE_ATTACHMENT_METADATA_KEY,
                            new FixedMetadataValue(MagicksAPI.instance, skill));
                        log(Level.INFO, "Attached " + skill.getName() + " to projectile of type " + projectile.getType());
                    } else { // Resolve immediately
                        ResolveOutcome resolveOutcome = resolveSkill(su, skill, target);
                        Bukkit.getPluginManager().callEvent(new SkillResolveEvent(su, skill, resolveOutcome));
                        log(Level.INFO, "SkillUser " + su.getName() + " fired SkillResolveEvent with outcome " + resolveOutcome);
                    }
                }
            }
        }
    }

    private enum PlayerState {
        NORMAL, CROUCHING, SPRINTING, JUMPING, FALLING;

        private static PlayerState forPlayer(Player player) {
            if (!player.isOnGround()) {
                if (PlayerStateManager.isJumping(player)) return JUMPING;
                    else return FALLING;
            } else if (player.isSneaking()) return CROUCHING;
            else if (player.isSprinting()) return SPRINTING;
            else return NORMAL;
        }
    }
}
