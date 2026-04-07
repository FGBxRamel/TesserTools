package de.bydora.tesserTools.enchantment.util;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class ItemContainer {

    private final Map<Material, List<Inventory>> sourceMap = new HashMap<>();
    private final Map<Material, Integer> amounts = new HashMap<>();
    private static final Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

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
                var box = (ShulkerBox) item.getItemMeta();
                var boxInventory = box.getInventory();
                for (var boxItem : boxInventory) {
                    if (!Objects.isNull(boxItem)
                            && (shouldCheckAllowed && allowedMaterial.contains(boxItem.getType()))) {
                        container.addMaterial(boxItem.getType(), boxItem.getAmount(), boxInventory);
                    }
                }
            } else if (shouldCheckAllowed) {
                if (allowedMaterial.contains(item.getType())) {
                    container.addMaterial(item.getType(), item.getAmount(), inventory);
                }
            } else {container.addMaterial(item.getType(), item.getAmount(), inventory);}
        }
        return container;
    }

    /**
     * Adds a material to the container. Accumulates the amount if the material already exists.
     * @param material The material to add
     * @param amount The amount of the material
     * @param source The source of the material
     * @return Whether the material already existed in the container
     */
    public boolean addMaterial(@NotNull Material material, int amount, @NotNull Inventory source) {
        boolean alreadyExisted = sourceMap.containsKey(material) && amounts.containsKey(material);
        if (alreadyExisted) {
            sourceMap.get(material).add(source);
            amounts.replace(material, amounts.get(material) + amount);
        } else {
            sourceMap.put(material, new LinkedList<>(List.of(source)));
            amounts.put(material, amount);
        }
        return alreadyExisted;
    }

    /**
     * Fully deletes a material from the ItemContainer
     * @param material The material to remove
     * @param removeFromInventory Whether to also remove the material from ALL LINKED sources. Use with caution!
     * @return The amount of items removed
     */
    public int deleteMaterial(@NotNull Material material, boolean removeFromInventory) {
        if (removeFromInventory) {
            for (var source : sourceMap.get(material)) {
                source.remove(material);
            }
        }
        int totalDeleted = amounts.get(material);
        sourceMap.remove(material);
        amounts.remove(material);
        return totalDeleted;
    }

    /**
     * Removes the specified amount of the material from the ItemContainer
     * @param material The material to remove
     * @param amount The amount to remove
     * @param removeFromInventory Whether to also remove the material from the linked sources
     */
    public void removeAmount(@NotNull Material material, int amount, boolean removeFromInventory) {
        if (amount == 0) {return;}
        if (amount > amounts.get(material)) {
            throw new IllegalArgumentException("Cannot remove more material than existing");
        }
        ItemStack removeItem = new ItemStack(material, amount);
        if (removeFromInventory) {
            for (var source : sourceMap.get(material)) {
                var notRemoved = source.removeItemAnySlot(removeItem);
                int notRemovedAmount = notRemoved.isEmpty() ? 0 : notRemoved.get(0).getAmount();
                if (material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET) {
                    source.addItem(new ItemStack(Material.BUCKET, amount-notRemovedAmount));
                }
                if (!notRemoved.isEmpty()) {
                    removeItem.setAmount(notRemovedAmount);
                    sourceMap.get(material).remove(source); // Remove the source as it's empty
                } else {break;}
            }
        }
        amounts.replace(material, amounts.get(material) - amount);
        if (amounts.get(material) == 0) {
            amounts.remove(material);
            sourceMap.remove(material);
        }
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
