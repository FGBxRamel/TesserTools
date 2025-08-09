package de.bydora.tesserTools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class CommandReallife {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("irl")
                .executes(CommandReallife::irlCommandLogic);
    }

    private static int irlCommandLogic(CommandContext<CommandSourceStack> ctx) {
        Entity executor = Objects.requireNonNull(ctx.getSource().getExecutor());
        if (!(executor instanceof Player player)) {
            executor.sendPlainMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return 1;
        } else if (Objects.equals(player.getUniqueId().toString(), "91acaf99-4f22-497d-9c28-1acb65511377")) {
            player.sendPlainMessage("Böse Nuray!! Los, weiter in die Sucht!");
            return 1;
        }

        var random = new Random();
        player.ban("Viel Spaß im Reallife Dungeon :D GLHF", Duration.ofHours(random.nextInt(1,4)),
                "Suchtprävention");
        return 1;
    }
}
