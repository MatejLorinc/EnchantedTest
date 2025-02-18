package com.matejlorinc.enchanted.entity.mining.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.goal.BlockTarget;
import com.matejlorinc.enchanted.entity.mining.PigMiningTask;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.List;

public class MineBlockGoal extends Goal {
    protected final CustomPig mob;
    private final double speedModifier;
    private Path path;
    private long lastCanUseCheck;
    private int ticksUntilNextPathRecalculation;
    private PigMiningTask miningTask;

    public MineBlockGoal(CustomPig mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L)
            return false;

        this.lastCanUseCheck = gameTime;
        BlockTarget target = this.mob.getBlockTarget();
        return isTargetValid(target);
    }

    @Override
    public boolean canContinueToUse() {
        return isTargetValid(this.mob.getBlockTarget()) && (miningTask == null || !miningTask.isDone());
    }

    private boolean isTargetValid(BlockTarget target) {
        if (target == null) return false;
        List<Material> mineableBlockTypes = mob.getManager().getPlugin().getConfig().getStringList("mining.blocks")
                .stream().map(Material::valueOf).toList();
        return mineableBlockTypes.contains(target.targetBlock().getType());
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.ticksUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop() {
        this.miningTask = null;
        this.path = null;
        this.mob.setBlockTarget(null);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockTarget target = this.mob.getBlockTarget();
        if (target == null) return;

        Block block = target.targetBlock();

        this.mob.getLookControl().setLookAt(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, 30.0F, 30.0F);
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

        if (this.ticksUntilNextPathRecalculation > 0) return;

        Path newPath = this.mob.getNavigation().createPath(target.locationBlockPos(), 0);
        if (newPath == null || !newPath.canReach()) {
            if (newPath == null || !newPath.canReach() || newPath.isDone()) {
                stop();
            }
            return;
        }

        if (!newPath.sameAs(this.path)) {
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            this.path = newPath;

            if (!this.mob.getNavigation().moveTo(this.path, this.speedModifier)) {
                this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }

        this.checkAndPerformMine();
    }

    private void checkAndPerformMine() {
        if (this.canMineBlock() && this.miningTask == null) {
            int breakDurationTicks = mob.getManager().getPlugin().getConfig().getInt("mining.break-duration");
            this.miningTask = new PigMiningTask(this.mob, breakDurationTicks, this::stop);
            this.miningTask.start();
        }
    }

    private boolean canMineBlock() {
        return this.mob.getBukkitEntity().getLocation().distance(this.mob.getBlockTarget().targetLocation().clone().add(0.5, 0, 0.5)) <= 1.5;
    }
}
