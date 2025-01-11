package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Missing implements CustomEnchantment<Event> {

    @Override
    public void enchantmentEvent(Event event) {

    }

    @Override
    public @NotNull String getID() {
        return "tessertools:missing";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "MISSING";
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public int getStartLevel() {
        return -1;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_MISSING.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        return false;
    }
}
