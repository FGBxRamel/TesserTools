package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
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


public class InvMoveItemListener implements Listener {

    private final Server server = TesserTools.getPlugin(TesserTools.class).getServer();
    final NamespacedKey boostLevelKey = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
            "boost-level");

    @SuppressWarnings("DataFlowIssue")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInvMoveItem(InventoryMoveItemEvent event) {
        final Inventory srcinv = event.getSource();
        final Inventory destinv = event.getDestination();
        int boostLevel = 0;

        // Führt Prozedur aus wenn: SrcInv == boosted || (DestInv == boosted && SrcInv != Hopper)
        if (srcinv.getLocation().getBlock().getState() instanceof final Hopper srcHopper) {
            final PersistentDataContainer srcContainer = srcHopper.getPersistentDataContainer();
            if (srcContainer.has(boostLevelKey)) {
                boostLevel = srcContainer.get(boostLevelKey, PersistentDataType.INTEGER);
            }
        }
        else if (destinv.getLocation().getBlock().getState() instanceof final Hopper destHopper) {
            final PersistentDataContainer destContainer = destHopper.getPersistentDataContainer();
            if (destContainer.has(boostLevelKey)) {
                boostLevel = destContainer.get(boostLevelKey, PersistentDataType.INTEGER);
            }
        }
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
