package de.bydora.tesserTools.hoppers.cache;

import de.bydora.tesserTools.TesserTools;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class BoostLevelCache {

    private final static TesserTools plugin = TesserTools.getPlugin(TesserTools.class);
    private static final Random random = new Random();

    private static class CachedBoostLevel {
        final int boostLevel;
        final long expiry;

        CachedBoostLevel(int boostLevel, long ttlMillis) {
            this.boostLevel = boostLevel;
            this.expiry = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }

    private final Map<Location, CachedBoostLevel> cache = new HashMap<>();
    private final NamespacedKey boostKey;

    public BoostLevelCache() {
        this.boostKey = new NamespacedKey(plugin, "boost-level");
    }

    public int getBoostLevel(Inventory inv) {
        Location loc = inv.getLocation();
        if (loc == null) return 0;

        CachedBoostLevel cached = cache.get(loc);
        if (cached != null && !cached.isExpired()) {
            return cached.boostLevel;
        }

        BlockState state = loc.getBlock().getState();
        if (!(state instanceof Hopper hopper)) {
            cache.remove(loc); // Invalid, not a hopper
            return 0;
        }

        PersistentDataContainer container = hopper.getPersistentDataContainer();
        int boostLevel = container.has(boostKey, PersistentDataType.INTEGER)
                ? Objects.requireNonNull(container.get(boostKey, PersistentDataType.INTEGER))
                : 0;

        cache.put(loc, new CachedBoostLevel(boostLevel, getRandomizedTtlMillis()));
        return boostLevel;
    }

    /**
     * Invalidate the cache of the given location
     * @param location The location which should be invalidated
     */
    public void invalidateCache(@NotNull Location location) {
        cache.remove(location);
    }

    private long getRandomizedTtlMillis() {
        // Zwischen 10s und 12s
        int steps = random.nextInt(41);
        return 10_000L + (steps * 50L);
    }
}
