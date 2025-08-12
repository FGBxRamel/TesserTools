package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Pathing extends CustomEnchantment<PlayerInteractEvent> {

    private final static String id = "tessertools:strassenbauer";
    private final static String displayName = "Straßenbauer";
    private final static int maxLevel = 3;
    private final static int minLevel = 1;
    private final static NamespacedKey key = EnchantmentSpaceKeys.ENCH_PATHING.getKey();
    private final static Material[] enchantableItems = EquipmentGroups.HOES;
    private final static List<Material[]> levelBlocks = new ArrayList<>(List.of(
            new Material[] { Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.GRASS_BLOCK,
                    Material.PODZOL, Material.DIRT_PATH, Material.MOSS_BLOCK, Material.MUD,
                    Material.MUDDY_MANGROVE_ROOTS},
            new Material[] { Material.PACKED_MUD, Material.MUD_BRICKS, Material.BROWN_CONCRETE_POWDER,
                    Material.BROWN_WOOL, Material.BROWN_MUSHROOM_BLOCK, Material.GRANITE, Material.TUFF,
                    Material.COBBLESTONE, Material.DIRT_PATH},
            new Material[] { Material.COBBLESTONE, Material.STONE_BRICKS, Material.MOSSY_COBBLESTONE,
                    Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.STONE, Material.ANDESITE,
                    Material.TUFF, Material.GRAVEL}
    ));

    public Pathing() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, key);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void enchantmentEvent(PlayerInteractEvent event) {
        if (!event.getAction().isLeftClick()
            || Objects.isNull(event.getItem())
            || getEnchantmentLevel(event.getItem()) <= 0
            || Objects.isNull(event.getClickedBlock())
            || event.getClickedBlock().getType() == Material.AIR
        ) {
            return;
        }
        int level = getEnchantmentLevel(event.getItem());
        if (level > 3) { level = 3; } // Limit level at 3 as we only defined blocks for 3 levels

        var availableMaterial = AreaFill.getAvailableItems(event.getPlayer().getInventory(), levelBlocks.get(level - 1),
        9);
        if (Objects.isNull(availableMaterial)) {
            ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", event.getPlayer().locale());
            event.getPlayer().sendMessage(l18.getString("pathingNotEnoughItems"));
            return;
        }

        var blocks = areaFinder(event.getClickedBlock().getLocation(), level);
        AreaFill.placeBlocks(blocks.stream().toList(), event.getPlayer().getInventory(), availableMaterial);
    }

    /**
     * Gets all blocks relevant for pathing with the middleBlock as origin.
     * @param middleBlock The origin from where to search.
     * @return A set of locations of the relevant blocks
     */
    private static @NotNull Set<Block> areaFinder(@NotNull Location middleBlock, int enchantmentLevel) {
        Set<Block> result = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location neighborLocation = middleBlock.clone().add(dx, 0, dz);
                Block neighbor = getVerticalMax(neighborLocation);
                if (Objects.isNull(neighbor)
                    || Arrays.stream(levelBlocks.get(enchantmentLevel - 1)).toList().contains(neighbor.getType())
                ) {continue;}
                result.add(neighbor);
            }
        }
        return result;
    }

    /**
     * Checks +-1dy vertically from the given location and gives back a location which should be used, or `null`.
     * If there's a block above, and it's the highest block (has no block above it) it will return this.
     * If not and the origin is Air, it will check the block below.
     * If the block below is Air, it will return `null`
     * @param origin The block to check from
     * @return Location of the block-to-take, or `null`
     */
    private static Block getVerticalMax(@NotNull Location origin) {
        final World world = origin.getWorld();
        final Block originBlock = world.getBlockAt(origin);
        final Block blockAbove = world.getBlockAt(origin.clone().add(0,1,0));
        final Block blockAboveAbove = world.getBlockAt(origin.clone().add(0,2,0));
        final Block blockBelow = world.getBlockAt(origin.clone().add(0,-1,0));

        if (blockAbove.getType() != Material.AIR
            && blockAboveAbove.getType() == Material.AIR
        ) {
            return blockAbove;
        } else if (originBlock.getType() != Material.AIR) {
            return originBlock;
        } else if (blockBelow.getType() != Material.AIR
        ) {
            return blockBelow;
        } else {return null;}
    }
}
