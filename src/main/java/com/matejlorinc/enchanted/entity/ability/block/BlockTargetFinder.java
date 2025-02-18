package com.matejlorinc.enchanted.entity.ability.block;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class BlockTargetFinder {
    public static BlockTarget findTarget(PathfinderMob entity, Function<Block, Boolean> blockValidator, int radius, Block ignoreBlock, boolean needsStandingLocation) {
        World world = entity.getBukkitEntity().getWorld();
        Location entityLocation = entity.getBukkitEntity().getLocation();
        List<Block> candidates = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = entityLocation.clone().add(x, y, z);
                    Block block = world.getBlockAt(loc);
                    if (!blockValidator.apply(block)) continue;
                    if (block.getLocation().equals(ignoreBlock.getLocation())) continue;
                    candidates.add(block);
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(b -> entityLocation.distanceSquared(b.getLocation())));

        for (Block candidate : candidates) {
            Location canditateCenterLocation = candidate.getLocation().add(0.5, 0, 0.5);
            Pair<Location, Path> target = needsStandingLocation
                    ? findBestStandingLocation(entity, canditateCenterLocation)
                    : new Pair<>(canditateCenterLocation, computePath(entity, canditateCenterLocation));
            if (target == null) continue;

            return new BlockTarget(candidate, target.getFirst(), target.getSecond());
        }
        return null;
    }

    private static Pair<Location, Path> findBestStandingLocation(PathfinderMob entity, Location blockLocation) {
        World world = blockLocation.getWorld();
        List<Location> possibleLocations = Arrays.asList(
                blockLocation.clone().add(1, 0, 0),
                blockLocation.clone().add(-1, 0, 0),
                blockLocation.clone().add(0, 1, 0),
                blockLocation.clone().add(0, -1, 0),
                blockLocation.clone().add(0, 0, 1),
                blockLocation.clone().add(0, 0, -1),
                blockLocation.clone().add(1, 1, 0),
                blockLocation.clone().add(-1, 1, 0),
                blockLocation.clone().add(0, 1, 1),
                blockLocation.clone().add(0, 1, -1)
        );

        possibleLocations.sort(Comparator.comparingDouble(loc -> entity.getBukkitEntity().getLocation().distanceSquared(loc)));

        for (Location location : possibleLocations) {
            if (!isValidStandingLocation(world, location)) continue;
            Path path = computePath(entity, location);
            if (path == null || !path.canReach()) continue;
            return new Pair<>(location, path);
        }
        return null;
    }

    private static boolean isValidStandingLocation(World world, Location loc) {
        Block feet = world.getBlockAt(loc);
        Block below = world.getBlockAt(loc.clone().add(0, -1, 0));
        return feet.isPassable() && below.getType().isSolid();
    }

    private static Path computePath(PathfinderMob entity, Location target) {
        BlockPos targetPos = new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ());
        return entity.getNavigation().createPath(targetPos, 0);
    }
}