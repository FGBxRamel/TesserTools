package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import org.bukkit.Location;
import org.bukkit.block.EnchantingTable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class BlockPlaceListener implements Listener {

    private final Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getState() instanceof EnchantingTable table) {

            Location loc = event.getBlockPlaced().getLocation();
            int[] coordinates = new int[]{
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ()
            };

            Plugin plugin = TesserTools.getPlugin(TesserTools.class);
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
