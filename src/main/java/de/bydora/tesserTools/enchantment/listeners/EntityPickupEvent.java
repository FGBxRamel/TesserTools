package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import org.bukkit.block.EnchantingTable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupEvent implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player
        ) {
            // TODO Rework this, it's awful
            var loc = event.getItem().getLocation().toCenterLocation();
            if (loc.getBlock().getState() instanceof EnchantingTable vanillaTable
            ) {
                var extTable = new ExtEnchantingTable(vanillaTable.getLocation());
                extTable.setBlocked(false);
                extTable.clearEnchantments();
            } else if (loc.clone().add(0, -1, 0).getBlock().getState() instanceof EnchantingTable vanillaTable) {
                var extTable = new ExtEnchantingTable(vanillaTable.getLocation());
                extTable.setBlocked(false);
                extTable.clearEnchantments();
            }
        }
    }

}
