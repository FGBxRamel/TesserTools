package de.bydora.tesserTools.enchantment.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class PlayerInteractListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()
            && Objects.requireNonNull(event.getClickedBlock()).getType() == Material.ENCHANTING_TABLE
        ) {
            event.setCancelled(true);
        }
    }
}
