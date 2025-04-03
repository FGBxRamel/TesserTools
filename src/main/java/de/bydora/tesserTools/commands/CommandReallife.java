package de.bydora.tesserTools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class CommandReallife implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return false;
        } else if (Objects.equals(player.getUniqueId().toString(), "91acaf99-4f22-497d-9c28-1acb65511377")) {
            sender.sendMessage("Böse Nuray!! Los, weiter in die Sucht!");
            return true;
        }

        var random = new Random();
        player.ban("Viel Spaß im Reallife Dungeon :D GLHF", Duration.ofHours(random.nextInt(1,4)),
                "Suchtprävention");
        return true;
    }
}
