package com.matejlorinc.enchanted.entity.ability.fishing;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.PigManager;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PigFishingListeners implements Listener {
    private final PigManager pigManager;

    public PigFishingListeners(PigManager pigManager) {
        this.pigManager = pigManager;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        CustomPig pig = pigManager.getPig(event.getPlayer().getUniqueId());
        if (pig == null) return;

        if (event.getState() != PlayerFishEvent.State.BITE) return;

        FishHook hook = event.getHook();

        //Loot is just exemplary
        CatchingFish catchingFish = new CatchingFish(pig, hook.getLocation(), new ItemStack(Material.COD));
        pig.setCatchingFish(catchingFish);
    }
}
