package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.AdjacentBlockFinder;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static java.util.Map.entry;

public class Debarker extends CustomEnchantment<PlayerInteractEvent> {

    private final static String id = "tessertools:debarker";
    private final static String displayName = "Debarker";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.AXES;
    private final static Map<Material, Material> woodTypes = Map.ofEntries(
            entry(Material.ACACIA_LOG, Material.STRIPPED_ACACIA_LOG),
            entry(Material.BIRCH_LOG, Material.STRIPPED_BIRCH_LOG),
            entry(Material.CHERRY_LOG, Material.STRIPPED_CHERRY_LOG),
            entry(Material.DARK_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG),
            entry(Material.PALE_OAK_LOG, Material.STRIPPED_PALE_OAK_LOG),
            entry(Material.OAK_LOG, Material.STRIPPED_OAK_LOG),
            entry(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG),
            entry(Material.MANGROVE_LOG, Material.STRIPPED_MANGROVE_LOG),
            entry(Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG),
            entry(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM),
            entry(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM)
    );

    public Debarker() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_DEBARKER.getKey());
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
    public void enchantmentEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final int level = getEnchantmentLevel(item);
        if (level != 0
            && canEnchantItem(item)
            && Objects.nonNull(event.getClickedBlock())
            && woodTypes.containsKey(event.getClickedBlock().getType())
        ) {
            event.setCancelled(true);
            final int logAmount = 5 * level;
            final AdjacentBlockFinder breaker = new AdjacentBlockFinder(woodTypes.keySet()
                    .toArray(new Material[0]), logAmount);
            List<Location> blocksToStrip = breaker.findConnectedBlocks(event.getClickedBlock());

            for (Location loc : blocksToStrip) {
                var log = loc.getBlock();
                log.setType(woodTypes.get(log.getType()));
            }
        }
    }
}
