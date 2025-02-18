package com.matejlorinc.enchanted.entity.mining;

import com.matejlorinc.enchanted.entity.CustomPig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;

public class PigMiningTask {
    public static final int MAX_BREAK_STAGE = 9;
    public static final int ANIMATION_SHOW_DISTANCE = 64;
    private final CustomPig entity;
    private final int durationTicks;
    private final Runnable finishCallback;
    private Instant start;
    private BukkitTask task;

    public PigMiningTask(CustomPig entity, int durationTicks, Runnable finishCallback) {
        this.entity = entity;
        this.durationTicks = durationTicks;
        this.finishCallback = finishCallback;
    }

    public void start() {
        Block block = entity.getBlockTarget().targetBlock();
        int interval = durationTicks / MAX_BREAK_STAGE;

        this.start = Instant.now();
        this.task = new BukkitRunnable() {
            int stage = 0;

            @Override
            public void run() {
                if (entity.getBlockTarget() == null) {
                    finishCallback.run();
                    cancel();
                    return;
                }

                block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5, block.getBlockData());
                if (stage > MAX_BREAK_STAGE) {
                    block.breakNaturally();
                    sendBlockBreakAnimation(block.getLocation(), -1);
                    finishCallback.run();
                    cancel();
                    return;
                }

                sendBlockBreakAnimation(block.getLocation(), stage);
                stage++;
            }
        }.runTaskTimer(entity.getManager().getPlugin(), 0, interval);
    }

    public boolean isDone() {
        return task != null && task.isCancelled();
    }

    private void sendBlockBreakAnimation(Location blockLocation, int stage) {
        BlockPos blockPos = new BlockPos(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(-1, blockPos, stage);

        for (Player player : blockLocation.getNearbyPlayers(ANIMATION_SHOW_DISTANCE)) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            connection.send(packet);
        }
    }
}
