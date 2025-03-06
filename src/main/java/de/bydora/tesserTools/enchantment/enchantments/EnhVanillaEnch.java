package de.bydora.tesserTools.enchantment.enchantments;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

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
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER,
                itemStack.getEnchantmentLevel(this.vanillaEnchantment));
    }

    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)
                && item.getType() != Material.BOOK
                && item.getType() != Material.ENCHANTED_BOOK
        )
        {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(this.vanillaEnchantment, level);
        return true;
    }

}
