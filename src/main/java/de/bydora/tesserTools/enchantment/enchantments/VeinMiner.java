package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AdjacentBlockFinder;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class VeinMiner implements CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:aderabbau";
    private final static String displayName = "Aderabbau";
    private final static int maxLevel = 3;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.PICKAXES;
    private final static Material[] ores = new Material[] {
            Material.COAL_ORE, Material.COPPER_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.ANCIENT_DEBRIS
    };

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(BlockBreakEvent event) {
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        int level = getEnchantmentLevel(itemInHand);
        if (level == 0
            || !Arrays.stream(ores).toList().contains(event.getBlock().getType())
        ) {return;}
        AdjacentBlockFinder finder = new AdjacentBlockFinder(ores, level * 5);
        List<Location> blocksToBreak = finder.findConnectedBlocks(event.getBlock());
        for (Location location : blocksToBreak) {
            location.getBlock().breakNaturally(itemInHand, true, true);
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
        return EnchantmentSpaceKeys.ENCH_VEIN_MINER.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        try {
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER, 0);
        } catch (NullPointerException e) {
            return 0;
        }
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
