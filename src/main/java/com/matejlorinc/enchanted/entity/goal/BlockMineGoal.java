package com.matejlorinc.enchanted.entity.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.block.BlockTarget;
import com.matejlorinc.enchanted.entity.ability.block.PigBlockMineTask;
import com.matejlorinc.enchanted.entity.ability.block.PigMineAbility;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.EnumSet;

public abstract class BlockMineGoal extends Goal {
    protected final CustomPig mob;
    protected final PigMineAbility mineAbility;
    private final double speedModifier;
    protected PigBlockMineTask miningTask;
    private long lastCanUseCheck;
    private int ticksUntilNextPathRecalculation;
    private int cantReachAttempts;

    public BlockMineGoal(CustomPig mob, PigMineAbility mineAbility, double speedModifier) {
        this.mob = mob;
        this.mineAbility = mineAbility;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L)
            return false;

        this.lastCanUseCheck = gameTime;
        BlockTarget target = this.mineAbility.getBlockTarget();
        return isTargetValid(target);
    }

    @Override
    public boolean canContinueToUse() {
        return isTargetValid(this.mineAbility.getBlockTarget()) && (miningTask == null || !miningTask.isDone());
    }

    protected abstract boolean isTargetValid(BlockTarget target);

    @Override
    public void start() {
        this.cantReachAttempts = 0;
        this.ticksUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop() {
        this.miningTask = null;
        this.mineAbility.setBlockTarget(null);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockTarget target = this.mineAbility.getBlockTarget();
        if (target == null) return;

        Block block = target.targetBlock();
        Location targetLocation = target.targetLocation();

        this.mob.getLookControl().setLookAt(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, 30.0F, 30.0F);
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

        if (this.ticksUntilNextPathRecalculation > 0) return;

        boolean canReach = this.mob.getNavigation().moveTo(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), this.speedModifier);
        if (!canReach && miningTask == null) {
            cantReachAttempts++;
        } else {
            cantReachAttempts = 0;
        }
        if (cantReachAttempts >= 10) {
            stop();
            return;
        }

        this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
        this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);

        this.checkAndPerformMine();
    }

    protected abstract void performMine();

    private void checkAndPerformMine() {
        if (this.canMineBlock() && this.miningTask == null) {
            performMine();
        }
    }

    private boolean canMineBlock() {
        return this.mob.getBukkitEntity().getLocation().distance(this.mineAbility.getBlockTarget().targetLocation().clone().add(0.5, 0, 0.5)) <= 2;
    }
}
