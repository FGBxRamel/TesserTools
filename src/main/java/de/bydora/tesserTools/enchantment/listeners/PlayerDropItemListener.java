package de.bydora.tesserTools.enchantment.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.*;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class PlayerDropItemListener implements Listener {

    private final static Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    @SuppressWarnings("rawtypes")
    private final static Map<String, CustomEnchantment> customEnchantments = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap();

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
                        var quarzLocation = item.getLocation().clone().add(0, -1, 0).toCenterLocation();
                        ExtEnchantingTable extTable = getExtTable(quarzLocation);
                        if (extTable == null) {
                            Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                            return;
                        }
                        Item enchantItem;
                        try {
                            enchantItem = extTable.getLocation().getNearbyEntitiesByType(
                                    Item.class, 1, 2, 1).iterator().next();
                        } catch (NoSuchElementException e) {
                            event.getPlayer().sendMessage("Kein Item auf dem Tisch gefunden!");
                            Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                            return;
                        }
                        ItemStack enchantStack = enchantItem.getItemStack();
                        var enchantment = extTable.getEnchantment(quarzLocation);
                        var player = event.getPlayer();
                        var chargeLevel = extTable.getChargeLevel();
                        boolean enchanted = false;
                        switch (enchantment) {
                            case CustomEnchantment<?> customEnch -> {
                                if (player.getLevel() >= 50
                                        && chargeLevel > 0
                                        && (customEnch.canEnchantItem(enchantStack)
                                        || enchantStack.getType() == Material.BOOK
                                        || enchantStack.getType() == Material.ENCHANTED_BOOK)
                                ) {
                                    enchanted = true;
                                    player.setLevel(player.getLevel() - 6);
                                    customEnch.enchantItem(enchantStack,
                                            customEnch.getEnchantmentLevel(enchantStack) + 1);
                                }
                            }
                            case Enchantment vanillaEnch -> {
                                if (player.getLevel() >= 30
                                        && (vanillaEnch.canEnchantItem(enchantStack)
                                        || enchantStack.getType() == Material.BOOK
                                        || enchantStack.getType() == Material.ENCHANTED_BOOK)
                                ) {
                                    enchanted = true;
                                    player.setLevel(player.getLevel() - 3);
                                    enchantStack.addUnsafeEnchantment(vanillaEnch,
                                            enchantStack.getEnchantmentLevel(vanillaEnch) + 1);
                                }
                            }
                            case null, default -> {
                                Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                                return;
                            }
                        }
                        if (!enchanted) {
                            Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                            return;
                        }
                        if (enchantStack.getType() == Material.BOOK) {
                            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                            enchantedBook.setItemMeta(enchantItem.getItemStack().getItemMeta());
                            enchantItem.setItemStack(enchantedBook);
                        }

                        item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                        spawnEnchantParticles(extTable.getLocation().clone().add(0, 1, 0));
                        extTable.clearEnchantments();
                        extTable.setChargeLevel(chargeLevel - 1);
                        extTable.removeTextDisplays();

                        if (new Random().nextInt(1000) == 0) {
                            player.sendMessage("Glücksspiel kann süchtig machen! Infos unter www.bzga.de");
                        }


                        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                    } else {
                        metadata[0]++;
                    }
                }
            }, 0L, 5L);
        }
        else if (item.getItemStack().getType() == Material.END_CRYSTAL) {
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
                        if (extTable.getChargeLevel() == 4
                        ) {
                            event.getPlayer().sendMessage("Bist du blind? (Tisch voll aufgeladen)");
                            return;
                        } else if (!extTable.isValid()) {
                            event.getPlayer().sendMessage("Weißt du wie ein Tisch aussieht? (Invalider Tisch)");
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
                        ) {
                            event.getPlayer().sendMessage("Weißt du wie ein Tisch aussieht? (Invalider Tisch)");
                            return;
                        } else if (event.getPlayer().getLevel() < 30) {
                            event.getPlayer().sendMessage("Geh im Sandkasten spielen! (Zu niedriges Level)");
                            return;
                        }

                        // If enchanted book was thrown on the table and the table is blocked
                        if (extTable.isBlocked()
                            && item.getItemStack().getType() == Material.ENCHANTED_BOOK
                        ) {
                            var existingItem = item.getLocation().getNearbyEntitiesByType(Item.class, 1)
                                    .iterator().next();
                            // Enchanted book on the table?
                            if (existingItem == null) {
                                Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
                                return;
                            } else if (existingItem.getItemStack().getType() != Material.ENCHANTED_BOOK) {
                                var bookMeta = (EnchantmentStorageMeta) item.getItemStack().getItemMeta();
                                existingItem.setItemStack(
                                        mergeVanillaEnchantments(bookMeta.getStoredEnchants(), existingItem.getItemStack().getEnchantments(),
                                        existingItem.getItemStack(),true)
                                        );
                                mergeCustomEnchantments(existingItem.getItemStack(), item.getItemStack());
                                item.remove();
                            } else if (existingItem.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                                var existingStack = existingItem.getItemStack();
                                var existingMeta = (EnchantmentStorageMeta) existingStack.getItemMeta();
                                var thrownStack = item.getItemStack();
                                var thrownMeta = (EnchantmentStorageMeta) thrownStack.getItemMeta();


                                // Put the vanilla enchantments on the book
                                var vanillaEnchThrown = thrownMeta.getStoredEnchants();
                                var vanillaEnchExisting = existingMeta.getStoredEnchants();
                                mergeVanillaEnchantments(vanillaEnchThrown, vanillaEnchExisting, thrownStack);

                                // Put the custom enchantments on the book
                                mergeCustomEnchantments(thrownStack, existingStack);

                                item.setItemStack(thrownStack);
                                existingItem.remove();
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

    /**
     * Enchant an item.
     * @param item The item to enchant
     * @param ench The enchantment
     * @param level The level of the enchantment
     */
    private void enchant(@NotNull ItemStack item, @NotNull Enchantment ench, @NotNull Integer level) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            addBookEnchantment(item, ench, level);
        } else {
            item.addUnsafeEnchantment(ench, level);
        }
    }

    /**
     * Enchant an item.
     * @param item The item to enchant
     * @param ench The enchantment
     * @param level The level of the enchantment
     */
    private void enchant(@NotNull ItemStack item, @NotNull CustomEnchantment ench, @NotNull Integer level) {
        ench.enchantItem(item, level);
    }

    /**
     * Adds an enchantment to a book.
     * @param item The book that should be enchanted
     * @param enchantment The enchantment to add
     * @param level Which level the enchantment has
     */
    private void addBookEnchantment(ItemStack item, Enchantment enchantment, int level){
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
        meta.removeStoredEnchant(enchantment);
        meta.addStoredEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    }

    /**
     * Takes two maps of enchantments with their level and puts them on the given ItemStack.<p />
     * It always takes the greater level. If the levels are equal and the enchantments max level allows it
     * the level will be incremented.
     * @param ench1 The first list
     * @param ench2 The second list
     * @param item The ItemStack the enchantments should go on
     * @return The enchanted item
     */
    private @NotNull ItemStack mergeVanillaEnchantments(
            @NotNull Map<Enchantment, Integer> ench1, @NotNull Map<Enchantment, Integer> ench2, @NotNull ItemStack item
    ) {
        return mergeVanillaEnchantments(ench1, ench2, item, false);
    }

    /**
     * Takes two maps of enchantments with their level and puts them on the given ItemStack.<p />
     * It always takes the greater level. If the levels are equal and the enchantments max level allows it
     * the level will be incremented.
     * @param ench1 The first map
     * @param ench2 The second map. If the item that should be enchanted is equal to where one of the maps came from,
     *      *              the respective list needs to be passed to this argument.
     * @param item The ItemStack the enchantments should go on
     * @param strict Whether it should check if the enchantments can be put on the item
     * @return The enchanted item
     */
    private @NotNull ItemStack mergeVanillaEnchantments(
            @NotNull Map<Enchantment, Integer> ench1, @NotNull Map<Enchantment, Integer> ench2, @NotNull ItemStack item,
            boolean strict
    ) {
        for (var ench : ench1.keySet()) {
            boolean conflicts = false;
            for (var itemEnch : item.getEnchantments().keySet()) {
                if (ench.conflictsWith(itemEnch)) {conflicts = true; break;}
            }
            if (conflicts && strict) {continue;}

            // Search for the highest level of the enchantments
            int level1 = ench1.get(ench);
            int level2 = Objects.requireNonNullElse(ench2.get(ench), 0);
            // If both lists have enchantment x and the second level is higher
            if (ench2.containsKey(ench)
                && level2 > level1
                && ench.getMaxLevel() > level2
            ) {
                enchant(item, ench, level2);
            } else if (
                    ench2.containsKey(ench)
                    && level2 == level1
                    && ench.getMaxLevel() > level2
            ) {
                enchant(item, ench, level2 + 1);
            } else if (
                    ench2.containsKey(ench)
                    && level2 == level1
                    && (ench == Enchantment.PROTECTION
                        || ench == Enchantment.SWIFT_SNEAK
                        || ench == Enchantment.UNBREAKING
                    )
                    && ench.getMaxLevel() > level2
            ) {
                CustomEnchantment customEnch;
                if (ench == Enchantment.PROTECTION) {
                    customEnch = new Protection();
                } else if (ench == Enchantment.SWIFT_SNEAK) {
                    customEnch = new SwiftSneak();
                } else {
                    customEnch = new Unbreaking();
                }

                if (customEnch.getMaxLevel() > level2) {
                    enchant(item, ench, level2 + 1);
                    enchant(item, customEnch, level2 + 1);
                }

            }
            // If not just use the one from the first map
            else if (ench.getMaxLevel() > level1) {
                enchant(item, ench, level1);
            }
        }
        return item;
    }

    /**
     * Takes the custom enchantments of two items and puts them on the first. <p />
     * It always takes the greater level. If the levels are equal and the enchantments max level allows it
     * the level will be incremented.
     *
     * @param item1 The first item
     * @param item2 The second item
     */
    private void mergeCustomEnchantments(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        for (var ench : customEnchantments.values()) {
            int existingLevel = ench.getEnchantmentLevel(item2);
            int thrownLevel = ench.getEnchantmentLevel(item1);
            if (existingLevel == thrownLevel
                && existingLevel > 0
                && ench.getMaxLevel() > existingLevel
            ) {
                ench.enchantItem(item1, existingLevel + 1);
                continue;
            }

            int level = Math.max(existingLevel, thrownLevel);
            if (level > 0) {
                ench.enchantItem(item1, level);
            }
        }
    }

}
