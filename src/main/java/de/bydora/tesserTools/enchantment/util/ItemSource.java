package de.bydora.tesserTools.enchantment.util;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemSource(@NotNull Inventory inventory, @NotNull InventoryType type, @Nullable ItemStack item) {}
