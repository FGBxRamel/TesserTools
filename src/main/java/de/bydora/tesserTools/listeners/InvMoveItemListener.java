package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.cache.BoostLevelCache;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public class InvMoveItemListener implements Listener {

    private final Server server;
    private final BoostLevelCache boostCache;

    public InvMoveItemListener(TesserTools plugin) {
        this.server = plugin.getServer();
        this.boostCache = plugin.getBoostLevelCache();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInvMoveItem(InventoryMoveItemEvent event) {
        final Inventory srcinv = event.getSource();
        final Inventory destinv = event.getDestination();

        int boostLevel = Math.max(
                boostCache.getBoostLevel(srcinv),
                boostCache.getBoostLevel(destinv)
        );
        if (boostLevel < 2) {return;}

        final ItemStack transferItems = event.getItem().clone();
        transferItems.setAmount(boostLevel - 1);
        final int finalBoostLevel = boostLevel; // Some Java Lambda bullshit
        server.getScheduler().runTaskLater(TesserTools.getPlugin(TesserTools.class),
                () -> {
            HashMap<Integer, ItemStack> notExistingItems = srcinv.removeItem(transferItems);
            if (!notExistingItems.isEmpty()) {
                transferItems.setAmount(finalBoostLevel - notExistingItems.get(0).getAmount() - 1);
            }
            HashMap<Integer, ItemStack> remainingItems = destinv.addItem(transferItems);
            if (!remainingItems.isEmpty()) {srcinv.addItem(remainingItems.get(0));}
            },
                0
        );
    }
}
