package net.havengarde.magicksapi;

import net.havengarde.aureycore.common.util.TaskUtils;
import net.havengarde.magicksapi.castables.ProjectileAttachedSkill;
import net.havengarde.magicksapi.enums.CastEvent;
import net.havengarde.magicksapi.enums.CastOutcome;
import net.havengarde.magicksapi.enums.ResolveOutcome;
import net.havengarde.magicksapi.castables.BlockTargetedSkill;
import net.havengarde.magicksapi.castables.EntityTargetedSkill;
import net.havengarde.magicksapi.resolvables.EntityResolvedSkill;
import net.havengarde.magicksapi.skills.Skill;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MagicksAPI extends JavaPlugin {
    private static final HashMap<UUID, SkillUser> skillUsers = new HashMap<>();
    private static final HashMap<Integer, UUID> entitySkillUserMappings = new HashMap<>();
    static final String PROJECTILE_ATTACHMENT_METADATA_KEY = "MagicksAPI_PAMK";
    static MagicksAPI instance;

    private static Logger logger;
    private static int resourceRegenTaskId, skillCooldownTaskId;

    // region Plugin overrides
    @Override
    public void onDisable() {
        TaskUtils.cancelTask(resourceRegenTaskId);
        TaskUtils.cancelTask(skillCooldownTaskId);
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new MagicksAPIListener(), this);

        resourceRegenTaskId = TaskUtils.runTaskPeriodically(this, 0, 20, () -> {
            for (SkillUser skillUser : MagicksAPI.skillUsers.values()) {
                for (SkillResource resource : skillUser.resourceMaximums.keySet()) {
                    double  max = skillUser.getResourceMaximum(resource),
                            regenAmount = skillUser.getResourceRegenRate(resource),
                            oldValue = skillUser.getResourceAmount(resource),
                            newValue = Math.min(oldValue + regenAmount, max);
                    skillUser.resourceAmounts.put(resource, newValue);
                }
            }
        });

        skillCooldownTaskId = TaskUtils.runTaskPeriodically(this, 0, 1, () -> {
            for (SkillUser skillUser : MagicksAPI.skillUsers.values()) {
                for (Skill s : skillUser.skillCooldownTicks.keySet()) {
                    long newValue = skillUser.getCooldownTicksForSkill(s) - 1;
                    if (newValue > 0)
                        skillUser.skillCooldownTicks.put(s, newValue);
                    else
                        skillUser.skillCooldownTicks.remove(s);
                }
            }
        });

        logger = getLogger();
        log(Level.INFO, "Started instance");
//        logger.setLevel(Level.SEVERE); // set logging to off
    }
    // endregion

    // region TabExecutor overrides
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isOp = sender.isOp();

        switch (command.getName().toLowerCase()) {
            case "magicksapi":
                if (isOp && args.length > 0) {
                    String subcommand = args[0];
                    if (subcommand.equalsIgnoreCase("debug")) {
                        if (args.length > 1) {
                            switch (args[1].toLowerCase()) {
                                case "on" -> {
                                    if (args.length > 2) {
                                        if (args[2].equalsIgnoreCase("confirm")) {
                                            logger.setLevel(Level.INFO);
                                            sender.sendMessage("MagicksAPI debugging set to " + args[1]);
                                        } else return false;
                                    } else {
                                        sender.sendMessage("MagicksAPI debugging may cause server lag.");
                                        sender.sendMessage("Please enter /magicksapi debug on confirm to confirm.");
                                    }
                                }
                                case "off" -> {
                                    logger.setLevel(Level.SEVERE);
                                    sender.sendMessage("MagicksAPI debugging set to " + args[1]);
                                }
                            }
                        }
                    }
                    else if (subcommand.equalsIgnoreCase("register") && sender instanceof Player p) {
                        SkillUser testUser = new SkillUser() {
                            @Override
                            public String getName() {
                                return p.getName();
                            }

                            @Override
                            public UUID getId() {
                                return p.getUniqueId();
                            }
                        };
                        Skill debuggingSkill = new Skill() {
                            @Override
                            public SkillResource getResource() {
                                return null;
                            }

                            @Override
                            public int getResourceCost() {
                                return 0;
                            }

                            @Override
                            public double getCooldown() {
                                return 0;
                            }

                            @Override
                            public String getName() {
                                return "Debugging Skill";
                            }

                            @Override
                            public long getCastTicks() {
                                return 0;
                            }
                        };
                        testUser.assignSkill(debuggingSkill, CastEvent.values());
                        sender.sendMessage("Registered " + p.getDisplayName() + " as SkillUser");
                    }
                    return true;
                } else return false;
            case "bind":
            case "cast":
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return null;
    }
    // endregion

    static <T> CastOutcome castSkill(SkillUser su, Skill skill, T target) {
        SkillResource resource = skill.getResource();
        int resourceCost = skill.getResourceCost();
        double resourceAmount = 0;
        if (resource != null) {
            resourceAmount = su.getResourceAmount(resource);
            if (resourceAmount < resourceCost)
                return CastOutcome.FAILURE_INSUFFICIENT_RESOURCE;
        }
        if (su.getCooldownTicksForSkill(skill) > 0)
            return CastOutcome.FAILURE_ON_COOLDOWN;
        else {
            FailureReason failureReason;
            if (skill instanceof EntityTargetedSkill entityTargetedSkill &&
                target instanceof Entity entityTarget) {
                failureReason = entityTargetedSkill.cast(su, entityTarget);
            } else if (skill instanceof BlockTargetedSkill blockTargetedSkill &&
                       target instanceof Block blockTarget) {
                failureReason = blockTargetedSkill.cast(su, blockTarget);
            } else if (skill instanceof ProjectileAttachedSkill projectileAttachedSkill &&
                        target instanceof Projectile projectileTarget) {
                failureReason = projectileAttachedSkill.cast(su, projectileTarget);
            } else {
                log(Level.SEVERE, "SkillUser " + su.getName() + " tried to cast " + skill.getName()
                     + " on an invalid target");
                return CastOutcome.FAILURE_INVALID_TARGET;
            }

            if (failureReason != null) {
                log(Level.SEVERE, "SkillUser " + su.getName() + " tried to cast " + skill.getName() +
                        " and failed because of custom reason: " + failureReason);
                return CastOutcome.FAILURE_CUSTOM;
            }

            if (resource != null)
                su.resourceAmounts.put(resource, resourceAmount - resourceCost);
            double cooldown = skill.getCooldown();
            if (skill.getCooldown() > 0)
                su.skillCooldownTicks.put(skill, Math.round(cooldown * 20));
            return CastOutcome.SUCCESS;
        }
    }

    static ResolveOutcome resolveSkill(SkillUser su, Skill skill, Object target) {
        FailureReason failureReason;
        if (skill instanceof EntityResolvedSkill entityResolvedSkill &&
                target instanceof Entity entityTarget) {
            failureReason = entityResolvedSkill.resolve(su, entityTarget);
        } else {
            log(Level.SEVERE, "Tried to resolve " + skill.getName() + " from " + su.getName() +
                    " on an invalid target");
            return ResolveOutcome.FAILURE_INVALID_TARGET;
        }

        if (failureReason != null) {
            log(Level.SEVERE, "Tried to resolve " + skill.getName() + " from " + su.getName() +
                    " and failed because of custom reason: " + failureReason);
            return ResolveOutcome.FAILURE_CUSTOM;
        } else return ResolveOutcome.SUCCESS;
    }

    public static void registerSkillUser(SkillUser skillUser) {
        UUID skillUserId = skillUser.getId();
        log(Level.INFO, "Registering SkillUser with id " + skillUserId.toString());
        MagicksAPI.skillUsers.put(skillUserId, skillUser);
    }

    public static boolean unregisterSkillUser(Entity entity) {
        final UUID skillUserId;
        if (entity instanceof Player p)
            skillUserId = p.getUniqueId();
        else
            skillUserId = entitySkillUserMappings.get(entity.getEntityId());

        return skillUsers.remove(skillUserId) != null;
    }

    public static SkillUser getRegisteredSkillUser(Entity entity) {
        final UUID skillUserId;
        if (entity instanceof Player p)
            skillUserId = p.getUniqueId();
        else
            skillUserId = entitySkillUserMappings.get(entity.getEntityId());

        if (skillUserId != null)
            return skillUsers.get(skillUserId);
        else
            return null;
    }

    public static boolean projectileHasAttachedSkill(Projectile projectile) {
        return projectile.hasMetadata(PROJECTILE_ATTACHMENT_METADATA_KEY);
    }

    static void log(Level level, String message) {
        logger.log(level, message);
    }
}
