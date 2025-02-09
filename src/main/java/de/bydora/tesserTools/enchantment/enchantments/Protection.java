package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class Protection extends CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:schutz";
    private final static String displayName = "Schutz";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;

    public Protection() {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
    }

    @Override
    public void enchantmentEvent(EntityDamageByEntityEvent event) {

    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_PROTECTION.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER,
                itemStack.getEnchantmentLevel(Enchantment.PROTECTION));
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
        item.addUnsafeEnchantment(Enchantment.PROTECTION, level);
        return level == container.get(getSaveKey(), PersistentDataType.INTEGER);
    }
}
