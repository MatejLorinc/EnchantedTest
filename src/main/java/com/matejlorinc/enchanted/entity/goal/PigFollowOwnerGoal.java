package com.matejlorinc.enchanted.entity.goal;

import com.matejlorinc.enchanted.EnchantedTest;
import com.matejlorinc.enchanted.entity.CustomPig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.EnumSet;

public class PigFollowOwnerGoal extends Goal {
    public static final int TELEPORT_DISTANCE_SQUARED = 1024;
    public static final int TELEPORT_ATTEMPTS = 5;
    public static final int TELEPORT_RADIUS = 4;

    private final CustomPig entity;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private final PathNavigation navigation;
    private LivingEntity owner;
    private int timeToRecalcPath = 0;
    private float oldWaterCost;

    public PigFollowOwnerGoal(CustomPig entity, double speed, float startDistance, float stopDistance) {
        this.entity = entity;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.navigation = entity.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = entity.getOwner();
        if (owner == null) return false;

        if (!isInSameWorld()) return false;
        if (unableToMoveToOwner()) return false;
        if (!isTooFar(owner.getX(), owner.getY(), owner.getZ())) return false;

        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return isInSameWorld() && !navigation.isDone() && !unableToMoveToOwner();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = entity.getPathfindingMalus(PathType.WATER);
        entity.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        navigation.stop();
        entity.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null) return;
        boolean shouldTeleport = shouldTryTeleportToOwner();

        if (!shouldTeleport) {
            if (entity.distanceToSqr(owner) <= 256) {
                entity.getLookControl().setLookAt(owner, 10.0f, entity.getMaxHeadXRot());
            }
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (shouldTeleport) {
                Location location = getNearbyTelerportableLocation();
                if (location != null) {
                    entity.teleportTo(location.getX(), location.getY(), location.getZ());
                }
            } else {
                moveToPlayer(owner);
            }
        }
    }

    private void moveToPlayer(LivingEntity owner) {
        double x = getDesiredCoordinate(entity.getX(), owner.getX());
        double z = getDesiredCoordinate(entity.getZ(), owner.getZ());
        double y = owner.getY();

        navigation.moveTo(x, y, z, speed);
    }

    private boolean isTooFar(double x, double y, double z) {
        return Math.abs(entity.getX() - x) >= startDistance ||
                Math.abs(entity.getY() - y) >= startDistance ||
                Math.abs(entity.getZ() - z) >= startDistance;
    }

    private boolean isInSameWorld() {
        return entity.getOwner() != null && entity.getOwner().getBukkitEntity().getWorld().equals(entity.getBukkitEntity().getWorld());
    }

    private double getDesiredCoordinate(double source, double target) {
        if (source > target && source > target + stopDistance) return target + stopDistance;
        if (source < target && source < target - stopDistance) return target - stopDistance;
        return target;
    }

    private boolean unableToMoveToOwner() {
        return entity.isPassenger() || entity.mayBeLeashed() || entity.getOwner() != null && entity.getOwner().isSpectator();
    }

    private boolean shouldTryTeleportToOwner() {
        return entity.getOwner() != null && entity.distanceToSqr(entity.getOwner()) >= TELEPORT_DISTANCE_SQUARED;
    }

    private Location getNearbyTeleportableLocation() {
        for (int i = 0; i <= TELEPORT_ATTEMPTS; i++) {
            int xMargin = EnchantedTest.RANDOM.nextInt(TELEPORT_RADIUS * 2 + 1) - TELEPORT_RADIUS;
            int zMargin = EnchantedTest.RANDOM.nextInt(TELEPORT_RADIUS * 2 + 1) - TELEPORT_RADIUS;

            Location location = owner.getBukkitEntity().getLocation().add(xMargin, 0, zMargin);
            if (!isLocationTeleportable(location)) continue;

            return location;
        }
        return null;
    }

    private boolean isLocationTeleportable(Location location) {
        Block block = location.getBlock();
        if (block.isSolid()) return false;

        for (int i = 1; i <= 5; i++) {
            if (location.clone().subtract(0, i, 0).getBlock().isSolid()) {
                return true;
            }
        }
        return false;
    }
}
