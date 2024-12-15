package de.bydora.tesserTools.enchantment.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface CustomEnchantment<T extends  Event> extends Listener {

    /**
     * The event that the enchantment should listen to
     * @param event The event
     */
    @EventHandler
    void enchantmentEvent(T event);

    /**
     * A method to get the ID of the enchantment.
     * @return The ID in format: plugin:enchantmentName
     */
    @NotNull String getID();

    /**
     * A method to get the user-friendly- / display name of the enchantment.
     * @return The user-friendly name of the enchantment.
     */
    @NotNull String getDisplayName();

    /**
     * A method to get the maximum level of this enchantment.
     * @return The maximum level of the enchantment
     */
    int getMaxLevel();

    /**
     * Get the level that the enchantment should start at.
     * @return The start level of the enchantment
     */
    int getStartLevel();

    /**
     * Checks if this Enchantment may be applied to the given {@link
     * ItemStack}.
     * <p>
     * This does not check if it conflicts with any enchantments already
     * applied to the item.
     *
     * @param item Item to test
     * @return True if the enchantment may be applied, otherwise False
     */
    boolean canEnchantItem(@NotNull ItemStack item);

    /**
     * Get the {@link NamespacedKey} which is used when enchanting an item with this enchantment.
     * @return The {@link NamespacedKey} which belongs to this enchantment
     */
    @NotNull NamespacedKey getSaveKey();

    /**
     * Checks wether or not the {@link ItemStack} has the enchantment.
     * @param itemStack The item stack to check
     * @return The level of the entchantment; 0 if not present.
     */
    int getEnchantmentLevel(@NotNull ItemStack itemStack);

    /**
     * Enchants the given item with the enchantment if possible.
     * @param item The item to enchant
     * @param level The level the enchantment should have
     * @return Whether the enchanting was successfully.
     */
    boolean enchantItem(@NotNull ItemStack item, int level);
}
