package de.bydora.tesserTools.enchantment.util;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemContainer {

    private final Map<Material, List<ItemSource>> sourceMap = new HashMap<>();
    private final Map<Material, Integer> amounts = new HashMap<>();
    private static final IllegalStateException ShulkerIsNullException =
            new IllegalStateException("Needs item to not be null if Inventory is ShulkerBox!");

    public ItemContainer() {}

    /**
     * Create an ItemContainer from an inventory
     * @param inventory The inventory
     * @return The filled ItemContainer
     */
    public static @NotNull ItemContainer fromInventory(@NotNull Inventory inventory) {
        return fromInventory(inventory, null);
    }

    /**
     * Create an ItemContainer from an inventory
     * @param inventory The inventory
     * @param allowedMaterial The materials that should be processed, ignores all others. Set to empty or null to ignore
     * @return The filled ItemContainer
     */
    public static @NotNull ItemContainer fromInventory(@NotNull Inventory inventory, Set<Material> allowedMaterial) {
        var shouldCheckAllowed = !(Objects.isNull(allowedMaterial) || allowedMaterial.isEmpty());
        var container = new ItemContainer();
        for (var item: inventory.getContents()) {
            if (Objects.isNull(item)) {continue;}
            if (item.getType() == Material.SHULKER_BOX) {
                var stateMeta = (BlockStateMeta) item.getItemMeta();
                var box = (ShulkerBox) stateMeta.getBlockState();
                var boxInventory = box.getInventory();
                for (var boxItem : boxInventory) {
                    if (!Objects.isNull(boxItem)
                            && (shouldCheckAllowed && allowedMaterial.contains(boxItem.getType()))) {
                        container.addMaterial(boxItem.getType(), boxItem.getAmount(), boxInventory, item);
                    }
                }
            } else if (shouldCheckAllowed) {
                if (allowedMaterial.contains(item.getType())) {
                    container.addMaterial(item.getType(), item.getAmount(), inventory, null);
                }
            } else {container.addMaterial(item.getType(), item.getAmount(), inventory, null);}
        }
        return container;
    }

    /**
     * Adds a material to the container. Accumulates the amount if the material already exists.
     * @param material The material to add
     * @param amount The amount of the material
     * @param source The source of the material
     * @param item The item corresponding to the inventory. Can be null.
     * @return Whether the material already existed in the container
     */
    public boolean addMaterial(@NotNull Material material, int amount, @NotNull Inventory source,
                               @Nullable ItemStack item) {
        if (source.getType() == InventoryType.SHULKER_BOX && Objects.isNull(item)) {
            // In case it is a ShulkerBox we need the item to write to the ItemMeta
            throw ShulkerIsNullException;
        }
        boolean alreadyExisted = sourceMap.containsKey(material) && amounts.containsKey(material);
        ItemSource itemSource = new ItemSource(source, source.getType(), item);
        if (alreadyExisted) {
            sourceMap.get(material).add(itemSource);
            amounts.replace(material, amounts.get(material) + amount);
        } else {
            sourceMap.put(material, new LinkedList<>(List.of(itemSource)));
            amounts.put(material, amount);
        }
        return alreadyExisted;
    }

    /**
     * Removes the specified amount of the material from the ItemContainer
     * @param material The material to remove
     * @param amount The amount to remove
     * @param removeFromInventory Whether to also remove the material from the linked sources
     */
    public void removeAmount(@NotNull Material material, int amount, boolean removeFromInventory) {
        if (amount == 0) {
            return;
        }

        Integer currentAmount = amounts.get(material);
        if (currentAmount == null || amount > currentAmount) {
            throw new IllegalArgumentException("Cannot remove more material than existing");
        }

        if (removeFromInventory) {
            int remainingToRemove = amount;
            var sources = sourceMap.get(material);
            if (sources != null) {
                Iterator<ItemSource> iterator = sources.iterator();
                while (iterator.hasNext() && remainingToRemove > 0) {
                    ItemSource source = iterator.next();
                    Inventory inventory = getInventory(source);

                    ItemStack removeStack = new ItemStack(material, remainingToRemove);
                    var notRemoved = inventory.removeItemAnySlot(removeStack);
                    int notRemovedAmount = notRemoved.isEmpty() ? 0 : notRemoved.values().iterator().next().getAmount();
                    int removedAmount = remainingToRemove - notRemovedAmount;

                    if (removedAmount > 0) {
                        if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET) {
                            inventory.addItem(new ItemStack(Material.BUCKET, removedAmount));
                        }

                        if (source.type() == InventoryType.SHULKER_BOX) {
                            if (Objects.isNull(source.item())) {
                                throw ShulkerIsNullException;
                            }

                            var stateMeta = (BlockStateMeta) source.item().getItemMeta();
                            var box = (ShulkerBox) stateMeta.getBlockState();

                            // Write changed contents to BlockState
                            box.getInventory().setContents(inventory.getContents());

                            // Write BlockState to ItemMeta and to the Item
                            stateMeta.setBlockState(box);
                            source.item().setItemMeta(stateMeta);
                        }
                    }

                    remainingToRemove = notRemovedAmount;

                    // If empty, remove the source
                    if (notRemovedAmount > 0) {
                        iterator.remove();
                    }
                }
            }

            if (remainingToRemove > 0) {
                throw new IllegalStateException("Container state is inconsistent. " +
                        "Could not remove full amount from inventories.");
            }
        }

        amounts.put(material, currentAmount - amount);
        if (amounts.get(material) == 0) {
            amounts.remove(material);
            sourceMap.remove(material);
        }
    }

    /**
     * Extracts the inventory from a given source.
     * @param source The source to extract from
     * @return The inventory of the source
     */
    private static @NotNull Inventory getInventory(ItemSource source) {
        var inventory = source.inventory();
        if (source.type() == InventoryType.SHULKER_BOX) {
            if (Objects.isNull(source.item())) {
                throw ShulkerIsNullException;
            }
            var stateMeta = (BlockStateMeta) source.item().getItemMeta();
            var box = (ShulkerBox) stateMeta.getBlockState();
            inventory = box.getInventory();
        }
        return inventory;
    }

    /**
     * Removes exactly one of a random Material and returns the material
     * @param removeFromInventory Whether to also remove it from the inventory it's in
     * @return The removed material. Gives back WATER and LAVA instead of their respective buckets.
     */
    public Material removeRandom(boolean removeFromInventory) {
        Random generator = new Random();
        // Get random material
        Material[] materials = amounts.keySet().toArray(new Material[0]);
        Material material = materials[generator.nextInt(materials.length)];
        removeAmount(material, 1, removeFromInventory);
        return switch (material) {
            case WATER_BUCKET -> Material.WATER;
            case LAVA_BUCKET -> Material.LAVA;
            default -> material;
        };
    }

    /**
     * Get the total amount of items in the ItemContainer
     * @return The amount of items in the ItemContainer
     */
    public int getTotalAmount() {
        return amounts.values()
                .stream()
                .mapToInt(Integer::valueOf)
                .sum();
    }

    /**
     * Get the amount of a specific material in the ItemContainer
     * @param material The material to get the amount for
     * @return The amount of the material in the ItemContainer
     */
    public int getAmount(@NotNull Material material) {return amounts.get(material);}

}
