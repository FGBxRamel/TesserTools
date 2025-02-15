package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class CustomEnchantment<T extends  Event> implements Listener {

    private final static Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final int minLevel;
    private final Material[] enchantableItems;

    public CustomEnchantment(String id, int maxLevel, String displayName, int minLevel, Material[] enchantableItems) {
        this.id = id;
        this.maxLevel = maxLevel;
        this.displayName = displayName;
        this.minLevel = minLevel;
        this.enchantableItems = enchantableItems;
    }

    /**
     * The event that the enchantment should listen to
     * @param event The event
     */
    @EventHandler
    public abstract void enchantmentEvent(T event);

    /**
     * Get the {@link NamespacedKey} which is used when enchanting an item with this enchantment.
     * @return The {@link NamespacedKey} which belongs to this enchantment
     */
    public abstract @NotNull NamespacedKey getSaveKey();

    /**
     * A method to get the ID of the enchantment.
     * @return The ID in format: plugin:enchantmentName
     */
    public @NotNull String getID() {
        return id;
    }

    /**
     * A method to get the user-friendly- / display name of the enchantment.
     * @return The user-friendly name of the enchantment.
     */
    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * A method to get the user-friendly- / display name of the enchantment.
     * @param lang The language that the enchantment should be translated to
     * @return The user-friendly name of the enchantment
     */
    public @NotNull String getDisplayName(Locale lang) {
        ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", lang);
        return l18.getString(getID().replaceFirst("tessertools:", ""));
    }

    /**
     * A method to get the maximum level of this enchantment.
     * @return The maximum level of the enchantment
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Get the level that the enchantment should start at.
     * @deprecated Bad naming. Use {@link CustomEnchantment#getMinLevel()} instead.
     * @return The start level of the enchantment
     */
    @Deprecated()
    public int getStartLevel() {
        return minLevel;
    }

    /**
     * Get the level that the enchantment should start at.
     * @return The start level of the enchantment
     */
    public int getMinLevel() {
        return minLevel;
    }

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
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return Arrays.stream(enchantableItems).toList().contains(item.getType());
    }

    /**
     * Checks wether or not the {@link ItemStack} has the enchantment.
     * @param itemStack The item stack to check
     * @return The level of the entchantment; 0 if not present.
     */
    public int getEnchantmentLevel(@NotNull ItemStack itemStack) {
        try {
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            return container.getOrDefault(getSaveKey(), PersistentDataType.INTEGER, 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * Enchants the given item with the enchantment if possible.
     * @param item The item to enchant
     * @param level The level the enchantment should have
     * @return Whether the enchanting was successfully.
     */
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)
            && item.getType() != Material.BOOK
            && item.getType() != Material.ENCHANTED_BOOK
        ) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);

        return true;
    }
}
