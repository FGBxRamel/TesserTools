package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import org.bukkit.Bukkit;
import org.bukkit.block.EnchantingTable;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        final int[] metadata = {0, 0}; // I hate Java; first is the number of tries, second a "bool" if it landed or not

        // Try every 5 Ticks if the item has landed
        Bukkit.getScheduler().runTaskTimer(TesserTools.getPlugin(TesserTools.class), new Runnable() {
            @Override
            public void run() {
                if (metadata[0] >= 3) {Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));}
                else if (item.isOnGround()
                        && item.isValid()
                        && item.getLocation().getBlock().getState() instanceof EnchantingTable table
                ) {
                    ExtEnchantingTable extTable = new ExtEnchantingTable(item.getLocation());
                    extTable.setBlocked(true);
                    extTable.spawnParticles();

                    Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                }
                else {
                    metadata[0]++;
                }
            }
        }, 0L, 5L);
    }
}
