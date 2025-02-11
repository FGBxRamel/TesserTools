package de.bydora.tesserTools.enchantment.listeners;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.blocks.ExtEnchantingTable;
import de.bydora.tesserTools.enchantment.enchantments.*;
import de.bydora.tesserTools.enchantment.exceptions.NotAnEnchantmentTableException;
import org.bukkit.*;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
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
            enchanted = applyCustomEnchantment(player, enchantStack, customEnch, chargeLevel);
        } else if (enchantment instanceof Enchantment vanillaEnch) {
            enchanted = applyVanillaEnchantment(player, enchantStack, vanillaEnch, chargeLevel);
        }

        if (enchanted) finalizeEnchantmentProcess(extTable, item, enchantItem);
    }

    private boolean applyCustomEnchantment(Player player, ItemStack enchantStack, CustomEnchantment<?> enchantment, int chargeLevel) {
        if (player.getLevel() >= 50 && chargeLevel > 0 && enchantment.canEnchantItem(enchantStack)) {
            player.setLevel(player.getLevel() - 6);
            enchantment.enchantItem(enchantStack, enchantment.getEnchantmentLevel(enchantStack) + 1);
            return true;
        }
        return false;
    }

    private boolean applyVanillaEnchantment(Player player, ItemStack enchantStack, Enchantment enchantment, int chargeLevel) {
        if (player.getLevel() >= 30 && enchantment.canEnchantItem(enchantStack)) {
            if (enchantStack.getType() == Material.BOOK || enchantStack.getType() == Material.ENCHANTED_BOOK){
                var meta = (EnchantmentStorageMeta) enchantStack.getItemMeta();
                meta.removeStoredEnchant(enchantment);
                meta.addStoredEnchant(enchantment, meta.getStoredEnchantLevel(enchantment) + 1,
                        true);
            } else {
                enchantStack.addUnsafeEnchantment(enchantment, enchantStack.getEnchantmentLevel(enchantment) + 1);
            }
            player.setLevel(player.getLevel() - 3);
            return true;
        }
        return false;
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
}
