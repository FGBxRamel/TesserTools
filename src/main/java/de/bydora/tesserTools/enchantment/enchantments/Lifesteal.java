package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Set;

public class Lifesteal extends CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:life_steal";
    private final static String displayName = "Lebensklauer";
    private final static int maxLevel = 1;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = EquipmentGroups.SWORDS;

    public Lifesteal() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_LIFESTEAL.getKey());
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
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player
            && event.isCritical()
        ) {
            if (canEnchantItem(player.getInventory().getItemInMainHand())) {
                if (getEnchantmentLevel(player.getInventory().getItemInMainHand()) == 0) {return;}
                AttributeInstance absorption = player.getAttribute(Attribute.MAX_ABSORPTION);
                assert absorption != null;
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
    }
}
