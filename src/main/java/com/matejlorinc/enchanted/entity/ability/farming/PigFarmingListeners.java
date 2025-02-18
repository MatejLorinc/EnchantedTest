package com.matejlorinc.enchanted.entity.ability.farming;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.PigManager;
import com.matejlorinc.enchanted.entity.ability.block.PigMineAbility;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PigFarmingListeners implements Listener {
    private final PigManager pigManager;

    public PigFarmingListeners(PigManager pigManager) {
        this.pigManager = pigManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        CustomPig pig = pigManager.getPig(event.getPlayer().getUniqueId());
        if (pig == null) return;

        Block block = event.getBlock();
        PigMineAbility mineAbility = pig.getFarmingAbility();
        if (!mineAbility.isValidBlock(block)) return;
        mineAbility.onPlayerMineBlock(block);
    }
}
