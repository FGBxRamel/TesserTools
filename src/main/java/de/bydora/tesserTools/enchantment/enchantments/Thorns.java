package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public class Thorns extends EnhVanillaEnch {

    private final static String id = "tessertools:dornen";
    private final static String displayName = "Dornen";
    private final static int maxLevel = 5;
    private final static int minLevel = 3;
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;
    private final static Enchantment vanillaEnchantment = Enchantment.THORNS;

    public Thorns() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment);
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_THORNS.getKey();
    }

}
