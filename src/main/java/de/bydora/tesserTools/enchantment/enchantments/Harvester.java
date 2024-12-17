package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

public class Harvester implements CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:ernter";
    private final static String displayName = "Ernter";
    private final static int maxLevel = 1;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.HOES;
    private final static Map<Material, Material> cropSeedMapping = Map.ofEntries(
            entry(Material.WHEAT, Material.WHEAT_SEEDS),
            entry(Material.BEETROOTS, Material.BEETROOT_SEEDS),
            entry(Material.POTATOES, Material.POTATO),
            entry(Material.CARROTS, Material.CARROT)
    );

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(BlockBreakEvent event) {
        final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if ( Arrays.stream(enchantableItems).toList().contains(item.getType()) ) {
            if (cropSeedMapping.containsKey(event.getBlock().getType())
                && getEnchantmentLevel(item) > 0
            ) {
                Material removedTypeSeed = cropSeedMapping.get(event.getBlock().getType());
                if (event.getPlayer().getInventory().contains(removedTypeSeed)) {
                    ItemStack seed = new ItemStack(removedTypeSeed, 1);
                    Map<Integer, ItemStack> notRemoved = event.getPlayer().getInventory().removeItemAnySlot(seed);
                    if (notRemoved.isEmpty()) {
                        Material crop = event.getBlock().getType();
                        BukkitScheduler scheduler = event.getPlayer().getServer().getScheduler();
                        scheduler.runTaskLater(TesserTools.getPlugin(TesserTools.class), () -> {
                            event.getBlock().setType(crop);
                                }, 1);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull String getID() {
        return id;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return startLevel;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return Arrays.stream(enchantableItems).toList().contains(item.getType());
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_HARVESTER.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER, 0);
    }

    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)) {return false;}
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        return level == container.get(getSaveKey(), PersistentDataType.INTEGER);
    }
}
