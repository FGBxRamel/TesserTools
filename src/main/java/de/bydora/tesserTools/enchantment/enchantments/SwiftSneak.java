package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public class SwiftSneak extends EnhVanillaEnch {

    private final static String id = "tessertools:huschen";
    private final static String displayName = "Huschen";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = EquipmentGroups.LEGS;
    private final static Enchantment vanillaEnchantment = Enchantment.SWIFT_SNEAK;

    public SwiftSneak() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, vanillaEnchantment);
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_SWIFT_SNEAK.getKey();
    }

}
