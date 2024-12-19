package de.bydora.tesserTools.commands;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class CommandShowEnch implements CommandExecutor {

    private final static TesserTools tesserTools = TesserTools.getPlugin(TesserTools.class);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return false;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        var enchantments = tesserTools.getEnchantmentMap().values();
        for (CustomEnchantment enchantment : enchantments) {
            if (enchantment.getEnchantmentLevel(itemInHand) > 0) {
                player.sendMessage(enchantment.getDisplayName() + ": " + enchantment.getEnchantmentLevel(itemInHand));
            }
        }
        return true;
    }
}
