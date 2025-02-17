package com.matejlorinc.enchanted.entity.combat.goal;

import com.matejlorinc.enchanted.entity.CustomPig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Ghast;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class PigOwnerHurtTargetGoal extends TargetGoal {
    private final CustomPig entity;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public PigOwnerHurtTargetGoal(CustomPig entity) {
        super(entity, false);
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = entity.getOwner();
        if (owner == null) {
            return false;
        } else {
            ownerLastHurt = owner.getLastHurtMob();
            int lastHurtMobTimestamp = owner.getLastHurtMobTimestamp();

            if (owner.equals(ownerLastHurt)) return false;
            if (lastHurtMobTimestamp == timestamp) return false;
            if (ownerLastHurt instanceof ArmorStand || ownerLastHurt instanceof Ghast) return false;

            return this.canAttack(ownerLastHurt, TargetingConditions.DEFAULT);
        }
    }

    @Override
    public void start() {
        mob.setTarget(this.ownerLastHurt, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }

        super.start();
    }

    @Override
    public boolean canAttack(LivingEntity target, TargetingConditions targetPredicate) {
        if (target == null || !target.isAlive()) return false;
        return super.canAttack(target, targetPredicate);
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            return false;
        }
        return super.canContinueToUse();
    }
}
