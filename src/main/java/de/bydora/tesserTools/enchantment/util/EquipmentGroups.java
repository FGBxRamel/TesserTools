package de.bydora.tesserTools.enchantment.util;

import org.bukkit.Material;

public record EquipmentGroups() {
    public static final Material[] AXES = new Material[] {
            Material.DIAMOND_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.NETHERITE_AXE
    };

    public static final Material[] PICKAXES = new Material[] {
            Material.DIAMOND_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE,
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.NETHERITE_PICKAXE
    };

    public static final Material[] SHOVELS = new Material[] {
            Material.DIAMOND_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.IRON_SHOVEL,
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.NETHERITE_SHOVEL
    };

}
