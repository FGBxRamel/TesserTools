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
import org.bukkit.enchantments.Enchantment;

import java.util.Set;

public class Unbreaking extends EnhVanillaEnch {

    private final static String id = "tessertools:haltbarkeit";
    private final static String displayName = "Haltbarkeit";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(
            MaterialArrayMerger.merge(EquipmentGroups.TOOLS, EquipmentGroups.ARMOR),
            new Material[] {Material.FISHING_ROD, Material.BOW, Material.MACE, Material.SHIELD, Material.TURTLE_HELMET,
            Material.WOLF_ARMOR, Material.CROSSBOW, Material.FLINT_AND_STEEL, Material.FLINT_AND_STEEL, Material.BRUSH,
            Material.CARROT_ON_A_STICK, Material.SPYGLASS, Material.WARPED_FUNGUS_ON_A_STICK, Material.ELYTRA});
    private final static Enchantment vanillaEnchantment = Enchantment.UNBREAKING;

    public Unbreaking() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment, EnchantmentSpaceKeys.ENCH_UNBRKEAING.getKey());
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
}
