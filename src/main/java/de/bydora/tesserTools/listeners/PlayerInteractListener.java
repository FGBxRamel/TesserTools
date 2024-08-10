package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Logger;

public class PlayerInteractListener implements Listener {

    private final Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
            event.getClickedBlock().getType() != Material.HOPPER ||
            (event.getMaterial() != Material.FIRE_CHARGE &&
                    event.getMaterial() != Material.NETHER_STAR)
        ) {return;}
        final Hopper hopper = (Hopper) event.getClickedBlock().getState();
        final PersistentDataContainer container = hopper.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
                "boost-level");

        if (!container.has(key)) {
            container.set(key, PersistentDataType.INTEGER, 1);
        }
        final int boostLevel = container.get(key, PersistentDataType.INTEGER);
        if (event.getMaterial() == Material.NETHER_STAR) {

            event.getPlayer().sendMessage("Die Booststufe beträgt: " + Integer.toString(boostLevel));
        }
        else {
            hopper.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, boostLevel + 1);
            hopper.update();
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
        event.setCancelled(true);
    }
}
