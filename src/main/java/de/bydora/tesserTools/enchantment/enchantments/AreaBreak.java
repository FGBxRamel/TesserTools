package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AreaBlockBreaker;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AreaBreak extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:flächenabbau";
    private final static String displayName = "3x3 Abbau";
    private final static int maxLevel = 1;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems =
            MaterialArrayMerger.merge(EquipmentGroups.PICKAXES, EquipmentGroups.SHOVELS);
    private final static Material[] affectedBlocks = new Material[] {
            Material.STONE, Material.DEEPSLATE, Material.DIRT, Material.GRAVEL, Material.DIORITE, Material.ANDESITE,
            Material.GRANITE, Material.BASALT, Material.NETHERRACK, Material.BLACKSTONE, Material.SOUL_SAND,
            Material.SAND, Material.END_STONE, Material.TUFF, Material.CALCITE, Material.SOUL_SOIL, Material.CLAY,
            Material.GRASS_BLOCK, Material.MUD
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
            AreaBlockBreaker breaker = new AreaBlockBreaker(Set.of(affectedBlocks));
            List<Location> horizontalLocations = breaker.findDirectNeighbors(event.getPlayer(), event.getBlock(), true);
            List<Location> verticalLocations = breaker.findDirectNeighbors(event.getPlayer(), event.getBlock(), false);
            List<Location> locations = horizontalLocations.size() > verticalLocations.size()
                    ? horizontalLocations : verticalLocations;
            for (Location location : locations) {
                location.getBlock().breakNaturally(item);
            }
        }
    }


    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_AREA_BREAK.getKey();
    }

}
