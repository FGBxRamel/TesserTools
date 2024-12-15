package de.bydora.tesserTools.enchantment.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()
            && event.getClickedBlock().getType() == Material.ENCHANTING_TABLE
        ) {
            event.setCancelled(true);
        }
    }
}
