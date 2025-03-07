package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import org.bukkit.Location;
import org.bukkit.block.EnchantingTable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getState() instanceof EnchantingTable table) {

            Location loc = event.getBlockPlaced().getLocation();

            PersistentDataContainer tableContainer = table.getPersistentDataContainer();
            PersistentDataContainer stateContainer = tableContainer.getAdapterContext().newPersistentDataContainer();

            stateContainer.set(EnchantmentSpaceKeys.STATE_CHARGE_LEVEL.getKey(), PersistentDataType.INTEGER, 0);
            stateContainer.set(EnchantmentSpaceKeys.STATE_BLOCKED.getKey(), PersistentDataType.BOOLEAN, false);
            tableContainer.set(EnchantmentSpaceKeys.STATE_CONTAINER.getKey(), PersistentDataType.TAG_CONTAINER,
                    stateContainer);
            ExtEnchantingTable extTable = new ExtEnchantingTable(loc);
            extTable.spawnParticles();
        }
    }
}
