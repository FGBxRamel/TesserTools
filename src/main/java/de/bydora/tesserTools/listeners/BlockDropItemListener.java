package de.bydora.tesserTools.listeners;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockDropItemListener implements Listener {


    @SuppressWarnings("DataFlowIssue")
    @EventHandler(ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!(event.getBlockState() instanceof Hopper hopper)) {return;}
        final PersistentDataContainer container = hopper.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
                "boost-level");
        if (!container.has(key)) {return;}

        final int boostLevel = container.get(key, PersistentDataType.INTEGER);
        final int amountToDrop;
        switch (boostLevel) {
            case 2: amountToDrop = 1; break;
            case 4: amountToDrop = 2; break;
            case 8: amountToDrop = 3; break;
            default: return;
        }
        final ItemStack fireCharge = new ItemStack(Material.FIRE_CHARGE);
        fireCharge.setAmount(amountToDrop);
        hopper.getWorld().dropItem(hopper.getLocation(), fireCharge);
    }
}
