package de.bydora.tesserTools.enchantment.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.*;
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

@SuppressWarnings({"rawtypes", "UnusedReturnValue", "SameParameterValue"})
public class PlayerDropItemListener implements Listener {

    @SuppressWarnings("unused")
    private final static Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    private final static int reqLevelVanilla = 30; // How many levels are required to show Vanilla enchantments
    private final static int reqLevelCustom = 40; // How many levels are required to show Custom enchantments
    private final static int usedLevelVanilla = 3; // How many levels are used when enchanting Vanilla enchantments
    private final static int usedLevelCustom = 4; // How many levels are used when enchanting Custom enchantments

    private final static Map<String, CustomEnchantment> customEnchantments = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap();
    private final static Map<Enchantment, CustomEnchantment> enhVanillaEnchMap =  Map.of(
            Enchantment.PROTECTION, new Protection(),
            Enchantment.SWIFT_SNEAK, new SwiftSneak(),
            Enchantment.UNBREAKING, new Unbreaking(),
            Enchantment.PROJECTILE_PROTECTION, new ProjectileProtection(),
            Enchantment.THORNS, new Thorns(),
            Enchantment.FIRE_PROTECTION, new FireProtection(),
            Enchantment.BLAST_PROTECTION, new BlastProtection()
    );
    private final static Set<Class<? extends CustomEnchantment>> excludedEnchantments = Set.of(
            Protection.class,
            SwiftSneak.class,
            Unbreaking.class,
            ProjectileProtection.class,
            Thorns.class,
            FireProtection.class,
            BlastProtection.class
    );

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
        ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", event.getPlayer().locale());
        ExtEnchantingTable extTable = getQuarzTable(item.getLocation().clone().add(0, -1, 0));
        if (extTable == null) return;
        if (!extTable.isValid()) {
            event.getPlayer().sendMessage(l18.getString("invalidTable"));
            return;
        }

        Item enchantItem = getNearbyItem(extTable);
        if (enchantItem == null) {
            event.getPlayer().sendMessage(l18.getString("noItemOnTable"));
            return;
        }

        ItemStack enchantStack = enchantItem.getItemStack();
        Object enchantment = extTable.getEnchantment(item.getLocation());
        var player = event.getPlayer();
        var chargeLevel = extTable.getChargeLevel();

        boolean enchanted = false;
        ItemStack newStack = null;
        if (enchantment instanceof CustomEnchantment<?> customEnch) {
            newStack = applyCustomEnchantment(player, enchantStack, customEnch, chargeLevel, true,
                    customEnch.getEnchantmentLevel(enchantStack) + 1);
        } else if (enchantment instanceof Enchantment vanillaEnch) {
                newStack = applyVanillaEnchantment(player, enchantStack, vanillaEnch,
                    enchantStack.getEnchantmentLevel(vanillaEnch) + 1);
        }
        if (Objects.nonNull(newStack)) {
            enchanted = true;
            enchantItem.setItemStack(newStack);
        }
        if (enchanted) finalizeEnchantmentProcess(extTable, item, enchantItem);
    }

    private ItemStack applyCustomEnchantment(@Nullable Player player, ItemStack enchantStack,
                                           CustomEnchantment<?> enchantment, int chargeLevel, boolean changeLevel,
                                           int level
    ) {
        // Whether the player has enough level
        boolean hasLevel = player == null || player.getLevel() >= reqLevelCustom;
        // Change the players level if one exists and level should change
        if (player != null && changeLevel) {
            player.setLevel(player.getLevel() - usedLevelCustom);
        }

        if (hasLevel && chargeLevel > 0
            && (enchantment.canEnchantItem(enchantStack)
                || enchantStack.getType() == Material.ENCHANTED_BOOK
                || enchantStack.getType() == Material.BOOK
                )
        ) {
            enchantStack = enchantment.enchantItem(enchantStack, level);
            return enchantStack;
        }
        return null;
    }

    private ItemStack applyCustomEnchantment(ItemStack enchantStack, CustomEnchantment<?> enchantment, int chargeLevel,
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
        boolean hasLevel = player == null || player.getLevel() >= reqLevelVanilla;
        // Change the players level if one exists and level should change
        if (player != null && changeLevel) {
            player.setLevel(player.getLevel() - usedLevelVanilla);
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
            ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", event.getPlayer().locale());
            event.getPlayer().sendMessage(l18.getString("invalidTable"));
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
        ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", event.getPlayer().locale());
        if (extTable == null) {return;}
        else if (!extTable.isValid()) {
            event.getPlayer().sendMessage(l18.getString("invalidTable"));
            return;
        } else if (event.getPlayer().getLevel() < reqLevelVanilla) {
            event.getPlayer().sendMessage(l18.getString("insufficientLevel"));
            return;
        }

        var secondItem = getNearbyItem(extTable, item);
        if (secondItem != null
            && secondItem.getItemStack().getType() == item.getItemStack().getType()
            && (hasEnchantments(secondItem.getItemStack()) || hasEnchantments(item.getItemStack()))
        ) {
            var enchanted = mergeEnchantments(item, secondItem, true, event.getPlayer());
            if (enchanted) {
                extTable.clearEnchantments();
                item.remove();
            } else {
                event.getPlayer().sendMessage(l18.getString("failedEnchantGeneric"));
            }
        } else {
            extTable.setBlocked(true);
            extTable.startEnchanting(item.getItemStack(), event.getPlayer().getLevel() >= reqLevelCustom
                    && extTable.getChargeLevel() > 0, event.getPlayer().locale());
        }
    }

    private void processEnchantedBook(PlayerDropItemEvent event, Item book) {
        ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", event.getPlayer().locale());
        ExtEnchantingTable extTable = getExtTable(book.getLocation());
        if (extTable == null) {return;}
        else if (!extTable.isValid()) {
            event.getPlayer().sendMessage(l18.getString("invalidTable"));
            return;
        }

        var secondItem = getNearbyItem(extTable, book);
        if (secondItem == null) {
            return;
        }
        var enchanted = mergeEnchantments(book, secondItem, true, event.getPlayer());
        if (enchanted) {
            extTable.clearEnchantments();
            book.remove();
        } else {
            event.getPlayer().sendMessage(l18.getString("failedEnchantGeneric"));
        }
    }

    /**
     * Merges the enchantments of two items.<p />
     * It will merge the enchantments onto the second item.
     * @param item1 Item with enchantments
     * @param mergeItem Item with enchantments. All enchantments will be merged onto this
     * @param strict Whether conflicts, level and tool restrictions should be checked
     */
    private boolean mergeEnchantments(Item item1, Item mergeItem, boolean strict, Player player) {
        var item1Stack = item1.getItemStack();
        var mergeStack = mergeItem.getItemStack();
        var itemVanillaEnch = getVanillaEnchantments(item1Stack);
        var mergeVanillaEnch = getVanillaEnchantments(mergeStack);
        var newStack = new ItemStack(mergeStack);
        int usedLevel = 0;
        Map<Enchantment, Integer> vanillas = new HashMap<>();

        for (var ench : itemVanillaEnch.keySet()) {
            boolean conflicts = false;
            // Check for conflicts
            if (strict) {
                if (!ench.canEnchantItem(mergeStack) && mergeStack.getType() != Material.ENCHANTED_BOOK) {
                    conflicts = true;
                } else {
                    for (var itemEnch : mergeVanillaEnch.keySet()) {
                        if (ench.conflictsWith(itemEnch) && ench != itemEnch) {
                            conflicts = true;
                            break;
                        }
                    }
                }
            }
            if (conflicts) {return false;}

            CustomEnchantment<?> customEnch = enhVanillaEnchMap.get(ench);
            // Search for the highest level of the enchantments
            int item1Level = itemVanillaEnch.get(ench);
            int mergeLevel = Objects.requireNonNullElse(mergeVanillaEnch.get(ench), 0);

            int customLevel = mergeLevel;

            // If it's an "advanced vanilla"
            if (Objects.nonNull(customEnch)) {
                // Level detection
                if (mergeLevel == item1Level && customEnch.getMaxLevel() > mergeLevel) {
                    customLevel = mergeLevel + 1;
                    usedLevel += customLevel > ench.getMaxLevel() ? usedLevelCustom : 0;
                    vanillas.put(ench, customLevel);
                }
                else if (customEnch.getMaxLevel() >= item1Level) {
                    customLevel = item1Level;
                    usedLevel += customLevel > ench.getMaxLevel() ? usedLevelCustom : 0;
                    vanillas.put(ench, customLevel);
                }
            }
            else if (mergeLevel == item1Level
                    && ench.getMaxLevel() > mergeLevel
            ) {
                usedLevel += usedLevelVanilla;
                vanillas.put(ench, mergeLevel + 1);
            }
            // If not just use the higher one
            else if (ench.getMaxLevel() >= item1Level
                    && ench.getMaxLevel() >= mergeLevel
            ) {
                usedLevel += usedLevelVanilla;
                vanillas.put(ench, Math.max(mergeLevel, item1Level));
            }


            if (customEnch != null) {
                applyCustomEnchantment(newStack, customEnch, 4, customLevel);
            }
        }
        for (var van : vanillas.keySet()) {
            applyVanillaEnchantment(newStack, van, vanillas.get(van));
        }

        // Customs
        for (var ench : customEnchantments.values()) {
            if (excludedEnchantments.contains(ench.getClass())) {continue;}

            if (!ench.canEnchantItem(mergeStack)
                && mergeStack.getType() != Material.ENCHANTED_BOOK
                && ench.getEnchantmentLevel(item1Stack) > 0
            ) {
                return false;
            }
            var mergeLevel = ench.getEnchantmentLevel(mergeStack);
            var item1Level = ench.getEnchantmentLevel(item1Stack);
            if (mergeLevel == item1Level
                    && mergeLevel > 0
                    && ench.getMaxLevel() > mergeLevel
            ) {
                usedLevel += usedLevelCustom;
                applyCustomEnchantment(newStack, ench, 4, mergeLevel + 1);
                continue;
            }

            if (item1Level > mergeLevel) {
                usedLevel += usedLevelCustom;
                applyCustomEnchantment(newStack, ench, 4, item1Level);
            }
        }
        if (player.getLevel() >= usedLevel) {
            mergeItem.setItemStack(newStack);
            player.setLevel(player.getLevel() - usedLevel);
            return true;
        } else {
            return false;
        }
    }

    private void finalizeEnchantmentProcess(ExtEnchantingTable extTable, Item item, Item enchantItem) {
        if (enchantItem.getItemStack().getType() == Material.BOOK) {
//            var enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
//            enchantedBook.setItemMeta(enchantItem.getItemStack().getItemMeta());
//            enchantItem.setItemStack(enchantedBook);
        }
        item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
        spawnEnchantParticles(extTable.getLocation().add(0, 1, 0));
        extTable.clearEnchantments();
        extTable.setChargeLevel(extTable.getChargeLevel() - 1);
    }

    /**
     * Gets the item near (on) the given enchanting table
     * @param extTable The table on which to check
     * @return The found item, or null
     */
    private @Nullable Item getNearbyItem(ExtEnchantingTable extTable) {
        return extTable.getLocation()
                .getNearbyEntitiesByType(Item.class, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the item near (on) the given enchanting table
     * @param extTable The table on which to check
     * @param excludeItem Excludes this item from the search, never giving back this one
     * @return The found item, or null
     */
    private @Nullable Item getNearbyItem(ExtEnchantingTable extTable, Item excludeItem) {
        var items = getNearbyItems(extTable);
        for (Item item : items) {
            if (!item.equals(excludeItem)) {
                return item;
            }
        }
        return null;
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

    /**
     * Checks whether an item has any enchantments or not
     * @param item The item to check
     * @return Whether it has a vanilla or custom enchantment
     */
    private boolean hasEnchantments(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            var meta = (EnchantmentStorageMeta) item.getItemMeta();
            if (!meta.getStoredEnchants().isEmpty()) {return true;}
        } else if (!item.getEnchantments().isEmpty()) {
            return true;
        }

        for (var ench : customEnchantments.values()) {
            if (ench.getEnchantmentLevel(item) > 0) {
                return true;
            }
        }

        return false;
    }
}
