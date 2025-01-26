package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class Magnetic implements CustomEnchantment<PlayerMoveEvent> {

    private final static String id = "tessertools:magnetisch";
    private final static String displayName = "Magnetisch";
    private final static int maxLevel = 2;
    private final static int startLevel = 1;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(new Material[] {Material.SHEARS},
            EquipmentGroups.TOOLS);

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(PlayerMoveEvent event) {
        final ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (!canEnchantItem(itemInHand)
            || getEnchantmentLevel(itemInHand) == 0
        ) {return;}
        int level = getEnchantmentLevel(itemInHand);
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        final double pickupRadius = 5 * level;
        Collection<Item> nearbyItems = player.getWorld().getNearbyEntitiesByType(
                Item.class,
                playerLocation,
                pickupRadius, pickupRadius, pickupRadius
        );

        for (Item item : nearbyItems) {
            // Prüfe, ob das Item noch existiert
            if (!item.isValid() || item.isDead()) continue;

            // Versuche, das Item in das Inventar des Spielers zu legen
            ItemStack itemStack = item.getItemStack();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
            // Wenn das Inventar den gesamten Stack aufnehmen konnte
            if (leftover.isEmpty()) {
                EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(player, item, 0);
                pickupEvent.callEvent();
                item.remove(); // Entferne das Item aus der Welt
                player.getWorld().playSound(playerLocation, "entity.item.pickup", 1.0f, 1.0f);
            } else {
                var leftoverItem = leftover.values().iterator().next();
                EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(player, item,
                        leftoverItem.getAmount());
                pickupEvent.callEvent();
                // Wenn ein Teil des Stacks nicht eingesammelt werden konnte
                item.setItemStack(leftoverItem);
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
        return EnchantmentSpaceKeys.ENCH_MAGNETIC.getKey();
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
