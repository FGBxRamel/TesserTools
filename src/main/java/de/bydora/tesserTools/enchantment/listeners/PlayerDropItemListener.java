package de.bydora.tesserTools.enchantment.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Random;

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
                        if (enchantItem == null) {
                            event.getPlayer().sendMessage("Kein Item auf dem Tisch gefunden!");
                            Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                            return;
                        }
                        ItemStack enchantStack = enchantItem.getItemStack();
                        var enchantment = extTable.getEnchantment(quarzLocation);
                        var player = event.getPlayer();
                        var chargeLevel = extTable.getChargeLevel();
                        switch (enchantment) {
                            case null -> {
                                Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                                return;
                            }
                            case CustomEnchantment<?> customEnch -> {
                                if (player.getLevel() >= 50 && chargeLevel > 0) {
                                    player.setLevel(player.getLevel() - 6);
                                    customEnch.enchantItem(enchantStack,
                                            customEnch.getEnchantmentLevel(enchantStack) + 1);
                                }
                            }
                            case Enchantment vanillaEnch -> {
                                if (player.getLevel() >= 30) {
                                    player.setLevel(player.getLevel() - 3);
                                    enchantStack.addUnsafeEnchantment(vanillaEnch,
                                            enchantStack.getEnchantmentLevel(vanillaEnch) + 1);
                                }
                            }
                            default -> {}
                        }
                        extTable.setChargeLevel(chargeLevel - 1);
                        item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                        spawnEnchantParticles(extTable.getLocation().clone().add(0,1,0));
                        extTable.removeTextDisplays();
                        if (new Random().nextInt(100000) == 0) {
                            player.sendMessage("Glücksspiel kann süchtig machen! Infos unter www.bzga.de");
                        }


                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else {
                        metadata[0]++;
                    }
                }
            }, 0L, 5L);
        } else if (item.getItemStack().getType() == Material.END_CRYSTAL) {
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
                        if (!extTable.isValid()
                            || extTable.getChargeLevel() == 4
                        ) {
                            return;
                        }
                        int maxChargeAmount = 4 - extTable.getChargeLevel();
                        int itemAmount = item.getItemStack().getAmount();
                        if (itemAmount > maxChargeAmount) {
                            item.getItemStack().setAmount(itemAmount - maxChargeAmount);
                            extTable.setChargeLevel(extTable.getChargeLevel() + maxChargeAmount);
                        }
                        else {
                            item.remove();
                            extTable.setChargeLevel(extTable.getChargeLevel() + itemAmount);
                        }


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
                        if (!extTable.isValid()
                            || event.getPlayer().getLevel() < 30
                        ) {
                            return;
                        }
                        if (extTable.isBlocked()
                            && item.getItemStack().getType() == Material.BOOK
                        ) {
                            var items = item.getLocation().getNearbyEntitiesByType(Item.class, 1);
                            for (var item : items) {
                                if (item.getItemStack().getType() == Material.BOOK) {
                                    // Hier fusionieren, dann item löschen
                                }
                            }

                        }
                        else {
                            boolean includeCustom = (event.getPlayer().getLevel() >= 50 && extTable.getChargeLevel() > 0);
                            extTable.setBlocked(true);
                            extTable.startEnchanting(item.getItemStack(), includeCustom);
                        }

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

    /**
     * Spawns the enchanting particles on the given location
     * @param loc The location of the particles
     */
    private void spawnEnchantParticles(Location loc) {
        var particle = new ParticleBuilder(Particle.ENCHANT);
        particle.offset(.2,.2,.2);
        particle.location(loc);
        particle.receivers(160);
        particle.count(500);
        particle.spawn();
    }
}
