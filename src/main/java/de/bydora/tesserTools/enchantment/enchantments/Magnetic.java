package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Magnetic extends CustomEnchantment<PlayerMoveEvent> {

    private final static String id = "tessertools:magnetisch";
    private final static String displayName = "Magnetisch";
    private final static int maxLevel = 2;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(new Material[] {Material.SHEARS},
            EquipmentGroups.TOOLS);

    public Magnetic() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_MAGNETIC.getKey());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static EnchantDef def() {
        var supported = RegistrySets.fromMaterials(enchantableItems);
        var description = Component.translatable(getBaseTranslationKey(id) + ".description");
        return new EnchantDef(
                sanitizeString(id),
                description,
                supported,
                1,
                maxLevel,
                10,
                Set.of(),
                RegistrySet.keySet(RegistryKey.ENCHANTMENT)
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(PlayerMoveEvent event) {
        final ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        final ItemStack itemInOff = event.getPlayer().getInventory().getItemInOffHand();
        final int level = Math.max(getEnchantmentLevel(itemInHand), getEnchantmentLevel(itemInOff));
        if (level == 0) {return;}

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
}
