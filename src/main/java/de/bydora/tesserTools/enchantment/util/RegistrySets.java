package de.bydora.tesserTools.enchantment.util;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public final class RegistrySets {
    private RegistrySets() {}

    public static RegistryKeySet<@NotNull ItemType> fromMaterials(Material... mats) {
        var values = Arrays.stream(mats)
                .map(Material::asItemType)
                .toList();
        return RegistrySet.keySetFromValues(RegistryKey.ITEM, values);
    }
}
