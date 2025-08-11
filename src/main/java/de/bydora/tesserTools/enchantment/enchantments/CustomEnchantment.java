package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "removal", "Contract", "UnstableApiUsage"})
public abstract class CustomEnchantment<T extends  Event> extends Enchantment implements Listener {

    private final static Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final int minLevel;
    private final Material[] enchantableItems;
    private final RegistryKeySet<@NotNull ItemType> supportedItems;
    private final NamespacedKey key;
    private final String baseTranslationKey;

    public CustomEnchantment(String id, int maxLevel, String displayName, int minLevel, Material[] enchantableItems,
    @NotNull NamespacedKey key) {
        this.id = id;
        this.maxLevel = maxLevel;
        this.displayName = displayName;
        this.minLevel = minLevel;
        this.enchantableItems = enchantableItems;
        this.supportedItems = RegistrySets.fromMaterials(this.enchantableItems);
        this.key = key;
        this.baseTranslationKey = "enchantment.tessertools." + key.getKey();
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
    public @NotNull NamespacedKey getSaveKey() {
        return key;
    }

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
     * Get the level that the enchantment should start at.
     * @return The start level of the enchantment
     */
    public int getMinLevel() {
        return minLevel;
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
    @SuppressWarnings("UnstableApiUsage")
    public boolean enchantItem(@NotNull ItemStack item, int level) {
        if (!canEnchantItem(item)
            && item.getType() != Material.BOOK
            && item.getType() != Material.ENCHANTED_BOOK
        ) {
            return false;
        }

        // Set CustomModelData
        CustomModelData currentModelData = item.getDataOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().build());
        CustomModelData.Builder builder = getBuilder(currentModelData);
        item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                builder.build()
        );

        // Set PersistentDataContainer
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(getSaveKey(), PersistentDataType.INTEGER, level);
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(this, level);

        return true;
    }

    @SuppressWarnings("UnstableApiUsage")
    CustomModelData.@NotNull Builder getBuilder(CustomModelData currentModelData) {
        CustomModelData.Builder builder = CustomModelData.customModelData();

        // Add the current enchantment or "multiple" flag if it's not present, add all non-related
        boolean multipleOrEqual = false;
        for (var string : currentModelData.strings()) {
            // If this enchantment already exists or the "multiple" flag is present
            if ((string.equals(sanitizeString(id)))
                && !multipleOrEqual) {
                builder.addString(sanitizeString(id));
                multipleOrEqual = true;
            }
            // If it's an enchantment from the plugin
            else if (string.startsWith("tessertools")
                    && !multipleOrEqual) {
                builder.addString("tessertools-multiple");
                multipleOrEqual = true;
            }
            // All the others, not plugin related
            else if (!string.startsWith("tessertools")) {
                builder.addString(string);
            }
        }
        if (!multipleOrEqual) {
            builder.addString(sanitizeString(id));
        }
        currentModelData.floats().forEach(builder::addFloat);
        currentModelData.flags().forEach(builder::addFlag);
        currentModelData.colors().forEach(builder::addColor);
        return builder;
    }

    /**
     * Returns a custom-model-data-safe string.
     * @param string The origin string
     * @return A safe string
     */
    private static String sanitizeString(String string) {
        return string
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace(":", "-");
    }

    //<editor-fold desc="Vanilla Enchantment implementation">
    @Override public @NotNull Component displayName(int level) {
        Component name = Component.translatable(this.baseTranslationKey);
        if (level != 1 || maxLevel > 1) {
            name = name.append(Component.space())
                    .append(Component.translatable("enchantment.level." + level));
        }
        return name;
    }

    @Override public @NotNull Component description() {
        return Component.translatable(baseTranslationKey + ".description");
    }

    @Override public int getMaxLevel() { return this.maxLevel; }
    @Override public int getStartLevel() { return getMinLevel(); }
    @Override public boolean canEnchantItem(@NotNull ItemStack item) {
        return Arrays.stream(enchantableItems).toList().contains(item.getType());
    }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isCursed() { return false; }
    @Override public boolean conflictsWith(@NotNull Enchantment other) { return false; }
    @Override
    public @NotNull String getName() {
        return displayName;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMinModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getAnvilCost() {
        return 0;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityType entityType) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return Set.of();
    }

    @Override
    public @NotNull RegistryKeySet<@NotNull ItemType> getSupportedItems() {
        return this.supportedItems;
    }

    @Override
    public @Nullable RegistryKeySet<@NotNull ItemType> getPrimaryItems() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public @NotNull RegistryKeySet<@NotNull Enchantment> getExclusiveWith() {
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT);
    }

    @Override
    public @NotNull String translationKey() {
        return this.baseTranslationKey;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return this.baseTranslationKey;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }
    //</editor-fold>

}
