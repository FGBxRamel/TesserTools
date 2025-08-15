package de.bydora.tesserTools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.bydora.tesserTools.TesserTools;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("unused")
public class CommandMigration {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("tes2migration")
//                .requires(sender -> sender.getSender().hasPermission(
//                        "tessertools.enchantment.migration"))
                .executes(CommandMigration::migrationLogic);
    }

    private static int migrationLogic(CommandContext<CommandSourceStack> ctx) {
        Entity executor = Objects.requireNonNull(ctx.getSource().getExecutor());
        if (!(executor instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }
        int level;
        var itemStack = player.getInventory().getItemInMainHand();
        var enchantments = TesserTools.getPlugin(TesserTools.class).getEnchantmentMap().values();
        for (var ench : enchantments) {
            level = ench.getEnchantmentLevel(itemStack);
            if (level > 0) {
                itemStack = ench.enchantItem(itemStack, level);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
