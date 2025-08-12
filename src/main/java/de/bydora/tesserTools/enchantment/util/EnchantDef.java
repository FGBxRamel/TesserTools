package de.bydora.tesserTools.enchantment.util;

import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public record EnchantDef(
        @Subst("tessertools:missing")
        String safeId,
        Component description,
        RegistryKeySet<@NotNull ItemType> supportedItems,
        int anvilCost,
        int maxLevel,
        int weight,
        Set<EquipmentSlotGroup> activeSlots,
        RegistryKeySet<@NotNull Enchantment> exclusiveWith
) { }
