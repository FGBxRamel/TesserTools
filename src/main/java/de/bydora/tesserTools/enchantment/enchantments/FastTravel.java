package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;
import java.util.Set;

public class FastTravel extends CustomEnchantment<EntityMountEvent>{

    private final static String id = "tessertools:fast_travel";
    private final static String displayName = "Schnelle Reise";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = new Material[]{
            Material.BLACK_HARNESS, Material.BLUE_HARNESS, Material.BROWN_HARNESS, Material.LIGHT_BLUE_HARNESS,
            Material.CYAN_HARNESS, Material.GRAY_HARNESS, Material.LIGHT_GRAY_HARNESS, Material.LIGHT_BLUE_HARNESS,
            Material.GREEN_HARNESS, Material.LIME_HARNESS, Material.ORANGE_HARNESS, Material.MAGENTA_HARNESS,
            Material.PINK_HARNESS, Material.PURPLE_HARNESS, Material.RED_HARNESS, Material.WHITE_HARNESS,
            Material.YELLOW_HARNESS};
    private final static double BASE_VALUE = .05;

    public FastTravel() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_FAST_TRAVEL.getKey());
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

    @Override
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void enchantmentEvent(EntityMountEvent event) {
        if (event.getMount() instanceof HappyGhast ghast
            && event.getEntity() instanceof Player
            && getEnchantmentLevel(ghast.getEquipment().getItem(EquipmentSlot.BODY)) > 0
            && ghast.getPassengers().isEmpty()) {

            final int level = getEnchantmentLevel(ghast.getEquipment().getItem(EquipmentSlot.BODY));
            final double modifier = switch (level) {
                case 1 -> 1.5;
                case 2 -> 1.75;
                case 3 -> 3.0;
                default -> 1.0;
            };
            Objects.requireNonNull(ghast.getAttribute(Attribute.FLYING_SPEED)).setBaseValue(BASE_VALUE * modifier);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void unmountEvent(EntityDismountEvent event) {
        if (event.getDismounted() instanceof HappyGhast ghast
                && event.getEntity() instanceof Player
                && getEnchantmentLevel(ghast.getEquipment().getItem(EquipmentSlot.BODY)) > 0
                && ghast.getPassengers().size() == 1) {
            Objects.requireNonNull(ghast.getAttribute(Attribute.FLYING_SPEED)).setBaseValue(BASE_VALUE);
        }
    }
}
