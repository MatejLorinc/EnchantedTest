package com.matejlorinc.enchanted.entity.ability.mining;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.block.PigMineAbility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.List;

public class PigMiningAbility extends PigMineAbility {
    private final FileConfiguration config;

    public PigMiningAbility(CustomPig entity, FileConfiguration config) {
        super(entity,
                config.getInt("mining.required-blocks"),
                Duration.ofSeconds(config.getInt("mining.reset-seconds")),
                config.getInt("mining.search-radius"));
        this.config = config;
    }

    @Override
    public boolean isValidBlock(Block block) {
        List<Material> mineableBlockTypes = config.getStringList("mining.blocks")
                .stream().map(Material::valueOf).toList();
        return mineableBlockTypes.contains(block.getType());
    }

    @Override
    public boolean needsNearbyStandingLocation(Material material) {
        return true;
    }
}
