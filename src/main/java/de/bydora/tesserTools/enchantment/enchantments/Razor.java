package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AdjacentBlockFinder;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Razor extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:razor";
    private final static String displayName = "Blattrasierer";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = new Material[] {Material.SHEARS};
    private final static Material[] LEAVES = new Material[] {
            Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.BIRCH_LEAVES,
            Material.CHERRY_LEAVES, Material.JUNGLE_LEAVES, Material.MANGROVE_LEAVES, Material.OAK_LEAVES,
            Material.DARK_OAK_LEAVES, Material.PALE_OAK_LEAVES, Material.SPRUCE_LEAVES, Material.FLOWERING_AZALEA_LEAVES
    };

    public Razor() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_RAZOR.getKey());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static EnchantDef def() {
        var supported = RegistrySets.fromMaterials(enchantableItems);
        var description = Component.translatable(getBaseTranslationKey(id) + ".description");
        return new EnchantDef(
                sanitizeString(id),
                description,
                supported,
                1,
                maxLevel,
                10,
                Set.of(),
                RegistrySet.keySet(RegistryKey.ENCHANTMENT)
        );
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final int level = getEnchantmentLevel(item);
        final var eventBlock = event.getBlock();
        if (level != 0
                && canEnchantItem(item)
                && Arrays.stream(LEAVES).toList().contains(eventBlock.getType())
        ) {
            final int leaveAmount = 5 * level;
            final AdjacentBlockFinder breaker = new AdjacentBlockFinder(LEAVES, leaveAmount);
            List<Location> blocksToBreak = breaker.findConnectedBlocks(eventBlock);

            for (Location loc : blocksToBreak) {
                loc.getBlock().breakNaturally(item);
            }
        }
    }
}
