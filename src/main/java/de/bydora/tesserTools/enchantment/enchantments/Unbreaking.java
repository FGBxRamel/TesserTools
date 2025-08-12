package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class Unbreaking extends EnhVanillaEnch {

    private final static String id = "tessertools:haltbarkeit";
    private final static String displayName = "Haltbarkeit";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static NamespacedKey key = EnchantmentSpaceKeys.ENCH_UNBRKEAING.getKey();
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(
            MaterialArrayMerger.merge(EquipmentGroups.TOOLS, EquipmentGroups.ARMOR),
            new Material[] {Material.FISHING_ROD, Material.BOW, Material.MACE, Material.SHIELD, Material.TURTLE_HELMET,
            Material.WOLF_ARMOR, Material.CROSSBOW, Material.FLINT_AND_STEEL, Material.FLINT_AND_STEEL, Material.BRUSH,
            Material.CARROT_ON_A_STICK, Material.SPYGLASS, Material.WARPED_FUNGUS_ON_A_STICK, Material.ELYTRA});
    private final static Enchantment vanillaEnchantment = Enchantment.UNBREAKING;

    public Unbreaking() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment, key);
    }
}
