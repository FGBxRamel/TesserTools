package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Lifesteal implements CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:lebensklauer";
    private final static String displayName = "Lebensklauer";
    private final static int maxLevel = 1;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.SWORDS;

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player
            && event.isCritical()
        ) {
            AttributeInstance absorption = player.getAttribute(Attribute.MAX_ABSORPTION);
            if (absorption.getBaseValue() < 20) {
                absorption.setBaseValue(20);
            }
            final double newAbsorption = player.getAbsorptionAmount() + 2;
            player.setAbsorptionAmount(newAbsorption);
            BukkitScheduler scheduler = player.getServer().getScheduler();
            scheduler.runTaskLater(TesserTools.getPlugin(TesserTools.class), () -> {
                final double newAmount = player.getAbsorptionAmount() - 2;
                player.setAbsorptionAmount(newAmount > 0 ? newAmount : 0);
                    }, 200);
        }
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
        return EnchantmentSpaceKeys.ENCH_LIFESTEAL.getKey();
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
