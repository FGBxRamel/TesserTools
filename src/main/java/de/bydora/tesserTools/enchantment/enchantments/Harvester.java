package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public class Harvester extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:harvester";
    private final static String displayName = "Ernter";
    private final static int maxLevel = 1;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.HOES;
    private final static Map<Material, Material> cropSeedMapping = Map.ofEntries(
            entry(Material.WHEAT, Material.WHEAT_SEEDS),
            entry(Material.BEETROOTS, Material.BEETROOT_SEEDS),
            entry(Material.POTATOES, Material.POTATO),
            entry(Material.CARROTS, Material.CARROT)
    );

    public Harvester() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_HARVESTER.getKey());
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
                        scheduler.runTaskLater(TesserTools.getPlugin(TesserTools.class),
                                () -> event.getBlock().setType(crop), 1);
                    }
                }
            }
        }
    }
}
