package com.matejlorinc.enchanted.entity.ability.mining.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.block.BlockTarget;
import com.matejlorinc.enchanted.entity.ability.block.PigBlockMineTask;
import com.matejlorinc.enchanted.entity.goal.BlockMineGoal;

public class MiningBlockMineGoal extends BlockMineGoal {
    public MiningBlockMineGoal(CustomPig mob, double speedModifier) {
        super(mob, mob.getMiningAbility(), speedModifier);
    }

    @Override
    protected boolean isTargetValid(BlockTarget target) {
        if (target == null) return false;
        return mob.getMiningAbility().isValidBlock(target.targetBlock());
    }

    @Override
    protected void performMine() {
        int breakDurationTicks = mob.getManager().getPlugin().getConfig().getInt("mining.break-duration");
        this.miningTask = new PigBlockMineTask(this.mob, this.mob.getMiningAbility(), breakDurationTicks, this::stop);
        this.miningTask.start();
    }
}
