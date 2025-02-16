package com.matejlorinc.enchanted.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListeners implements Listener {
    private final PigManager manager;

    public PlayerConnectionListeners(PigManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.removePig(event.getPlayer().getUniqueId());
    }
}
