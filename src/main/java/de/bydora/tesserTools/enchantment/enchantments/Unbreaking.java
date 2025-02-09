package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class Unbreaking extends CustomEnchantment<PlayerItemDamageEvent> {

    private final static String id = "tessertools:haltbarkeit";
    private final static String displayName = "Haltbarkeit";
    private final static int maxLevel = 5;
    private final static int minLevel = 4;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(
            MaterialArrayMerger.merge(EquipmentGroups.TOOLS, EquipmentGroups.ARMOR),
            new Material[] {Material.FISHING_ROD, Material.BOW, Material.MACE, Material.SHIELD, Material.TURTLE_HELMET,
            Material.WOLF_ARMOR, Material.CROSSBOW, Material.FLINT_AND_STEEL, Material.FLINT_AND_STEEL, Material.BRUSH,
            Material.CARROT_ON_A_STICK, Material.SPYGLASS, Material.WARPED_FUNGUS_ON_A_STICK, Material.ELYTRA});

    public Unbreaking() {
        super(id, maxLevel, displayName, minLevel, enchantableItems);
    }

    @Override
    public void enchantmentEvent(PlayerItemDamageEvent event) {

    }

    @Override
    public @NotNull NamespacedKey getSaveKey() {
        return EnchantmentSpaceKeys.ENCH_UNBRKEAING.getKey();
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER,
                itemStack.getEnchantmentLevel(Enchantment.UNBREAKING));
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
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, level);
        return level == container.get(getSaveKey(), PersistentDataType.INTEGER);
    }
}
