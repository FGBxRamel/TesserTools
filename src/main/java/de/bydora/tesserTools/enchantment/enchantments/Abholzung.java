package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AdjacentBlockFinder;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class Abholzung extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:abholzung";
    private final static String displayName = "Abholzung";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.AXES;
    private final static Material[] woodTypes = new Material[] {
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.DARK_OAK_LOG,
            Material.PALE_OAK_LOG, Material.OAK_LOG, Material.JUNGLE_LOG, Material.MANGROVE_LOG,
            Material.SPRUCE_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM
    };

    public Abholzung() {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final int level = getEnchantmentLevel(item);
        if (level != 0
            && canEnchantItem(item)
            && Arrays.stream(woodTypes).toList().contains(event.getBlock().getType())
        ) {
            event.setCancelled(true);
            final int logAmount = 5 * level;
            final AdjacentBlockFinder breaker = new AdjacentBlockFinder(woodTypes, logAmount);
            List<Location> blocksToBreak = breaker.findConnectedBlocks(event.getBlock());

            for (Location loc : blocksToBreak) {
                loc.getBlock().breakNaturally(item);
            }
        }
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_ABHOLZUNG.getKey();
    }

}
