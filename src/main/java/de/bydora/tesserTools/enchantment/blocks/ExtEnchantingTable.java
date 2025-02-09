package de.bydora.tesserTools.enchantment.blocks;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import de.bydora.tesserTools.enchantment.enchantments.Missing;
import de.bydora.tesserTools.enchantment.enchantments.Protection;
import de.bydora.tesserTools.enchantment.enchantments.Unbreaking;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.exceptions.NotAnEnchantmentTableException;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

import static java.util.Map.entry;

@SuppressWarnings("rawtypes")
public class ExtEnchantingTable {

    Logger log = TesserTools.getPlugin(TesserTools.class).getLogger();

    private static final Map ENCHANTMENT_MAP = TesserTools.getPlugin(TesserTools.class).getEnchantmentMap();
    public static final Registry<Enchantment> ENCHANTMENT_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private static final Map<Integer, String> ROMAN_NUMERALS = Map.ofEntries(
            entry(1, "I"),
            entry(2, "II"),
            entry(3, "III"),
            entry(4, "IV"),
            entry(5, "V"),
            entry(6, "VI")
    );
    private final static Collection<CustomEnchantment> CUSTOM_ENCHANTMENTS = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap().values();

    private final Location[] simpleQuarzLocations;
    private final Location[] simpleLapisLocations;
    private final Location location;
    private final List<Enchantment> vanillaEnchantments;
    private boolean isBlocked;
    private int chargeLevel;
    private final List<Object> rolledEnchantments = new ArrayList<>();
    private final EnchantingTable vanillaTable;

    public ExtEnchantingTable(@NotNull Location location) {

        this.location = location;

        if (location.getBlock().getState() instanceof EnchantingTable table) {
            this.vanillaTable = table;
            loadState();
        }
        else {
            throw new NotAnEnchantmentTableException("You can only make an instance of ExtEnchantingTable from" +
                    "an Enchanting Table!");
        }

        final Location calcQuarzLocation = location.clone().toCenterLocation();
        this.simpleQuarzLocations = new Location[]{
                calcQuarzLocation.add(4, 0, 4).clone(),
                calcQuarzLocation.add(-8, 0, 0).clone(),
                calcQuarzLocation.add(0, 0, -8).clone(),
                calcQuarzLocation.add(8, 0, 0).clone()
        };
        // Reset so the calc is right
        final Location calcLapisLocation = location.clone().toCenterLocation();
        this.simpleLapisLocations = new Location[]{
                calcLapisLocation.add(6, 0, 0).clone(),
                calcLapisLocation.add(-12, 0, 0).clone(),
                calcLapisLocation.add(6, 0, -6).clone(),
                calcLapisLocation.add(0, 0, 12).clone()
        };

        this.vanillaEnchantments = new ArrayList<>();
        for (Enchantment enchantment : ENCHANTMENT_REGISTRY) {
            vanillaEnchantments.add(enchantment);
        }
    }

    /**
     * Determines if the extended enchantment block has a valid quarz constellation.
     * @return Whether it has a valid quarz constellation
     */
    public boolean hasValidQuarz() {
        for(Location quarz : simpleQuarzLocations) {
            Material materialBottom = quarz.getBlock().getType();
            if (materialBottom != Material.CHISELED_QUARTZ_BLOCK) {return false;}
        }
        return true;
    }

    /**
     * Determines if the extended enchantment block has a valid lapis constellation.
     * @return Whether it has a valid lapis constellation
     */
    public boolean hasValidLapis() {
        for(Location lapis : simpleLapisLocations) {
            Material materialBottom = lapis.getBlock().getType();
            Material materialTop = lapis.clone().add(0,1,0).getBlock().getType();
            if (materialBottom != Material.LAPIS_BLOCK || materialTop != Material.LAPIS_BLOCK) { return false; }
        }
        return true;
    }

    /**
     * Determines if the table is valid for enchantments
     * @return Whether it is valid for enchantments
     */
    public boolean isValid() {
        return (this.hasValidLapis() && this.hasValidQuarz());
    }

    public void spawnParticles() {
        final ParticleBuilder builder = getBaseParticle(this.location);
        builder.color(255, 255,255);
        for (Location quarz : simpleQuarzLocations) {
            builder.location(quarz);
            builder.spawn();
        }
        builder.color(38,97,156);
        for (Location lapis : simpleLapisLocations) {
            builder.location(lapis);
            builder.spawn();
        }
    }

    private static @NotNull ParticleBuilder getBaseParticle(@NotNull Location originLocation) {
        final ParticleBuilder baseParticle = new ParticleBuilder(Particle.DUST);
        baseParticle.offset(.2,.2,.2);
        baseParticle.location(originLocation);
        baseParticle.receivers(160);
        baseParticle.count(10);
        return baseParticle.clone();
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
        this.saveState();
    }

    public Location getLocation() {
        return this.location;
    }

    /**
     * Get the {@link CustomEnchantment} or {@link Enchantment} of the quarz block given.
     * @param loc The location of the quarz block
     * @return The {@link CustomEnchantment} or {@link Enchantment} or null
     */
    public @Nullable Object getEnchantment(@NotNull Location loc) {
        int i = 0;
        for (var location : simpleQuarzLocations) {
            // Check if it's the same block location... Bad, but works
            if (Math.abs(loc.getX() - location.getX()) < 1
                && Math.abs(loc.getY() - location.getY()) < 1
                && Math.abs(loc.getZ() - location.getZ()) < 1
            ) {
                return this.rolledEnchantments.get(i);
            }
            i++;
        }
        return null;
    }

    /**
     * Loads the state of the enchantment table from it's {@link PersistentDataContainer}.
     */
    private void loadState() {
        var stateContainer = this.vanillaTable.getPersistentDataContainer().getOrDefault(
                EnchantmentSpaceKeys.STATE_CONTAINER.getKey(), PersistentDataType.TAG_CONTAINER,
                this.vanillaTable.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer()
        );
        this.isBlocked = stateContainer.getOrDefault(EnchantmentSpaceKeys.STATE_BLOCKED.getKey(),
                PersistentDataType.BOOLEAN, false);
        this.chargeLevel = stateContainer.getOrDefault(EnchantmentSpaceKeys.STATE_CHARGE_LEVEL.getKey(),
                PersistentDataType.INTEGER, 0);

        //<editor-fold desc="Loading the enchantments">
        var enchantments = stateContainer.get(EnchantmentSpaceKeys.STATE_ENCHANTMENTS.getKey(),
                PersistentDataType.LIST.strings());
        if (enchantments == null) {
            var missing = new Missing();
            for (int i = 0; i < 5; i++) {
                this.rolledEnchantments.add(missing);
            }
        }
        else {
            for (var ench : enchantments) {
                Object enchantment = null;
                if (ench.startsWith("tessertools:")) {
                    enchantment = ENCHANTMENT_MAP.get(ench);
                } else if (ench.startsWith("minecraft:")) {
                    enchantment = ENCHANTMENT_REGISTRY.get(TypedKey.create(RegistryKey.ENCHANTMENT,
                            Key.key(ench)));
                }
                this.rolledEnchantments.add(Objects.requireNonNullElseGet(enchantment, Missing::new));
            }
        }
        //</editor-fold>
    }

    /**
     * Saves the states of the enchantment table to it's {@link PersistentDataContainer}.
     */
    private void saveState() {
        var stateContainer = this.vanillaTable.getPersistentDataContainer().getOrDefault(
                EnchantmentSpaceKeys.STATE_CONTAINER.getKey(), PersistentDataType.TAG_CONTAINER,
                this.vanillaTable.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer()
        );
        stateContainer.set(EnchantmentSpaceKeys.STATE_BLOCKED.getKey(), PersistentDataType.BOOLEAN,
                this.isBlocked);
        stateContainer.set(EnchantmentSpaceKeys.STATE_CHARGE_LEVEL.getKey(), PersistentDataType.INTEGER,
                this.chargeLevel);
        stateContainer.set(EnchantmentSpaceKeys.STATE_ENCHANTMENTS.getKey(), PersistentDataType.LIST.strings(),
                this.getEnchantmentStrings());
        this.vanillaTable.getPersistentDataContainer().set(EnchantmentSpaceKeys.STATE_CONTAINER.getKey(),
                PersistentDataType.TAG_CONTAINER, stateContainer);

        this.vanillaTable.update();
    }

    /**
     * Get the vanilla enchantments which can be put on the {@link ItemStack}.
     * @param item The {@link ItemStack} to check
     * @return A list of vanilla enchantments valid for the given {@link ItemStack}.
     */
    private List<? extends Enchantment> getVanillaEnchantments(ItemStack item) {
        var enchantments = new ArrayList<Enchantment>();

        for (var enchantment : vanillaEnchantments) {
            if ((enchantment.canEnchantItem(item)
                || item.getType() == Material.ENCHANTED_BOOK
                || item.getType() == Material.BOOK)
                && item.getEnchantmentLevel(enchantment) < enchantment.getMaxLevel()
            ) {
                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    /**
     * Get the custom enchantments which can be put on the {@link ItemStack}.
     * @param item The {@link ItemStack} to check
     * @return A list of custom enchantments valid for the given {@link ItemStack}.
     */
    private List<CustomEnchantment> getCustomEnchantments(ItemStack item) {
        var enchantments = new ArrayList<CustomEnchantment>();
        for (var enchantment : CUSTOM_ENCHANTMENTS) {
            if ((item.getType() == Material.ENCHANTED_BOOK
                || item.getType() == Material.BOOK)
                && enchantment.getEnchantmentLevel(item) < enchantment.getMaxLevel()
            ) {
                // Check for the "advanced vanilla enchantments" to not add them if the required level isn't met
                if (enchantment instanceof Unbreaking
                        && item.getEnchantmentLevel(Enchantment.UNBREAKING) < enchantment.getStartLevel()
                ) {continue;}
                else if (enchantment instanceof Protection
                        && item.getEnchantmentLevel(Enchantment.PROTECTION) < enchantment.getStartLevel()
                ) {continue;}
                enchantments.add(enchantment);
            }
            else if (enchantment.canEnchantItem(item)
                && enchantment.getEnchantmentLevel(item) < enchantment.getMaxLevel()
            ) {
                // Check for the "advanced vanilla enchantments" to not add them if the required level isn't met
                if (enchantment instanceof Unbreaking
                    && item.getEnchantmentLevel(Enchantment.UNBREAKING) < enchantment.getStartLevel()
                ) {continue;}
                else if (enchantment instanceof Protection
                        && item.getEnchantmentLevel(Enchantment.PROTECTION) < enchantment.getStartLevel()
                ) {continue;}

                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    /**
     * Creates four random enchantments. The statistical distribution is 3:1 (vanilla:custom).
     * @param item The item that should be enchanted.
     */
    public void startEnchanting(@NotNull ItemStack item) {
        var customs = getCustomEnchantments(item);
        Collections.shuffle(customs);
        List<Object> mixedList = new ArrayList<>(getVanillaEnchantments(item));
        for (int i = mixedList.size() / 3; i > 0; i--) {
            if (customs.isEmpty()) {break;}
            mixedList.add(customs.removeFirst());
        }
        if (mixedList.size() < 4) { return; }
        Collections.shuffle(mixedList);

        this.rolledEnchantments.clear();
        for (int i = 1; i <= 4; i++) {
            this.rolledEnchantments.add(mixedList.removeFirst());
        }
        this.saveState();
        showEnchantments(item);

    }

    /**
     * Creates four random enchantments. The statistical distribution is 3:1 (vanilla:custom).
     * @param item The item that should be enchanted.
     * @param includeCustom Whether to include custom enchantments or not
     */
    public void startEnchanting(@NotNull ItemStack item, boolean includeCustom) {
        if (includeCustom) {startEnchanting(item); return;}
        var vanillas = getVanillaEnchantments(item);
        if (vanillas.size() < 4) { return; }
        Collections.shuffle(vanillas);

        this.rolledEnchantments.clear();
        for (int i = 1; i <= 4; i++) {
            this.rolledEnchantments.add(vanillas.removeFirst());
        }
        this.saveState();
        showEnchantments(item);

    }

    private void showEnchantments(ItemStack itemStack) {
        removeTextDisplays();

        int i = 0;
        for (var enchantment : this.rolledEnchantments) {
            if (enchantment instanceof CustomEnchantment<?>) {
                int nextLevel = getNextEnchantmentLevel(itemStack, (CustomEnchantment) enchantment);
                String roman = nextLevel <= ROMAN_NUMERALS.size() ? ROMAN_NUMERALS.get(nextLevel) : "UNDEFINED";
                spawnText(this.simpleQuarzLocations[i].clone().add(0,1,0),
                        ((CustomEnchantment<?>) enchantment).getDisplayName() + " " + roman,
                        true);
            }
            else if (enchantment instanceof Enchantment) {
                int nextLevel = getNextEnchantmentLevel(itemStack, (Enchantment) enchantment);
                spawnText(this.simpleQuarzLocations[i].clone().add(0,1,0),
                        PlainTextComponentSerializer.plainText().serialize(((Enchantment) enchantment).displayName(nextLevel)),
                        false);
            }
            i++;
        }
    }

    public void removeTextDisplays() {
        for (var display : location.getNearbyEntitiesByType(TextDisplay.class, 6,6,6)) {
            display.remove();
        }
    }

    private void spawnText(Location location, String text, boolean red) {
        // https://www.spigotmc.org/threads/tutorial-holograms-1-8.65183/
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.text(Component.text(text, red ? NamedTextColor.DARK_RED : NamedTextColor.WHITE));
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setPersistent(false);
        });
    }

    /**
     * Gets the next possible level of the vanilla enchantment.
     * @param itemStack The {@link ItemStack} that should be checked
     * @param enchantment The {@link Enchantment} to check
     * @return The next possible level
     */
    private int getNextEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
        return itemStack.getEnchantmentLevel(enchantment) + 1;
    }

    /**
     * Gets the next possible level of the custom enchantment.
     * @param itemStack The {@link ItemStack} that should be checked
     * @param enchantment The {@link CustomEnchantment} to check
     * @return The next possible level
     */
    private int getNextEnchantmentLevel(ItemStack itemStack, CustomEnchantment enchantment) {
        return enchantment.getEnchantmentLevel(itemStack) + 1;
    }

    /**
     * Get a list of the ids of the current rolled enchantments.
     * @return A list of enchantment ids
     */
    private List<String> getEnchantmentStrings() {
        var enchantments = new ArrayList<String>();
        for (var enchantment : this.rolledEnchantments) {
            if (enchantment instanceof Enchantment) {
                enchantments.add(((Enchantment) enchantment).getKey().toString());
            } else if (enchantment instanceof CustomEnchantment) {
                enchantments.add(((CustomEnchantment<?>) enchantment).getID());
            }
        }
        return enchantments;
    }

    /**
     * Get the charge level of the enchanting table.
     * @return The charge level
     */
    public int getChargeLevel() {
        return this.chargeLevel;
    }

    /**
     * Set the charge level of the enchanting table and update the end crystal display. <p>
     * Will ignore anything below 0 and higher than 4.
     * @param level The new charge level
     * @return Whether the level was set
     */
    public boolean setChargeLevel(int level) {
        if (level < 0 || level > 4) {
            this.updateLevelCrystals();
            return false;
        }
        this.chargeLevel = level;
        this.saveState();
        this.updateLevelCrystals();
        return true;
    }

    private void updateLevelCrystals() {
        int remainingCharge = this.chargeLevel;
        for (var crystal : location.getNearbyEntitiesByType(EnderCrystal.class, 7,7,7)) {
            crystal.remove();
        }
        for (var location : simpleLapisLocations) {
            var blockLocation = location.clone().add(0,2,0);
            if (remainingCharge-- > 0){
                var crystal = (EnderCrystal) blockLocation.getWorld().spawn(blockLocation, EnderCrystal.class);
                crystal.setShowingBottom(false);
                crystal.setInvulnerable(true);
            }
        }
    }

    /**
     * Clears the enchantments rolled by the table.<p>
     * Use after enchanting.
     */
    public void clearEnchantments() {
        this.rolledEnchantments.clear();
        for (int i = 1; i <= 4; i++) {
            this.rolledEnchantments.add(new Missing());
        }
        this.saveState();
    }

}
