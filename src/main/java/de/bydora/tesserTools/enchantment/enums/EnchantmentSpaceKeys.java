package de.bydora.tesserTools.enchantment.enums;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.NamespacedKey;

public enum EnchantmentSpaceKeys {

    //<editor-fold desc="Table States">
    STATE_BLOCKED(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateBlocked")),
    STATE_CONTAINER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateContainer")),
    STATE_CHARGE_LEVEL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateCharge")),
    STATE_ENCHANTMENTS(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateEnchantments")),
    //</editor-fold>

    //<editor-fold desc="Enchantments">
    ENCH_ABHOLZUNG(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchAbholzung")),
    ENCH_AREA_BREAK(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchAreaBreak")),
    ENCH_VEIN_MINER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchVeinMiner")),
    ENCH_UNBRKEAING(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchUnbreaking")),
    ENCH_HARVESTER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchHarvester")),
    ENCH_MAGNETIC(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchMagnetic")),
    ENCH_LIFESTEAL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchLifesteal")),
    ENCH_PROTECTION(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchProtection")),
    ENCH_SWIFT_SNEAK(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchSwiftSneak")),
    ENCH_MISSING(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchMISSING")),
    ENCH_PROJ_PROT(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchProjectileProtection")),
    ENCH_FIRE_PROT(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchFireProtection")),
    ENCH_BLAST_PROT(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchBlastProtection")),
    ENCH_THORNS(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchThorns")),
    ENCH_DEEPMINE(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchDeepMine")),
    ENCH_PATHING(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchPathing")),
    ENCH_AREA_FILL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchAreaFill")),
    ENCH_SPACE_FILL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchSpaceFill")),
    ENCH_FAST_TRAVEL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchFastTravel")),
    ENCH_RAZOR(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchRazor")),
    ENCH_DEBARKER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchDebarker")),
    //</editor-fold>

    //<editor-fold desc="Other">
    //</editor-fold>
    ;

    private final NamespacedKey key;

    EnchantmentSpaceKeys(NamespacedKey key) {
        this.key = key;
    }

    public NamespacedKey getKey() {
        return key;
    }

}
