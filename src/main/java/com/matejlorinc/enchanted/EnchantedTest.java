package com.matejlorinc.enchanted;

import com.matejlorinc.enchanted.commands.LockoutCommand;
import com.matejlorinc.enchanted.commands.SpawnPigCommand;
import com.matejlorinc.enchanted.entity.PigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class EnchantedTest extends JavaPlugin {
    public static final Random RANDOM = new Random();

    private PigManager pigManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("spawnpig").setExecutor(new SpawnPigCommand(this));
        getCommand("lockout").setExecutor(new LockoutCommand(this));

        pigManager = new PigManager(this);
    }

    public PigManager getPigManager() {
        return pigManager;
    }
}
