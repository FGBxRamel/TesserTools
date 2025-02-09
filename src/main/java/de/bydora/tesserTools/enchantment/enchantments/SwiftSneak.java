package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class SwiftSneak extends CustomEnchantment<PlayerToggleSneakEvent> {

    private final static String id = "tessertools:huschen";
    private final static String displayName = "Huschen";
    private final static int maxLevel = 5;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.LEGS;

    public SwiftSneak() {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
    }

    @Override
    public void enchantmentEvent(PlayerToggleSneakEvent event) {

    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_SWIFT_SNEAK.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER,
                itemStack.getEnchantmentLevel(Enchantment.SWIFT_SNEAK));
    }

    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)) {return false;}
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, level);
        return level == container.get(getSaveKey(), PersistentDataType.INTEGER);
    }
}
