package de.bydora.tesserTools.enchantment.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
// Thank you, ChatGPT
public class AdjacentBlockFinder {
    private final Set<Material> woodTypes; // Set für schnellere Materialsuche
    private final int maxBlocks; // Maximale Anzahl an abzubauenden Blöcken

    public AdjacentBlockFinder(Material[] woodTypes, int maxBlocks) {
        this.woodTypes = new HashSet<>(Arrays.asList(woodTypes)); // Konvertiere Array in Set
        this.maxBlocks = maxBlocks;
    }

    public List<Location> findConnectedBlocks(Block startBlock) {
        List<Location> foundBlocks = new ArrayList<>();
        Set<Location> visited = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();

        // Überprüfen, ob der Startblock ein Holzblock ist
        if (!isMaterial(startBlock.getType())) {
            return foundBlocks;
        }

        // BFS starten
        toCheck.add(startBlock);
        visited.add(startBlock.getLocation());

        while (!toCheck.isEmpty() && foundBlocks.size() < maxBlocks) {
            Block currentBlock = toCheck.poll();

            // Füge den aktuellen Block zur Ergebnisliste hinzu
            foundBlocks.add(currentBlock.getLocation());

            // Suche benachbarte Blöcke
            for (Block neighbor : getAdjacentBlocks(currentBlock)) {
                if (visited.contains(neighbor.getLocation())) continue; // Überspringe bereits besuchte
                if (!isMaterial(neighbor.getType())) continue; // Überspringe keine Holzblöcke

                toCheck.add(neighbor);
                visited.add(neighbor.getLocation());
            }
        }

        return foundBlocks;
    }

    // Hilfsfunktion: Überprüft, ob der Block ein Holzblock ist
    private boolean isMaterial(Material material) {
        return woodTypes.contains(material);
    }

    // Hilfsfunktion: Gibt die angrenzenden Blöcke zurück
    private List<Block> getAdjacentBlocks(Block block) {
        List<Block> neighbors = new ArrayList<>();
        Location loc = block.getLocation();

        // Alle 6 angrenzenden Blöcke hinzufügen
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(1, 0, 0)));
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(-1, 0, 0)));
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(0, 1, 0)));
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(0, -1, 0)));
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(0, 0, 1)));
        neighbors.add(block.getWorld().getBlockAt(loc.clone().add(0, 0, -1)));

        return neighbors;
    }
}
