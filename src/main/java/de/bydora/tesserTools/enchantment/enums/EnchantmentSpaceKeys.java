package de.bydora.tesserTools.enchantment.enums;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.NamespacedKey;

public enum EnchantmentSpaceKeys {

    //<editor-fold desc="Table States">
    STATE_BLOCKED(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateBlocked")),
    STATE_CONTAINER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateContainer")),
    STATE_CHARGE_LEVEL(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "tableStateBlocked")),
    //</editor-fold>

    //<editor-fold desc="Enchantments">
    ENCH_ABHOLZUNG(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchAbholzung")),
    ENCH_AREA_BREAK(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchAreaBreak")),
    ENCH_VEIN_MINER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchVeinMiner")),
    ENCH_UNBRKEAING(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchUnbreaking")),
    ENCH_HARVESTER(new NamespacedKey(TesserTools.getPlugin(TesserTools.class), "enchHarvester")),
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
