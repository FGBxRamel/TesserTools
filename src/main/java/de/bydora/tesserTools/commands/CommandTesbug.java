package de.bydora.tesserTools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CommandTesbug implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) commandSender;
        player.sendMessage(Enchantment.BANE_OF_ARTHROPODS.key().asMinimalString());
        return true;
    }
}
