package de.bydora.tesserTools.enchantment.blocks;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.TesserTools;
import de.bydora.tesserTools.enchantment.enchantments.*;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.exceptions.NotAnEnchantmentTableException;
import de.bydora.tesserTools.enchantment.util.RolledEnchantment;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
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
import java.util.stream.Collectors;

import static java.util.Map.entry;

@SuppressWarnings({"rawtypes"})
public class ExtEnchantingTable {

    public static final Registry<@NotNull Enchantment> ENCHANTMENT_REGISTRY = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT);

    private static final NamespacedKey TT_CRYSTAL = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
            "tt_crystal");
    private static final NamespacedKey TT_DISPLAY = new NamespacedKey(TesserTools.getPlugin(TesserTools.class),
            "tt_display");
    private static final Map<String, CustomEnchantment<?>> ENCHANTMENT_MAP = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap();
    private static final Map<Integer, String> ROMAN_NUMERALS = Map.ofEntries(
            entry(1, "I"),
            entry(2, "II"),
            entry(3, "III"),
            entry(4, "IV"),
            entry(5, "V"),
            entry(6, "VI")
    );
    private final static Collection<CustomEnchantment<?>> CUSTOM_ENCHANTMENTS = TesserTools.getPlugin(TesserTools.class)
            .getEnchantmentMap().values();
    private final static Map<Class<? extends EnhVanillaEnch>, Enchantment> ENH_VANILLA_ENCH_MAP =  Map.of(
            Protection.class, Enchantment.PROTECTION,
            SwiftSneak.class, Enchantment.SWIFT_SNEAK,
            Unbreaking.class, Enchantment.UNBREAKING,
            ProjectileProtection.class, Enchantment.PROJECTILE_PROTECTION,
            Thorns.class, Enchantment.THORNS,
            FireProtection.class, Enchantment.FIRE_PROTECTION,
            BlastProtection.class, Enchantment.BLAST_PROTECTION
    );
    private final Location[] simpleQuartzLocations;
    private final Location[] simpleLapisLocations;
    private final Location location;
    private final List<Enchantment> vanillaEnchantments;
    private boolean isBlocked;
    private int chargeLevel;
    private final List<RolledEnchantment> rolledEnchantments = new ArrayList<>();
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

        final Location calcQuartzLocation = location.clone().toCenterLocation();
        this.simpleQuartzLocations = new Location[]{
                calcQuartzLocation.add(4, 0, 4).clone(),
                calcQuartzLocation.add(-8, 0, 0).clone(),
                calcQuartzLocation.add(0, 0, -8).clone(),
                calcQuartzLocation.add(8, 0, 0).clone()
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
            if (!enchantment.getKey().asMinimalString().contains("tessertools")) {
                vanillaEnchantments.add(enchantment);
            }
        }
    }

    /**
     * Returns whether the extended enchantment block has a valid quartz constellation.
     * @return Whether it has a valid quartz constellation
     */
    public boolean hasValidQuartz() {
        return Arrays.stream(simpleQuartzLocations)
                .map(Location::toBlockLocation)
                .allMatch(loc -> loc.getWorld() != null
                        && loc.getBlock().getType() == Material.CHISELED_QUARTZ_BLOCK);
    }

    /**
     * Returns whether the extended enchantment block has a valid lapis constellation.
     * @return Whether it has a valid lapis constellation
     */
    public boolean hasValidLapis() {
        return Arrays.stream(simpleLapisLocations)
                .map(Location::toBlockLocation)
                .allMatch(loc -> loc.getWorld() != null
                        && loc.getBlock().getType() == Material.LAPIS_BLOCK
                        && loc.clone().add(0,1,0).getBlock().getType() == Material.LAPIS_BLOCK);
    }

    /**
     * Whether the table is valid for enchantments
     * @return Whether it is valid for enchantments
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        return (this.hasValidLapis() && this.hasValidQuartz());
    }

    public void spawnParticles() {
        final ParticleBuilder builder = getBaseParticle(this.location);
        builder.color(255, 255,255);
        for (Location quartz : simpleQuartzLocations) {
            builder.location(quartz);
            builder.spawn();
        }
        builder.color(38,97,156);
        for (Location lapis : simpleLapisLocations) {
            builder.location(lapis);
            builder.spawn();
        }
    }

    private static @NotNull ParticleBuilder getBaseParticle(@NotNull Location originLocation) {
        return new ParticleBuilder(Particle.DUST)
                .location(originLocation).clone()
                .offset(.2,.2,.2)
                .receivers(160)
                .count(10);
    }

    @SuppressWarnings("unused")
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
     * Get the {@link CustomEnchantment} or {@link Enchantment} of the quartz block given.
     * @param loc The location of the quartz block
     * @return The {@link CustomEnchantment} or {@link Enchantment} or null
     */
    public @Nullable RolledEnchantment getEnchantment(@NotNull Location loc) {
        var blockLoc = loc.toBlockLocation();
        for (int i = 0; i < simpleQuartzLocations.length; i++) {
            if (simpleQuartzLocations[i].toBlockLocation().equals(blockLoc)) {
                return rolledEnchantments.get(i);
            }
        }
        return null;
    }

    /**
     * Loads the state of the enchantment table from it's {@link PersistentDataContainer}.
     */
    @SuppressWarnings({"PatternValidation"})
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
            for (int i = 0; i < 4; i++) {rolledEnchantments.add(new RolledEnchantment.Missing());}
        }
        else {
            for (var ench : enchantments) {
                if (ench.startsWith("tessertools:")) {
                    rolledEnchantments.add(new RolledEnchantment.Custom(ENCHANTMENT_MAP.get(ench)));
                } else if (ench.startsWith("minecraft:")) {
                    var vanilla = ENCHANTMENT_REGISTRY.get(TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(ench)));
                    rolledEnchantments.add(new RolledEnchantment.Vanilla(vanilla));
                } else {rolledEnchantments.add(new RolledEnchantment.Missing());}
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
            var vanillaEnch = ENH_VANILLA_ENCH_MAP.get(enchantment.getClass());
            if ((item.getType() == Material.ENCHANTED_BOOK
                || item.getType() == Material.BOOK)
                && enchantment.getEnchantmentLevel(item) < enchantment.getMaxLevel()
            ) {
                // Check for the "advanced vanilla enchantments" to not add them if the required level isn't met
                if (Objects.nonNull(vanillaEnch)
                        && item.getEnchantmentLevel(vanillaEnch) < enchantment.getMinLevel()
                ) {continue;}
                enchantments.add(enchantment);
            }
            else if (enchantment.canEnchantItem(item)
                && enchantment.getEnchantmentLevel(item) < enchantment.getMaxLevel()
            ) {
                // Check for the "advanced vanilla enchantments" to not add them if the required level isn't met
                if (Objects.nonNull(vanillaEnch)
                    && item.getEnchantmentLevel(vanillaEnch) < enchantment.getMinLevel()
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
    public void startEnchanting(@NotNull ItemStack item, Locale lang) {
        var customs = getCustomEnchantments(item);
        Collections.shuffle(customs);
        List<RolledEnchantment> mixedList = getVanillaEnchantments(item)
                .stream()
                .map(RolledEnchantment.Vanilla::new).collect(Collectors.toList());

        for (int i = mixedList.size() / 3; i > 0; i--) {
            if (customs.isEmpty()) {break;}
            mixedList.add(new RolledEnchantment.Custom(customs.removeFirst()));
        }
        if (mixedList.size() < 4) { return; }
        Collections.shuffle(mixedList);

        rolledEnchantments.clear();
        for (int i = 1; i <= 4; i++) {
            rolledEnchantments.add(mixedList.removeFirst());
        }
        saveState();
        showEnchantments(item, lang);
    }

    /**
     * Creates four random enchantments. The statistical distribution is 3:1 (vanilla:custom).
     * @param item The item that should be enchanted.
     * @param includeCustom Whether to include custom enchantments or not
     */
    public void startEnchanting(@NotNull ItemStack item, boolean includeCustom, Locale lang) {
        if (includeCustom) {startEnchanting(item, lang); return;}
        var vanillas = getVanillaEnchantments(item);
        if (vanillas.size() < 4) { return; }
        Collections.shuffle(vanillas);

        this.rolledEnchantments.clear();
        for (int i = 1; i <= 4; i++) {
            rolledEnchantments.add(new RolledEnchantment.Vanilla(vanillas.removeFirst()));
        }
        this.saveState();
        showEnchantments(item, lang);

    }

    private void showEnchantments(ItemStack itemStack, Locale lang) {
        removeTextDisplays();

        int i = 0;
        for (var enchantment : rolledEnchantments) {
            switch (enchantment) {
                case RolledEnchantment.Custom c -> {
                    int nextLevel = getNextEnchantmentLevel(itemStack, c.enchantment());
                    // Get roman numeral or just the arabic number if no numeral is defined
                    String roman = nextLevel <= ROMAN_NUMERALS.size() ? ROMAN_NUMERALS.get(nextLevel) :
                            Integer.toString(nextLevel);
                    spawnText(this.simpleQuartzLocations[i].clone().add(0,1,0),
                            (c.enchantment()).getDisplayName(lang) + " " + roman,
                            true);
                }
                case RolledEnchantment.Vanilla v -> {
                    ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", lang);
                    var level = ROMAN_NUMERALS.get(getNextEnchantmentLevel(itemStack, v.enchantment()));
                    String enchName = l18.getString(v.enchantment().getKey().asMinimalString()) + " " + level;
                    spawnText(this.simpleQuartzLocations[i].clone().add(0,1,0),
                            enchName,
                            false);
                }
                default -> spawnText(this.simpleQuartzLocations[i].clone().add(0,1,0),
                        "ERROR: MISSING",
                        true);
            }
            i++;
        }
    }

    public void removeTextDisplays() {
        for (var display : location.getNearbyEntitiesByType(TextDisplay.class, 6,6,6)) {
            if (display.getPersistentDataContainer().has(TT_DISPLAY)) display.remove();
        }
    }

    private void spawnText(Location location, String text, boolean red) {
        // https://www.spigotmc.org/threads/tutorial-holograms-1-8.65183/
        location.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.text(Component.text(text, red ? NamedTextColor.DARK_RED : NamedTextColor.WHITE));
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setPersistent(false);
            entity.getPersistentDataContainer().set(TT_DISPLAY, PersistentDataType.BOOLEAN, true);
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
     * @param enchantment The {@link CustomEnchantment} to checktext displays
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
        for (var enchantment : rolledEnchantments) {
            switch (enchantment) {
                case RolledEnchantment.Custom c -> enchantments.add(c.enchantment().getID());
                case RolledEnchantment.Vanilla v -> enchantments.add(v.enchantment().getKey().toString());
                default -> {}
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
    @SuppressWarnings("UnusedReturnValue")
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
            if (crystal.getPersistentDataContainer().has(TT_CRYSTAL)) crystal.remove();
        }
        for (var location : simpleLapisLocations) {
            var blockLocation = location.clone().add(0,2,0);
            if (remainingCharge-- > 0){
                var crystal = blockLocation.getWorld().spawn(blockLocation, EnderCrystal.class);
                crystal.setShowingBottom(false);
                crystal.setInvulnerable(true);
                crystal.getPersistentDataContainer().set(TT_CRYSTAL, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    /**
     * Clears the enchantments rolled by the table.<p>
     * Use after enchanting.
     */
    public void clearEnchantments() {
        this.rolledEnchantments.clear();
        for (int i = 1; i < 4; i++) {this.rolledEnchantments.add(new RolledEnchantment.Missing());}
        this.removeTextDisplays();
        this.saveState();
    }

}
