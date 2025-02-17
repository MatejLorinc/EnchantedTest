package com.matejlorinc.enchanted.entity;

import com.matejlorinc.enchanted.EnchantedTest;
import com.matejlorinc.enchanted.entity.combat.goal.PigOwnerHurtTargetGoal;
import com.matejlorinc.enchanted.entity.combat.goal.SingleMeleeAttackGoal;
import com.matejlorinc.enchanted.entity.goal.PigFollowOwnerGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class CustomPig extends Pig {
    private static final FileConfiguration config = JavaPlugin.getProvidingPlugin(EnchantedTest.class).getConfig();
    private final PigManager manager;
    private final Player owner;
    private PigLockout lockout;

    public CustomPig(PigManager manager, Player owner, Location location) {
        super(EntityType.PIG, ((CraftWorld) location.getWorld()).getHandle());
        this.owner = owner;
        this.manager = manager;

        setPos(location.x(), location.y() + 0.2, location.z());
        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(this);

        modifyAttributes();

        getBukkitEntity().setPersistent(false);
    }

    private void modifyAttributes() {
        CraftLivingEntity bukkitLivingEntity = getBukkitLivingEntity();

        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(config.getDouble("speed"));
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(config.getDouble("health"));


        bukkitLivingEntity.registerAttribute(Attribute.ATTACK_DAMAGE);
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(config.getDouble("combat.damage"));
    }

    @Override
    public void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));

        registerCombatGoals();

        goalSelector.addGoal(4, new PigFollowOwnerGoal(this, 1.0, 5.0f, 1.5f));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    private void registerCombatGoals() {
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        goalSelector.addGoal(3, new SingleMeleeAttackGoal(this, config.getDouble("combat.speed-multiplier")));
        targetSelector.addGoal(1, new PigOwnerHurtTargetGoal(this));
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        manager.unregisterPig(owner.getUUID());
    }

    public Player getOwner() {
        return owner;
    }

    public void lockout(Instant started, Duration duration, String animation) {
        lockout = new PigLockout(this, started, duration, animation);

        for (WrappedGoal goal : goalSelector.getAvailableGoals()) {
            if (!goal.isRunning()) continue;
            goal.stop();
        }

        for (WrappedGoal target : targetSelector.getAvailableGoals()) {
            if (!target.isRunning()) continue;
            target.stop();
        }

        goalSelector.removeAllGoals((goal) -> true);
        targetSelector.removeAllGoals((goal) -> true);

        getNavigation().stop();

        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), this::registerGoals, duration.toMillis() / 50);
    }

    public boolean isLockedAi() {
        return lockout != null && lockout.isLocked();
    }
}
