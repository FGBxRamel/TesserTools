package de.bydora.tesserTools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("unused")
public class CommandTesbug {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("tesbug")
//                .requires(sender -> sender.getSender().hasPermission(
//                        "tessertools.debug.tesbug"))
                .executes(CommandTesbug::tesbugLogic);
    }

    private static int tesbugLogic(CommandContext<CommandSourceStack> ctx) {
        Entity executor = Objects.requireNonNull(ctx.getSource().getExecutor());
        if (!(executor instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }
        player.sendPlainMessage("Hello!");
        return Command.SINGLE_SUCCESS;
    }
}
