package com.matejlorinc.enchanted.entity;

import com.matejlorinc.enchanted.entity.goal.PigFollowOwnerGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

import java.time.Duration;
import java.time.Instant;

public class CustomPig extends Pig {
    private final PigManager manager;
    private final Player owner;
    private PigLockout lockout;

    public CustomPig(PigManager manager, Player owner, Location location) {
        super(EntityType.PIG, ((CraftWorld) location.getWorld()).getHandle());
        this.owner = owner;
        this.manager = manager;

        setPos(location.x(), location.y() + 0.2, location.z());
        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(this);

        getBukkitEntity().setPersistent(false);
    }

    @Override
    public void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));

        goalSelector.addGoal(4, new PigFollowOwnerGoal(this, 1.0, 5.0f, 1.5f));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
