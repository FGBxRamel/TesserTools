package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FastAttack implements CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:schneller_schlag";
    private final static String displayName = "Schneller Schlag";
    private final static int maxLevel = 3;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(
            EquipmentGroups.SWORDS, EquipmentGroups.AXES);

    @Override
    // @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            player.sendMessage("Hier");
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (!Arrays.stream(enchantableItems).toList().contains(itemInHand.getType())) {
                final int level = getEnchantmentLevel(itemInHand);
                final double percentage = level * 0.2;
                final int newCooldown = (int) (player.getAttackCooldown() - (player.getAttackCooldown() * percentage));
                BukkitScheduler scheduler = player.getServer().getScheduler();
                scheduler.runTaskLater(TesserTools.getPlugin(TesserTools.class), player::resetCooldown, newCooldown);
            }
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
        return EnchantmentSpaceKeys.ENCH_FAST_ATTACK.getKey();
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
