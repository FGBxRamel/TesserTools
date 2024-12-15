package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.WoodBlockBreaker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CustomEnchAbholzung implements CustomEnchantment {

    private final static String id = "tessertools:abholzung";
    private final static String displayName = "Abholzung";
    private final static int maxLevel = 3;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.AXES;
    private final static Material[] woodTypes = new Material[] {
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.DARK_OAK_LOG,
            Material.PALE_OAK_LOG, Material.OAK_LOG, Material.JUNGLE_LOG, Material.MANGROVE_LOG,
            Material.SPRUCE_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM
    };

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final int level = getEnchantmentLevel(item);
        if (level != 0
            && canEnchantItem(item)
            && Arrays.stream(woodTypes).toList().contains(event.getBlock().getType())
        ) {
            event.setCancelled(true);
            final int logAmount = 5 * level;
            final WoodBlockBreaker breaker = new WoodBlockBreaker(woodTypes, logAmount);
            List<Location> blocksToBreak = breaker.findConnectedWoodBlocks(event.getBlock());

            for (Location loc : blocksToBreak) {
                loc.getBlock().breakNaturally(item);
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
        return EnchantmentSpaceKeys.ENCH_ABHOLZUNG.getKey();
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
