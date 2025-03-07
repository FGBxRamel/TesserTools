package de.bydora.tesserTools.enchantment.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class PrepareAnvilListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        var firstItem = event.getInventory().getFirstItem();
        var secondItem = event.getInventory().getSecondItem();
        if (firstItem == null || secondItem == null) {
            return;
        }
        if (firstItem.getType() == Material.ENCHANTED_BOOK
            || secondItem.getType() == Material.ENCHANTED_BOOK
        ) {
            event.setResult(null);
        }
    }

}
