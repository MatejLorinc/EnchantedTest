package com.matejlorinc.enchanted.entity;

import com.matejlorinc.enchanted.EnchantedTest;
import com.matejlorinc.enchanted.entity.combat.PigCombatListeners;
import com.matejlorinc.enchanted.entity.mining.PigMiningListeners;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PigManager {
    private final EnchantedTest plugin;
    private final Map<UUID, CustomPig> playerPigs = new HashMap<>();

    public PigManager(EnchantedTest plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListeners(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PigCombatListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(new PigMiningListeners(this), plugin);
    }

    public CustomPig spawnPig(Player player) {
        CustomPig customPig = new CustomPig(this, ((CraftPlayer) player).getHandle(), player.getLocation());
        playerPigs.put(player.getUniqueId(), customPig);
        return customPig;
    }

    public CustomPig getPig(UUID playerUniqueId) {
        return playerPigs.get(playerUniqueId);
    }

    public void unregisterPig(UUID playerUniqueId) {
        playerPigs.remove(playerUniqueId);
    }

    public void removePig(UUID playerUniqueId) {
        CustomPig pig = playerPigs.remove(playerUniqueId);
        if (pig == null) return;
        pig.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
    }

    public EnchantedTest getPlugin() {
        return plugin;
    }
}
