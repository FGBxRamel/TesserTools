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

    public static final Material[] HOES = new Material[] {
            Material.DIAMOND_HOE,
            Material.GOLDEN_HOE,
            Material.IRON_HOE,
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.NETHERITE_HOE
    };

    public static final Material[] SWORDS = new Material[] {
            Material.DIAMOND_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.NETHERITE_SWORD
    };

    public static final Material[] HELMETS = new Material[] {
            Material.DIAMOND_HELMET,
            Material.GOLDEN_HELMET,
            Material.IRON_HELMET,
            Material.NETHERITE_HELMET
    };

    public static final Material[] CHESTPLATES = new Material[] {
            Material.DIAMOND_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE
    };

    public static final Material[] LEGS = new Material[] {
            Material.DIAMOND_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.NETHERITE_LEGGINGS
    };

    public static final Material[] BOOTS = new Material[] {
            Material.DIAMOND_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.IRON_BOOTS,
            Material.NETHERITE_BOOTS
    };

    public static final Material[] ARMOR =
            MaterialArrayMerger.merge(
                    MaterialArrayMerger.merge(HELMETS, CHESTPLATES),
                    MaterialArrayMerger.merge(LEGS, BOOTS)
            );

    public static final Material[] NONE = new Material[0];

    // This comes straight from hell, but it will do
    public static final Material[] TOOLS =
            MaterialArrayMerger.merge(
                    MaterialArrayMerger.merge(
                            SWORDS,
                            MaterialArrayMerger.merge(HOES, SHOVELS)
                    ),
                    MaterialArrayMerger.merge(PICKAXES, AXES)
            );

}
