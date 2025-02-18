package com.matejlorinc.enchanted.entity.ability.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.block.Block;

public record BlockTarget(Block targetBlock, Location targetLocation, Path path) {
    public BlockPos locationBlockPos() {
        return BlockPos.containing(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
    }
}
