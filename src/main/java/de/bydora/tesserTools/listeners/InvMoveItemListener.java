package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.logging.Logger;


public class InvMoveItemListener implements Listener {

    private final Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();
    private final Server server = TesserTools.getPlugin(TesserTools.class).getServer();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInvMoveItem(InventoryMoveItemEvent event) {
        final Inventory srcinv = event.getSource();
        final Inventory destinv = event.getDestination();
        final Material blockMaterial = srcinv.getLocation().getBlock().getType();
        final Hopper hopper = (Hopper) srcinv.getLocation().getBlock().getState();
        final PersistentDataContainer container = hopper.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
                "boost-level");
        if (srcinv.getSize() != 5 ||
            blockMaterial != Material.HOPPER ||
            !container.has(key)
        ) {return;}

        final int boostLevel = container.get(key, PersistentDataType.INTEGER);
        if (boostLevel == 0) {return;}

        final ItemStack transferItems = event.getItem().clone();
        transferItems.setAmount(boostLevel - 1);
        server.getScheduler().runTaskLater(TesserTools.getPlugin(TesserTools.class),
                () -> {
            HashMap<Integer, ItemStack> notExistingItems = srcinv.removeItem(transferItems);
            if (!notExistingItems.isEmpty()) {
                transferItems.setAmount(boostLevel - notExistingItems.get(0).getAmount() - 1);
            }
            HashMap<Integer, ItemStack> remainingItems = destinv.addItem(transferItems);
            if (!remainingItems.isEmpty()) {srcinv.addItem(remainingItems.get(0));}
            },
                0
        );
    }
}
