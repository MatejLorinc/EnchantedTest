package com.matejlorinc.enchanted.entity.ability.fishing.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.fishing.CatchingFish;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class CatchFishGoal extends Goal {
    protected final CustomPig mob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private long lastCanUseCheck;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextJump;
    private boolean returning;

    public CatchFishGoal(CustomPig mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L)
            return false;

        this.lastCanUseCheck = gameTime;
        return mob.getCatchingFish() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getCatchingFish() != null;
    }

    @Override
    public void start() {
        this.returning = false;
        this.ticksUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop() {
        navigation.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        CatchingFish catchingFish = this.mob.getCatchingFish();
        if (catchingFish == null) return;

        Location target = returning ? this.mob.getOwner().getBukkitEntity().getLocation() : catchingFish.hookLocation();

        ticksUntilNextJump = Math.max(--ticksUntilNextJump, 0);
        if (returning && this.mob.getBukkitEntity().getLocation().add(0.0, 0.75, 0.0).getBlock().isLiquid()) {
            if (ticksUntilNextJump == 0) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.5, 0.0));
                ticksUntilNextJump = 10;
            }
        }

        this.mob.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ(), 30.0F, 30.0F);
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

        if (this.ticksUntilNextPathRecalculation > 0) return;

        navigation.moveTo(target.getX(), target.getY(), target.getZ(), this.speedModifier);

        this.ticksUntilNextPathRecalculation = 10;
        this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);

        if (!returning) {
            this.checkAndPerformReturn();
        } else {
            checkAndPerformCancel();
        }
    }

    private void checkAndPerformReturn() {
        if (this.isNearHook()) {
            this.ticksUntilNextPathRecalculation = 0;
            this.returning = true;
            Player player = (Player) this.mob.getOwner().getBukkitLivingEntity();
            player.getInventory().addItem(this.mob.getCatchingFish().caught());
        }
    }

    private void checkAndPerformCancel() {
        if (this.isNearPlayer()) {
            this.mob.setCatchingFish(null);
        }
    }

    private boolean isNearHook() {
        Location mobLocation = this.mob.getBukkitEntity().getLocation();
        Location hookLocation = this.mob.getCatchingFish().hookLocation();
        return Math.abs(mobLocation.getX() - hookLocation.getX()) <= 1.5 && Math.abs(mobLocation.getZ() - hookLocation.getZ()) <= 1.5;
    }

    private boolean isNearPlayer() {
        return this.mob.getBukkitEntity().getLocation().distance(this.mob.getOwner().getBukkitEntity().getLocation()) <= 1.5;
    }
}
