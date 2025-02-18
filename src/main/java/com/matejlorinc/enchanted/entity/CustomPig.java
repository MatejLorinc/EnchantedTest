package com.matejlorinc.enchanted.entity;

import com.matejlorinc.enchanted.EnchantedTest;
import com.matejlorinc.enchanted.entity.combat.goal.PigOwnerHurtTargetGoal;
import com.matejlorinc.enchanted.entity.combat.goal.SingleMeleeAttackGoal;
import com.matejlorinc.enchanted.entity.goal.BlockTarget;
import com.matejlorinc.enchanted.entity.goal.BlockTargetFinder;
import com.matejlorinc.enchanted.entity.goal.PigFollowOwnerGoal;
import com.matejlorinc.enchanted.entity.mining.goal.MineBlockGoal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class CustomPig extends Pig {
    private static final FileConfiguration config = JavaPlugin.getProvidingPlugin(EnchantedTest.class).getConfig();
    private final PigManager manager;
    private final Player owner;
    private PigLockout lockout;
    private BlockTarget blockTarget;
    private int ownerMinedBlocks = 0;
    private Instant lastOwnerMinedBlocks = Instant.MIN;

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
        registerMiningGoals();

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

    private void registerMiningGoals() {
        goalSelector.addGoal(2, new MineBlockGoal(this, config.getDouble("mining.speed-multiplier")));
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

    public PigManager getManager() {
        return manager;
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

    public BlockTarget getBlockTarget() {
        return blockTarget;
    }

    public void setBlockTarget(BlockTarget blockTarget) {
        this.blockTarget = blockTarget;
    }

    public void onPlayerMineBlock(Block block) {
        int requiredBlocks = config.getInt("mining.required-blocks");
        Duration resetDuration = Duration.ofSeconds(config.getInt("mining.reset-seconds"));

        org.bukkit.entity.Player bukkitOwner = (org.bukkit.entity.Player) owner.getBukkitEntity();

        if (ownerMinedBlocks == 0 || lastOwnerMinedBlocks.plus(resetDuration).isBefore(Instant.now())) {
            ownerMinedBlocks = 1;
        } else {
            ownerMinedBlocks++;
        }
        lastOwnerMinedBlocks = Instant.now();

        if (ownerMinedBlocks >= requiredBlocks) {
            ownerMinedBlocks = 0;
            BlockTarget closestMineableBlock = findClosestMineableBlock(block);

            if (closestMineableBlock == null) {
                bukkitOwner.sendActionBar(Component.text("There is no block available to mine nearby!").color(NamedTextColor.RED));
                return;
            }

            setBlockTarget(closestMineableBlock);
            bukkitOwner.sendActionBar(Component.text("Performing Mining Action").color(NamedTextColor.GREEN));

            return;
        }

        bukkitOwner.sendActionBar(Component.text(ownerMinedBlocks + " / " + requiredBlocks).color(NamedTextColor.GRAY));
    }

    private BlockTarget findClosestMineableBlock(Block ignoreBlock) {
        List<Material> mineableBlockTypes = config.getStringList("mining.blocks").stream().map(Material::valueOf).toList();
        int radius = config.getInt("mining.search-radius");

        return BlockTargetFinder.findTarget(this, mineableBlockTypes, radius, ignoreBlock, true);
    }
}
