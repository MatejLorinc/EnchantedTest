package com.matejlorinc.enchanted.commands;

import com.matejlorinc.enchanted.EnchantedTest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnPigCommand implements CommandExecutor {
    private final EnchantedTest plugin;

    public SpawnPigCommand(EnchantedTest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player player)) return false;

        if (plugin.getPigManager().getPig(player.getUniqueId()) != null) {
            player.sendMessage(Component.text("You have already spawned custom pig for yourself!").color(NamedTextColor.RED));
            return true;
        }

        plugin.getPigManager().spawnPig(player);
        player.sendMessage(Component.text("Successfully spawned custom pig").color(NamedTextColor.GREEN));

        return true;
    }
}
