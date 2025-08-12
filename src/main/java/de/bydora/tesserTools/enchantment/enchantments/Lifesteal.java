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
import org.bukkit.scheduler.BukkitScheduler;

public class Lifesteal extends CustomEnchantment<EntityDamageByEntityEvent> {

    private final static String id = "tessertools:lebensklauer";
    private final static String displayName = "Lebensklauer";
    private final static int maxLevel = 1;
    private final static int minLevel = 1;
    private final static NamespacedKey key =EnchantmentSpaceKeys.ENCH_LIFESTEAL.getKey();
    private final static Material[] enchantableItems = EquipmentGroups.SWORDS;

    public Lifesteal() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, key);
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
