package com.matejlorinc.enchanted.entity.ability.block;

import com.matejlorinc.enchanted.entity.CustomPig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

public abstract class PigMineAbility {
    protected final CustomPig entity;
    private final int requiredBlocks;
    private final Duration resetDuration;
    private final int searchRadius;
    private BlockTarget blockTarget;
    private int ownerMinedBlocks = 0;
    private Instant lastOwnerMinedBlocks = Instant.MIN;

    public PigMineAbility(CustomPig entity, int requiredBlocks, Duration resetDuration, int searchRadius) {
        this.entity = entity;
        this.requiredBlocks = requiredBlocks;
        this.resetDuration = resetDuration;
        this.searchRadius = searchRadius;
    }

    public void onPlayerMineBlock(Block block) {
        Player bukkitOwner = (Player) entity.getOwner().getBukkitEntity();

        if (ownerMinedBlocks == 0 || lastOwnerMinedBlocks.plus(resetDuration).isBefore(Instant.now())) {
            ownerMinedBlocks = 1;
        } else {
            ownerMinedBlocks++;
        }
        lastOwnerMinedBlocks = Instant.now();

        if (ownerMinedBlocks >= requiredBlocks) {
            ownerMinedBlocks = 0;
            BlockTarget closestMineableBlock = BlockTargetFinder.findTarget(entity, this::isValidBlock, searchRadius, block, needsNearbyStandingLocation(block.getType()));

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

    public BlockTarget getBlockTarget() {
        return blockTarget;
    }

    public void setBlockTarget(BlockTarget blockTarget) {
        this.blockTarget = blockTarget;
    }

    public abstract boolean needsNearbyStandingLocation(Material material);

    public abstract boolean isValidBlock(Block block);
}
