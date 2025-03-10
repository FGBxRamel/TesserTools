package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AreaBlockBreaker;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AreaBreak extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:flächenabbau";
    private final static String displayName = "Flächenabbau";
    private final static int maxLevel = 2;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems =
            MaterialArrayMerger.merge(EquipmentGroups.PICKAXES, EquipmentGroups.SHOVELS);
    private final static Material[] affectedBlocks = new Material[] {
            Material.STONE, Material.DEEPSLATE, Material.DIRT, Material.GRAVEL, Material.DIORITE, Material.ANDESITE,
            Material.GRANITE, Material.BASALT, Material.NETHERRACK, Material.BLACKSTONE, Material.SOUL_SAND,
            Material.SAND, Material.END_STONE, Material.TUFF, Material.CALCITE, Material.SOUL_SOIL, Material.CLAY,
            Material.GRASS_BLOCK, Material.MUD, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
            Material.RED_SAND, Material.SANDSTONE, Material.RED_SANDSTONE,
            Material.COBBLED_DEEPSLATE, Material.CRIMSON_STEM, Material.WARPED_STEM,

            Material.TERRACOTTA, Material.BLACK_TERRACOTTA, Material.BLUE_TERRACOTTA, Material.BROWN_TERRACOTTA,
            Material.CYAN_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.LIME_TERRACOTTA,
            Material.RED_TERRACOTTA, Material.WHITE_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.PURPLE_TERRACOTTA
    };

    public AreaBreak() {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (getEnchantmentLevel(item) > 0
            && Arrays.stream(affectedBlocks).toList().contains(event.getBlock().getType())
        ) {
            breakArea(getEnchantmentLevel(item) == 2, event.getPlayer(), event.getBlock(), item);
        }
    }


    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_AREA_BREAK.getKey();
    }

    /**
     * Returns the blocks that are affected by this enchantment.
     * Primarily used to clone the blocks to {@link DeepMine}.
     * @return The affected blocks
     */
    public static @NotNull Material[] getAffectedBlocks() {
        return affectedBlocks;
    }

    /**
     * Breaks an area originating from the given block.<p>
     * The given block will *not* be broken.
     * @param bigArea Whether it's 5x5 or 3x3
     * @param player The player executing the break
     * @param block The block that the break will originate from
     */
    public static void breakArea(boolean bigArea, @NotNull Player player, @NotNull Block block,
                                 @NotNull ItemStack item) {
        AreaBlockBreaker breaker = new AreaBlockBreaker(Set.of(affectedBlocks), bigArea);
        var pitch = Math.abs(player.getEyeLocation().getPitch());
        List<Location> locations = breaker.findDirectNeighbors(player, block, pitch >= 30);
        for (Location location : locations) {
            location.getBlock().breakNaturally(item);
        }
    }

}
