package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;


public class InvMoveItemListener implements Listener {

    private final Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInvMoveItem(InventoryMoveItemEvent event) {
        final Inventory srcinv = event.getSource();
        final Material blockMaterial = srcinv.getLocation().getBlock().getType();
        if (srcinv.getSize() != 5 || blockMaterial != Material.HOPPER) {return;}
        // TODO Check if the block is boosted

        // TODO check which boost factor

        // TODO check if enough items are there ? item count = boostlevel : max itemcount
        int i = 1;
        ItemStack srcitem = srcinv.getItem(i);
        while (srcitem == null && i < srcinv.getSize()) {
            srcitem = srcinv.getItem(i);
            i++;
        }
        if (srcitem == null) {return;}

    }

}
