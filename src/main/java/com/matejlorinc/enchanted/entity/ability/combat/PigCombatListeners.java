package com.matejlorinc.enchanted.entity.ability.combat;

import com.matejlorinc.enchanted.entity.CustomPig;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class PigCombatListeners implements Listener {
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() == null) return;
        if (!(((CraftEntity) event.getTarget()).getHandle() instanceof CustomPig)) return;

        event.setCancelled(true);
    }
}
