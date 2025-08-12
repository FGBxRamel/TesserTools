package de.bydora.tesserTools.enchantment.enchantments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class EnhVanillaEnch extends CustomEnchantment<Event> {

    private final Enchantment vanillaEnchantment;

    public EnhVanillaEnch(String id, int maxLevel, String displayName, int minLevel, Material[] enchantableItems,
                          Enchantment vanillaEnchantment, @NotNull NamespacedKey key) {
        super(id, maxLevel, displayName, minLevel, enchantableItems, key);
        this.vanillaEnchantment = vanillaEnchantment;
    }

    @Override
    public void enchantmentEvent(Event event) {

    }

    @Override
    public @NotNull String getDisplayName(Locale lang) {
        ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", lang);
        return l18.getString(vanillaEnchantment.getKey().asMinimalString());
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER,
                itemStack.getEnchantmentLevel(this.vanillaEnchantment));
    }
}
