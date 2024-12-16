package de.bydora.tesserTools.enchantment.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AreaBlockBreaker {

    private final Set<Material> allowedMaterials; // Erlaubte Materialien

    public AreaBlockBreaker(Set<Material> allowedMaterials) {
        this.allowedMaterials = allowedMaterials;
    }

    /**
     * Sucht alle direkt angrenzenden Blöcke in einem 3x3-Bereich (horizontal oder vertikal).
     *
     * @param startBlock Der Ausgangsblock
     * @param horizontal True für horizontal (2D), False für vertikal ("an der Wand")
     * @return Eine Liste aller angrenzenden Block-Locations
     */
    public List<Location> findDirectNeighbors(Player player, Block startBlock, boolean horizontal) {
        List<Location> result = new ArrayList<>();
        World world = startBlock.getWorld();
        Location loc = startBlock.getLocation();

        if (horizontal) {
            // Suche in einem 3x3-Bereich in der horizontalen Ebene
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue; // Den zentralen Block überspringen
                    Block neighbor = world.getBlockAt(loc.clone().add(dx, 0, dz));
                    if (isAllowed(neighbor)) {
                        result.add(neighbor.getLocation());
                    }
                }
            }
        } else {
            List<Location> eastWestLocations = new ArrayList<>();
            // Suche in einem 3x3-Bereich an einer vertikalen Wand
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dy == 0 && dz == 0) continue; // Den zentralen Block überspringen
                    Block neighbor = world.getBlockAt(loc.clone().add(0, dy, dz));
                    if (isAllowed(neighbor)) {
                        eastWestLocations.add(neighbor.getLocation());
                    }
                }
            }
            List<Location> northSouthLocations = new ArrayList<>();
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dy == 0 && dx == 0) continue; // Den zentralen Block überspringen
                    Block neighbor = world.getBlockAt(loc.clone().add(dx, dy, 0));
                    if (isAllowed(neighbor)) {
                        northSouthLocations.add(neighbor.getLocation());
                    }
                }
            }
            // Check which direction has more blocks... Should be reliable enough
            result = eastWestLocations.size() > northSouthLocations.size() ? eastWestLocations : northSouthLocations;
        }

        return result;
    }

    /**
     * Überprüft, ob der Block in den erlaubten Materialien enthalten ist.
     */
    private boolean isAllowed(Block block) {
        return allowedMaterials.contains(block.getType());
    }
}
