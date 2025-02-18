package com.matejlorinc.enchanted.entity.ability.farming;

import com.matejlorinc.enchanted.entity.CustomPig;
import com.matejlorinc.enchanted.entity.ability.block.PigMineAbility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.List;

public class PigFarmingAbility extends PigMineAbility {
    private final FileConfiguration config;

    public PigFarmingAbility(CustomPig entity, FileConfiguration config) {
        super(entity,
                config.getInt("farming.required-blocks"),
                Duration.ofSeconds(config.getInt("farming.reset-seconds")),
                config.getInt("farming.search-radius"));
        this.config = config;
    }

    private static List<Material> getMaterialTypesFromConfig(FileConfiguration config, String path) {
        return config.getStringList(path).stream().map(Material::valueOf).toList();
    }

    @Override
    public boolean isValidBlock(Block block) {
        List<Material> mineableBlockTypes = getMaterialTypesFromConfig(config, "farming.blocks");
        if (mineableBlockTypes.contains(block.getType())) return true;

        List<Material> mineableCropTypes = getMaterialTypesFromConfig(config, "farming.crops");
        if (!mineableCropTypes.contains(block.getType())) return false;
        if (!(block.getBlockData() instanceof Ageable ageableBlock)) return false;
        return ageableBlock.getAge() >= ageableBlock.getMaximumAge();
    }

    @Override
    public boolean needsNearbyStandingLocation(Material material) {
        return getMaterialTypesFromConfig(config, "farming.blocks").contains(material);
    }
}
