package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Protection implements CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:schutz";
    private final static String displayName = "Schutz";
    private final static int maxLevel = 5;
    private final static int startLevel = 4;
    private final static Material[] enchantableItems = EquipmentGroups.ARMOR;

    @Override
    public void enchantmentEvent(EntityDamageByEntityEvent event) {

    }

    @Override
    public @NotNull String getID() {
        return id;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return startLevel;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return Arrays.stream(enchantableItems).toList().contains(item.getType());
    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_PROTECTION.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER, 0);
    }

    @Override
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)) {return false;}
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        return level == container.get(getSaveKey(), PersistentDataType.INTEGER);
    }
}
