package de.bydora.tesserTools.commands;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class CommandEnchant implements CommandExecutor, TabCompleter {

    private static final TesserTools plugin = TesserTools.getPlugin(TesserTools.class);
    private static final List<String> ENCHANTMENTS = plugin.getEnchantmentIDs();
    @SuppressWarnings("rawtypes")
    private static final Map<String, CustomEnchantment> ENCHANTMENT_MAP = plugin.getEnchantmentMap();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return false;
        }
        if (args.length < 2) {
            return false;
        }

        String enchantmentId = args[0];
        if (!ENCHANTMENTS.contains(enchantmentId)) {
            player.sendMessage("Ungültige ID!");
            return false;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Das Level muss eine Zahl sein.");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage("Du hältst kein Item in der Hand.");
            return true;
        }

        CustomEnchantment<?> enchantment = ENCHANTMENT_MAP.get(enchantmentId);
        if (enchantment == null || !enchantment.enchantItem(item, level)) {
            player.sendMessage("Fehler beim verzaubern!");
            return false;
        }
        player.sendMessage("Das Item wurde mit " + enchantment.getDisplayName() + " Stufe " + level + " verzaubert!");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Wenn keine Argumente eingegeben wurden
        if (args.length == 1) {
            // Gib alle Enchantment-IDs zurück, die mit dem eingegebenen Text beginnen
            String partial = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (String id : ENCHANTMENTS) {
                if (id.toLowerCase().startsWith(partial)) {
                    suggestions.add(id);
                } else if (id.replaceFirst("tessertools:", "").startsWith(partial)) {
                    suggestions.add(id);
                }
            }
            return suggestions;
        }

        // Für das zweite Argument (Level)
        if (args.length == 2) {
            // Gib Level-Vorschläge (1-5) zurück
            List<String> levels = new ArrayList<>();
            String id = args[0];
            CustomEnchantment<?> enchantment = ENCHANTMENT_MAP.get(id);
            if (enchantment == null) {return levels;}

            String partial = args[1];
            for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
                String level = String.valueOf(i);
                if (level.startsWith(partial)) {
                    levels.add(level);
                }
            }
            return levels;
        }

        // Für alle anderen Argumente: Keine Vorschläge
        return Collections.emptyList();
    }
}
