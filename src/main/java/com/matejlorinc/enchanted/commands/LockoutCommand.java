package com.matejlorinc.enchanted.commands;

import com.matejlorinc.enchanted.EnchantedTest;
import com.matejlorinc.enchanted.entity.CustomPig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class LockoutCommand implements CommandExecutor, TabCompleter {
    private final EnchantedTest plugin;

    public LockoutCommand(EnchantedTest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player player)) return false;
        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /lockout <ticks>").color(NamedTextColor.RED));
            return true;
        }

        int ticks = -1;
        try {
            ticks = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            player.sendMessage(Component.text("'" + args[0] + "' is not a number!").color(NamedTextColor.RED));
            return true;
        }

        CustomPig pig = plugin.getPigManager().getPig(player.getUniqueId());
        if (pig == null) {
            player.sendMessage(Component.text("You don't have any pig spawned!").color(NamedTextColor.RED));
            return true;
        }

        pig.lockout(Instant.now(), Duration.ofMillis(ticks * 50L), "");

        player.sendMessage(Component.text("Successfully locked your pig for " + ticks + " ticks!").color(NamedTextColor.GREEN));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length > 1) return List.of();
        return List.of("<ticks>");
    }
}
