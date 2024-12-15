package de.bydora.tesserTools.enchantment.blocks;

import com.destroystokyo.paper.ParticleBuilder;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.exceptions.NotAnEnchantmentTableException;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExtEnchantingTable {

    private final Location[] simpleQuarzLocations;
    private final Location[] simpleLapisLocations;
    private final Location location;
    private final List<Enchantment> vanillaEnchantments;
    private boolean isBlocked;
    private int chargeLevel;
    private final PersistentDataContainer stateContainer;

    public ExtEnchantingTable(@NotNull Location location) {

        this.location = location;

        if (location.getBlock().getState() instanceof EnchantingTable table) {
            this.stateContainer = table.getPersistentDataContainer().getOrDefault(
                    EnchantmentSpaceKeys.STATE_CONTAINER.getKey(), PersistentDataType.TAG_CONTAINER,
                    table.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer()
            );
            this.isBlocked = this.stateContainer.getOrDefault(EnchantmentSpaceKeys.STATE_BLOCKED.getKey(),
                    PersistentDataType.BOOLEAN, false);
            this.chargeLevel = this.stateContainer.getOrDefault(EnchantmentSpaceKeys.STATE_CHARGE_LEVEL.getKey(),
                    PersistentDataType.INTEGER, 0);
        }
        else {
            throw new NotAnEnchantmentTableException("You can only make an instance of ExtEnchantingTable from" +
                    "an Enchanting Table!");
        }

        final Location calcQuarzLocation = location.clone().toCenterLocation();
        this.simpleQuarzLocations = new Location[]{
                calcQuarzLocation.add(4, 1, 4).clone(),
                calcQuarzLocation.add(-8, 0, 0).clone(),
                calcQuarzLocation.add(0, 0, -8).clone(),
                calcQuarzLocation.add(8, 0, 0).clone()
        };
        // Reset so the calc is right
        final Location calcLapisLocation = location.clone().toCenterLocation();
        this.simpleLapisLocations = new Location[]{
                calcLapisLocation.add(6, 1, 0).clone(),
                calcLapisLocation.add(-12, 0, 0).clone(),
                calcLapisLocation.add(6, 0, -6).clone(),
                calcLapisLocation.add(0, 0, 12).clone()
        };

        this.vanillaEnchantments = new ArrayList<>();
        for (Enchantment enchantment : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)) {
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
        isBlocked = blocked;
        this.stateContainer.set(EnchantmentSpaceKeys.STATE_BLOCKED.getKey(), PersistentDataType.BOOLEAN, blocked);
    }
}
