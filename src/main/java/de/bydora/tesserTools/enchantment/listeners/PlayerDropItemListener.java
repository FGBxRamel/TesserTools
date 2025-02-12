package de.bydora.tesserTools.enchantment.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import de.bydora.tesserTools.enchantment.enchantments.Protection;
import de.bydora.tesserTools.enchantment.enchantments.SwiftSneak;
import de.bydora.tesserTools.enchantment.enchantments.Unbreaking;
import de.bydora.tesserTools.enchantment.exceptions.NotAnEnchantmentTableException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class PlayerDropItemListener implements Listener {

    private final static Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();
    private final static Map<String, CustomEnchantment> customEnchantments = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        switch (item.getItemStack().getType()) {
            case LAPIS_LAZULI -> handleLapisDrop(event, item);
            case END_CRYSTAL -> handleEndCrystalDrop(event, item);
            case ENCHANTED_BOOK -> handleEnchantedBookDrop(event, item);
            default -> handleGenericItemDrop(event, item);
        }
    }

    private void handleLapisDrop(PlayerDropItemEvent event, Item item) {
        scheduleItemCheck(item, () -> {
            if (isOnQuartzBlock(item)) {
                processLapisEnchant(event, item);
                cancelTasks();
            }
        });
    }

    private void handleEndCrystalDrop(PlayerDropItemEvent event, Item item) {
        scheduleItemCheck(item, () -> {
            if (isOnEnchantingTable(item)) {
                chargeEnchantingTable(event, item);
                cancelTasks();
            }
        });
    }

    private void handleEnchantedBookDrop(PlayerDropItemEvent event, Item item) {
        scheduleItemCheck(item, () -> {
            if (isOnEnchantingTable(item)) {
                processEnchantedBook(event, item);
                cancelTasks();
            }
        });
    }

    private void handleGenericItemDrop(PlayerDropItemEvent event, Item item) {
        scheduleItemCheck(item, () -> {
            if (isOnEnchantingTable(item)) {
                processEnchantment(event, item);
                cancelTasks();
            }
        });
    }

    private void scheduleItemCheck(Item item, Runnable action) {
        Bukkit.getScheduler().runTaskTimer(TesserTools.getPlugin(TesserTools.class), new Runnable() {
            int attempts = 0;
            @Override
            public void run() {
                if (attempts++ >= 3 || !item.isValid()) {
                    cancelTasks();

             } else {
                    action.run();
                }
         }
     }, 0L, 5L);
    }

    private boolean isOnQuartzBlock(Item item) {
        return item.isOnGround()
                && item.getLocation().clone().add(0, -1, 0).getBlock()
                    .getType() == Material.CHISELED_QUARTZ_BLOCK;
    }

    private boolean isOnEnchantingTable(Item item) {
        return item.isOnGround() && item.getLocation().getBlock().getState() instanceof EnchantingTable;
    }

    private void processLapisEnchant(PlayerDropItemEvent event, Item item) {
        ExtEnchantingTable extTable = getQuarzTable(item.getLocation().clone().add(0, -1, 0));
        if (extTable == null) return;

        Item enchantItem = getNearbyItem(extTable);
        if (enchantItem == null) {
            event.getPlayer().sendMessage("Kein Item auf dem Tisch gefunden!");
            return;
        }

        ItemStack enchantStack = enchantItem.getItemStack();
        Object enchantment = extTable.getEnchantment(item.getLocation());
        var player = event.getPlayer();
        var chargeLevel = extTable.getChargeLevel();

        boolean enchanted = false;
        if (enchantment instanceof CustomEnchantment<?> customEnch) {
            enchanted = applyCustomEnchantment(player, enchantStack, customEnch, chargeLevel, true,
                    customEnch.getEnchantmentLevel(enchantStack) + 1);
        } else if (enchantment instanceof Enchantment vanillaEnch) {
            enchanted = Objects.nonNull(applyVanillaEnchantment(player, enchantStack, vanillaEnch,
                    enchantStack.getEnchantmentLevel(vanillaEnch) + 1));
        }
        if (enchanted) finalizeEnchantmentProcess(extTable, item, enchantItem);
    }

    private boolean applyCustomEnchantment(@Nullable Player player, ItemStack enchantStack,
                                           CustomEnchantment<?> enchantment, int chargeLevel, boolean changeLevel,
                                           int level
    ) {
        // Whether the player has enough level
        boolean hasLevel = player == null || player.getLevel() >= 50;
        // Change the players level if one exists and level should change
        if (player != null && changeLevel) {
            player.setLevel(player.getLevel() - 6);
        }

        if (hasLevel && chargeLevel > 0
            && (enchantment.canEnchantItem(enchantStack)
                || enchantStack.getType() == Material.ENCHANTED_BOOK
                || enchantStack.getType() == Material.BOOK
                )
        ) {
            enchantment.enchantItem(enchantStack, level);
            return true;
        }
        return false;
    }

    private boolean applyCustomEnchantment(ItemStack enchantStack, CustomEnchantment<?> enchantment, int chargeLevel,
                                           int level) {
        return applyCustomEnchantment(null, enchantStack, enchantment, chargeLevel, false, level);
    }

    /**
     * Applies an {@link Enchantment} to an {@link ItemStack}.
     * @param player The player doing the action
     * @param enchantStack The item to be enchanted
     * @param enchantment The enchantment to put on the item
     * @param changeLevel Whether to change the players level
     * @param level The level of the enchantment
     * @return The enchanted ItemStack; null if it didn't enchant
     */
    private @Nullable ItemStack applyVanillaEnchantment(@Nullable Player player, ItemStack enchantStack, Enchantment enchantment,
                                            boolean changeLevel, Integer level) {
        // Whether the player has enough level
        boolean hasLevel = player == null || player.getLevel() >= 30;
        // Change the players level if one exists and level should change
        if (player != null && changeLevel) {
            player.setLevel(player.getLevel() - 3);
        }
        if (!hasLevel) {return null;}
        if (enchantStack.getType() == Material.BOOK) {
            enchantStack = new ItemStack(Material.ENCHANTED_BOOK);
        }

        if (enchantStack.getType() == Material.ENCHANTED_BOOK){
            var meta = (EnchantmentStorageMeta) enchantStack.getItemMeta();
            meta.removeStoredEnchant(enchantment);
            meta.addStoredEnchant(enchantment, level, true);
            enchantStack.setItemMeta(meta);
            return enchantStack;
        } else if (enchantment.canEnchantItem(enchantStack)) {
            enchantStack.addUnsafeEnchantment(enchantment, level);
            return enchantStack;
        }
        return null;
    }

    /**
     * Applies an {@link Enchantment} to an {@link ItemStack}, removing the appropriate levels from the player.
     * @param player The player doing the action
     * @param enchantStack The item to be enchanted
     * @param enchantment The enchantment to put on the item
     * @return The enchanted ItemStack; null if it didn't enchant
     */
    private @Nullable ItemStack applyVanillaEnchantment(Player player, ItemStack enchantStack, Enchantment enchantment,
                                            @Nullable Integer level) {
        return applyVanillaEnchantment(player, enchantStack, enchantment, true, level);
    }

    /**
     * Applies an {@link Enchantment} to an {@link ItemStack}.
     * @param enchantStack The item to be enchanted
     * @param enchantment The enchantment to put on the item
     * @return The enchanted ItemStack; null if it didn't enchant
     */
    private @Nullable ItemStack applyVanillaEnchantment(ItemStack enchantStack, Enchantment enchantment, @Nullable Integer level) {
        return applyVanillaEnchantment(null, enchantStack, enchantment, false, level);
    }

    private void chargeEnchantingTable(PlayerDropItemEvent event, Item item) {
        ExtEnchantingTable extTable = getExtTable(item.getLocation());
        if (extTable == null) {return;}
        else if (!extTable.isValid()) {
            event.getPlayer().sendMessage("Ungültiger Verzauberungstisch!");
            return;
        }
        int maxChargeAmount = 4 - extTable.getChargeLevel();
        int itemAmount = item.getItemStack().getAmount();
        if (itemAmount > maxChargeAmount) {
            item.getItemStack().setAmount(itemAmount - maxChargeAmount);
            extTable.setChargeLevel(extTable.getChargeLevel() + maxChargeAmount);
        } else {
            item.remove();
            extTable.setChargeLevel(extTable.getChargeLevel() + itemAmount);
        }
    }

    private void processEnchantment(PlayerDropItemEvent event, Item item) {
        ExtEnchantingTable extTable = getExtTable(item.getLocation());
        if (extTable == null) {return;}
        else if (!extTable.isValid() || event.getPlayer().getLevel() < 30) {
            event.getPlayer().sendMessage("Zu niedriges Level oder ungültiger Tisch!");
            return;
        }
        extTable.setBlocked(true);
        extTable.startEnchanting(item.getItemStack(), event.getPlayer().getLevel() >= 50
                && extTable.getChargeLevel() > 0);
    }

    private void processEnchantedBook(PlayerDropItemEvent event, Item book) {
        ExtEnchantingTable extTable = getExtTable(book.getLocation());
        if (extTable == null) {return;}
        else if (!extTable.isValid()) {
            event.getPlayer().sendMessage("Ungültiger Verzauberungstisch!");
            return;
        }

        var items = getNearbyItems(extTable);
        Item secondItem = null;
        for (Item item : items) {
            if (!item.equals(book)) {
                secondItem = item;
            }
        }
        if (secondItem == null) {
            return;
        }
        mergeEnchantments(book, secondItem, false, event.getPlayer());
        book.remove();
    }

    /**
     * Merges the enchantments of two items.<p />
     * It will merge the enchantments onto the second item.
     * @param item1 Item with enchantments
     * @param mergeItem Item with enchantments. All enchantments will be merged onto this
     * @param strict Whether conflicts, level and tool restrictions should be checked
     */
    private void mergeEnchantments(Item item1, Item mergeItem, boolean strict, Player player) {
        var item1Stack = item1.getItemStack();
        var mergeStack = mergeItem.getItemStack();
        var itemVanillaEnch = getVanillaEnchantments(item1Stack);
        var mergeVanillaEnch = getVanillaEnchantments(mergeStack);

        for (var ench : itemVanillaEnch.keySet()) {
            boolean conflicts = false;
            // Check for conflicts
            if (strict) {
                for (var itemEnch : mergeVanillaEnch.keySet()) {
                    if (ench.conflictsWith(itemEnch) && ench.canEnchantItem(mergeStack)) {
                        conflicts = true;
                        break;
                    }
                }
            }
            if (conflicts) {continue;}

            // Search for the highest level of the enchantments
            int item1Level = itemVanillaEnch.get(ench);
            int mergeLevel = Objects.requireNonNullElse(mergeVanillaEnch.get(ench), 0);
            // If both lists have enchantment x and the second level is higher
            if (mergeLevel > item1Level
                    && ench.getMaxLevel() >= mergeLevel
            ) {
                applyVanillaEnchantment(player, mergeStack, ench, true, mergeLevel);
            } else if (mergeLevel == item1Level // If level is equal, and it's one of the "advanced vanillas"
                    && (ench == Enchantment.PROTECTION
                    || ench == Enchantment.SWIFT_SNEAK
                    || ench == Enchantment.UNBREAKING
                    )
            ) {
                CustomEnchantment<?> customEnch;
                if (ench == Enchantment.PROTECTION) {
                    customEnch = new Protection();
                } else if (ench == Enchantment.SWIFT_SNEAK) {
                    customEnch = new SwiftSneak();
                } else {
                    customEnch = new Unbreaking();
                }

                if (customEnch.getMaxLevel() > mergeLevel) {
                    applyVanillaEnchantment(player, mergeStack, ench, true, mergeLevel + 1);
                    applyCustomEnchantment(player, mergeStack, customEnch, 4, true, mergeLevel + 1);
                }

            } else if (mergeLevel == item1Level
                    && ench.getMaxLevel() > mergeLevel
            ) {
                applyVanillaEnchantment(player, mergeStack, ench, true, mergeLevel + 1);
            }
            // If not just use the one from the first map
            else if (ench.getMaxLevel() >= item1Level) {
                applyVanillaEnchantment(player, mergeStack, ench, true, item1Level);
            }
        }
        mergeItem.setItemStack(mergeStack);

        // Customs
        for (var ench : customEnchantments.values()) {
            var mergeLevel = ench.getEnchantmentLevel(mergeStack);
            var item1Level = ench.getEnchantmentLevel(item1Stack);
            if (mergeLevel == item1Level
                    && mergeLevel > 0
                    && ench.getMaxLevel() > mergeLevel
            ) {
                applyCustomEnchantment(player, mergeStack, ench, 4, true, mergeLevel + 1);
                continue;
            }

            int level = Math.max(mergeLevel, item1Level);
            if (level > 0) {
                applyCustomEnchantment(player, mergeStack, ench, 4, true, level);
            }
        }
        mergeItem.setItemStack(mergeStack);
    }

    private void finalizeEnchantmentProcess(ExtEnchantingTable extTable, Item item, Item enchantItem) {
        if (enchantItem.getItemStack().getType() == Material.BOOK) {
            var enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            enchantedBook.setItemMeta(enchantItem.getItemStack().getItemMeta());
            enchantItem.setItemStack(enchantedBook);
        }
        item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
        spawnEnchantParticles(extTable.getLocation().add(0, 1, 0));
        extTable.clearEnchantments();
        extTable.setChargeLevel(extTable.getChargeLevel() - 1);
    }

    private Item getNearbyItem(ExtEnchantingTable extTable) {
        return extTable.getLocation()
                .getNearbyEntitiesByType(Item.class, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Collection<Item> getNearbyItems(ExtEnchantingTable extTable) {
        return extTable.getLocation()
                .getNearbyEntitiesByType(Item.class, 1);
    }

    private void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(TesserTools.getPlugin(TesserTools.class));
    }

    private void spawnEnchantParticles(Location loc) {
        new ParticleBuilder(Particle.ENCHANT).offset(.2,.2,.2).location(loc).count(500).spawn();
    }

    private ExtEnchantingTable getExtTable(Location loc) {
        try {
            return new ExtEnchantingTable(loc);
        } catch (NotAnEnchantmentTableException e) {
            return null;
        }
    }

    /**
     * Checks if the quarz block given is part of an enchanting table.
     * @param loc The location of the quarz block
     * @return An instance of an {@link ExtEnchantingTable} or null
     */
    private @Nullable ExtEnchantingTable getQuarzTable(Location loc) {
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

    private Map<Enchantment, Integer> getVanillaEnchantments(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK
            || item.getType() == Material.BOOK
        ) {
            var meta = (EnchantmentStorageMeta) item.getItemMeta();
            return meta.getStoredEnchants();
        } else {
            return item.getEnchantments();
        }
    }
}
