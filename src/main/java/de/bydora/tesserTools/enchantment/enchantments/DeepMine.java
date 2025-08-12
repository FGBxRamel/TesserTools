package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class DeepMine extends CustomEnchantment<BlockBreakEvent> {

    private final static String id = "tessertools:tiefenabbau";
    private final static String displayName = "Tiefenabbau";
    private final static int maxLevel = 2;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(
            EquipmentGroups.SHOVELS, EquipmentGroups.PICKAXES);
    private final static Material[] affectedBlocks = AreaBreak.getAffectedBlocks();

    public DeepMine() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_DEEPMINE.getKey());
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
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (getEnchantmentLevel(item) > 0
                && Arrays.stream(affectedBlocks).toList().contains(event.getBlock().getType())
        ) {
            var areaBreakLevel = new AreaBreak().getEnchantmentLevel(item);
            var addVec = getAddVector(event.getPlayer().getEyeLocation().getYaw(),
                    event.getPlayer().getEyeLocation().getPitch());
            var loc = event.getBlock().getLocation().clone();

            for (int i = 0; i < getEnchantmentLevel(item); i++) {
                loc.add(addVec);
                var block = loc.getBlock();
                block.breakNaturally(item);
                if (areaBreakLevel > 0) {
                    AreaBreak.breakArea(areaBreakLevel > 1, event.getPlayer(), block, item);
                }
            }
        }
    }

    /**
     * Gets the vector to add to the location according to the yaw.
     * @param yaw The yaw of the players eyes
     * @return The vector
     */
    private static @NotNull Vector getAddVector(float yaw, float pitch) {
        Vector addVector;
        //  Block break in Y direction
        if (Math.abs(pitch) >= 30) {
            if (pitch > 0) {
                addVector = new Vector(0,-1,0);
            }
            else addVector = new Vector(0,1,0);
        }
        // Block break in XZ-Direction
        else {
            // Positive Z
            if (yaw >= -45 && yaw <= 45) {
                addVector = new Vector(0, 0, 1);
            }
            // Negative X
            else if (yaw > 45 && yaw < 135) {
                addVector = new Vector(-1, 0, 0);
            }
            // Negative Z
            else if ((yaw >= 135 && yaw <= 180)
                    || yaw < -135
            ) {
                addVector = new Vector(0, 0, -1);
            }
            // Positive X
            else {
                addVector = new Vector(1, 0, 0);
            }
        }
        return addVector;
    }

}
