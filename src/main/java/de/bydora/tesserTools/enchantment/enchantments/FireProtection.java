package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public class FireProtection extends EnhVanillaEnch {

    private final static String id = "tessertools:feuerschutz";
    private final static String displayName = "Feuerschutz";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;
    private final static Enchantment vanillaEnchantment = Enchantment.FIRE_PROTECTION;

    public FireProtection() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment);
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_FIRE_PROT.getKey();
    }

}
