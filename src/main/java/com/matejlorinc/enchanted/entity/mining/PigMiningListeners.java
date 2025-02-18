package com.matejlorinc.enchanted.entity.mining;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.PigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class PigMiningListeners implements Listener {
    private final PigManager pigManager;

    public PigMiningListeners(PigManager pigManager) {
        this.pigManager = pigManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        CustomPig pig = pigManager.getPig(event.getPlayer().getUniqueId());
        if (pig == null) return;
        List<Material> mineableBlockTypes = pigManager.getPlugin().getConfig().getStringList("mining.blocks")
                .stream().map(Material::valueOf).toList();
        if (!mineableBlockTypes.contains(event.getBlock().getType())) return;
        pig.onPlayerMineBlock(event.getBlock());
    }
}
