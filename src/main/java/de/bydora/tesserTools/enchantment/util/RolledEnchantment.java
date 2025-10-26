package de.bydora.tesserTools.enchantment.util;

import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;

public sealed interface RolledEnchantment
    permits RolledEnchantment.Vanilla, RolledEnchantment.Custom, RolledEnchantment.Missing {

    record Vanilla(Enchantment enchantment) implements RolledEnchantment {}
    record Custom(CustomEnchantment<?> enchantment) implements RolledEnchantment {}
    record Missing() implements RolledEnchantment {}
}
