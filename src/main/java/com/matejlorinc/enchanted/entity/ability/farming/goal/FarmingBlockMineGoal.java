package com.matejlorinc.enchanted.entity.ability.farming.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.block.BlockTarget;
import com.matejlorinc.enchanted.entity.ability.block.PigBlockMineTask;
import com.matejlorinc.enchanted.entity.goal.BlockMineGoal;
import org.bukkit.Particle;
import org.bukkit.block.Block;

public class FarmingBlockMineGoal extends BlockMineGoal {
    public FarmingBlockMineGoal(CustomPig mob, double speedModifier) {
        super(mob, mob.getFarmingAbility(), speedModifier);
    }

    @Override
    protected boolean isTargetValid(BlockTarget target) {
        if (target == null) return false;
        return mob.getFarmingAbility().isValidBlock(target.targetBlock());
    }

    @Override
    protected void performMine() {
        Block block = mob.getFarmingAbility().getBlockTarget().targetBlock();
        if (mob.getFarmingAbility().needsNearbyStandingLocation(block.getType())) {
            int breakDurationTicks = mob.getManager().getPlugin().getConfig().getInt("farming.break-duration");
            this.miningTask = new PigBlockMineTask(this.mob, this.mob.getFarmingAbility(), breakDurationTicks, this::stop);
            this.miningTask.start();
            return;
        }

        block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, block.getBlockData());
        block.breakNaturally();
        this.stop();
    }
}
