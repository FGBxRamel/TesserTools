package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class ProjectileProtection extends EnhVanillaEnch {

    private final static String id = "tessertools:schusssicher";
    private final static String displayName = "Schusssicher";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static NamespacedKey key = EnchantmentSpaceKeys.ENCH_PROJ_PROT.getKey();
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;
    private final static Enchantment vanillaEnchantment = Enchantment.PROJECTILE_PROTECTION;

    public ProjectileProtection() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment, key);
    }
}
