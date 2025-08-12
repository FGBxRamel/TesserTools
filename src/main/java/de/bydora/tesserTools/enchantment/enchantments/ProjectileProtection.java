package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Set;

public class ProjectileProtection extends EnhVanillaEnch {

    private final static String id = "tessertools:schusssicher";
    private final static String displayName = "Schusssicher";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;
    private final static Enchantment vanillaEnchantment = Enchantment.PROJECTILE_PROTECTION;

    public ProjectileProtection() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment, EnchantmentSpaceKeys.ENCH_PROJ_PROT.getKey());
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
