package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import org.bukkit.block.EnchantingTable;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.Objects;

public class EntityPickupEvent implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            EnchantingTable vanillaTable = null;
            var loc = event.getItem().getLocation().toBlockLocation();
            if (loc.getBlock().getState() instanceof EnchantingTable table) {
                vanillaTable = table;
            } else if (loc.clone().add(0, -1, 0).getBlock().getState() instanceof EnchantingTable table) {
                vanillaTable = table;
            }

            if (Objects.isNull(vanillaTable)) return;
            var extTable = new ExtEnchantingTable(vanillaTable.getLocation());
            extTable.setBlocked(false);
            extTable.clearEnchantments();
        }
    }

}
