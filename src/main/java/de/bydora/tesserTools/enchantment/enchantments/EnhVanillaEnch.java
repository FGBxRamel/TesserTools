package de.bydora.tesserTools.enchantment.enchantments;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class EnhVanillaEnch extends CustomEnchantment<Event> {

    private final Enchantment vanillaEnchantment;

    public EnhVanillaEnch(String id, int maxLevel, String displayName, int minLevel, Material[] enchantableItems,
                          Enchantment vanillaEnchantment) {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)
                && item.getType() != Material.BOOK
                && item.getType() != Material.ENCHANTED_BOOK
        )
        {
            return false;
        }

        // Set CustomModelData
        CustomModelData currentModelData = item.getDataOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().build());
        CustomModelData.Builder builder = this.getBuilder(currentModelData);
        item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                builder.build()
        );

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(this.vanillaEnchantment, level);
        return true;
    }

}
