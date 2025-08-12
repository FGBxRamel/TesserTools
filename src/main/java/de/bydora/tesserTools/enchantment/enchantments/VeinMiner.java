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

import java.util.Arrays;
import java.util.List;

public class VeinMiner extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:aderabbau";
    private final static String displayName = "Aderabbau";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static NamespacedKey key = EnchantmentSpaceKeys.ENCH_VEIN_MINER.getKey();
    private final static Material[] enchantableItems = EquipmentGroups.PICKAXES;
    private final static Material[] ores = new Material[] {
            Material.COAL_ORE, Material.COPPER_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.ANCIENT_DEBRIS
    };

    public VeinMiner() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, key);
    }

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
}
