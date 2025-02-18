package com.matejlorinc.enchanted.entity.ability.fishing;

import com.matejlorinc.enchanted.entity.CustomPig;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public record CatchingFish(CustomPig entity, Location hookLocation, ItemStack caught) {
}
