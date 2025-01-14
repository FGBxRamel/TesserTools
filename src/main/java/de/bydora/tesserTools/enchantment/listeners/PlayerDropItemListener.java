package de.bydora.tesserTools.enchantment.listeners;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PlayerDropItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        final int[] metadata = {0, 0}; // I hate Java; first is the number of tries, second a "bool" if it landed or not

        if (item.getItemStack().getType() == Material.LAPIS_LAZULI) {
            Bukkit.getScheduler().runTaskTimer(TesserTools.getPlugin(TesserTools.class), new Runnable() {
                @Override
                public void run() {
                    if (metadata[0] >= 3) {
                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else if (item.isOnGround()
                            && item.isValid()
                            && item.getLocation().clone().add(0,-1,0).getBlock().getType() == Material.CHISELED_QUARTZ_BLOCK
                    ) {
                        var quarzLocation = item.getLocation().clone().add(0, -1,0).toCenterLocation();
                        ExtEnchantingTable extTable = getExtTable(quarzLocation);
                        if (extTable == null) {
                            Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                            return;
                        }
                        Item enchantItem = extTable.getLocation().getNearbyEntitiesByType(
                                Item.class, 1,2,1).iterator().next();
                        ItemStack enchantStack = enchantItem.getItemStack();
                        var enchantment = extTable.getEnchantment(quarzLocation);
                        switch (enchantment) {
                            case null -> {
                                Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                                return;
                            }
                            case CustomEnchantment<?> customEnch -> {
                                customEnch.enchantItem(enchantStack,
                                        customEnch.getEnchantmentLevel(enchantStack) + 1);
                            }
                            case Enchantment vanillaEnch -> {
                                enchantStack.addUnsafeEnchantment(vanillaEnch,
                                        enchantStack.getEnchantmentLevel(vanillaEnch) + 1);
                            }
                            default -> {
                            }
                        }
                        item.remove();
                        extTable.removeDisplays();


                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else {
                        metadata[0]++;
                    }
                }
            }, 0L, 5L);
        }
        else {
            // Try every 5 Ticks if the item has landed
            Bukkit.getScheduler().runTaskTimer(TesserTools.getPlugin(TesserTools.class), new Runnable() {
                @Override
                public void run() {
                    if (metadata[0] >= 3) {
                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else if (item.isOnGround()
                            && item.isValid()
                            && item.getLocation().getBlock().getState() instanceof EnchantingTable table
                    ) {
                        ExtEnchantingTable extTable = new ExtEnchantingTable(item.getLocation());
                        if (extTable.isBlocked() || !extTable.isValid()) {
                            return;
                        }
                        extTable.setBlocked(true);
                        extTable.startEnchanting(item.getItemStack());

                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else {
                        metadata[0]++;
                    }
                }
            }, 0L, 5L);
        }
    }

    /**
     * Checks if the quarz block given is part of an enchanting table.
     * @param loc The location of the quarz block
     * @return An instance of an {@link ExtEnchantingTable} or null
     */
    private @Nullable ExtEnchantingTable getExtTable(Location loc) {
        if (loc.clone().add(-4,0,-4).getBlock().getType() == Material.ENCHANTING_TABLE)
        {
            return new ExtEnchantingTable(loc.clone().add(-4,0,-4));
        } else if (loc.clone().add(4,0,-4).getBlock().getType() == Material.ENCHANTING_TABLE) {
            return new ExtEnchantingTable(loc.clone().add(4,0,-4));
        } else if (loc.clone().add(4,0,4).getBlock().getType() == Material.ENCHANTING_TABLE) {
            return new ExtEnchantingTable(loc.clone().add(4,0,4));
        } else if (loc.clone().add(-4,0,4).getBlock().getType() == Material.ENCHANTING_TABLE) {
            return new ExtEnchantingTable(loc.clone().add(-4,0,4));
        } else {
            return null;
        }
    }
}
