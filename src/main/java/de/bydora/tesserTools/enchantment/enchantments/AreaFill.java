package de.bydora.tesserTools.enchantment.enchantments;

import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AreaFill extends CustomEnchantment<PlayerInteractEvent> {

    private final static String id = "tessertools:flaechenfuellung";
    private final static String displayName = "Flächenfüllung";
    private final static int maxLevel = 2;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = new Material[]{Material.BUNDLE};
    private final static Material[] fillBlocks = new Material[] {
            Material.STONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
            Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.GRAVEL, Material.RED_SAND, Material.SANDSTONE,
            Material.RED_SANDSTONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE, Material.TUFF,
            Material.CALCITE, Material.BASALT, Material.TERRACOTTA, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
            Material.NETHERRACK, Material.BLACKSTONE, Material.SOUL_SAND, Material.END_STONE, Material.SOUL_SOIL,
            Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.PODZOL, Material.COARSE_DIRT, Material.MYCELIUM,
            Material.ROOTED_DIRT, Material.MUD, Material.CLAY, Material.SNOW_BLOCK, Material.WATER_BUCKET
    };

    public AreaFill() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_AREA_FILL.getKey());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static EnchantDef def() {
        var supported = RegistrySets.fromMaterials(enchantableItems);
        var description = Component.translatable(getBaseTranslationKey(id) + ".description");
        return new EnchantDef(
                sanitizeString(id),
                description,
                supported,
                1,
                maxLevel,
                10,
                Set.of(),
                RegistrySet.keySet(RegistryKey.ENCHANTMENT)
        );
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void enchantmentEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || Objects.isNull(event.getItem())
                || getEnchantmentLevel(event.getItem()) <= 0
                || Objects.isNull(event.getClickedBlock())
                || event.getClickedBlock().getType() == Material.AIR
        ) {
            return;
        }
        final int level = getEnchantmentLevel(event.getItem());
        final var player = event.getPlayer();
        var airBlocks = getAirBlocks(event.getClickedBlock(), player.getFacing(), level > 1 ? 5 : 3);
        var availableItems = getAvailableItems(player.getInventory(), fillBlocks, airBlocks.size());
        if (Objects.isNull(availableItems)) {
            ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", player.locale());
            player.sendMessage(l18.getString("areaEnchNotEnoughItems"));
            return;
        }
        placeBlocks(airBlocks, player.getInventory(), availableItems);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        final boolean hasSpaceFill = new AreaFill().getEnchantmentLevel(item) > 0;
        return Arrays.stream(enchantableItems).toList().contains(item.getType()) && !hasSpaceFill;
    }

    /**
     * Returns all air blocks in a horizontal field starting at the clicked block (bottom-left),
     * expanding in facing direction and to the right (from player's perspective)
     *
     * @param clickedBlock The clicked block (acts as bottom-left corner)
     * @param facing The direction the player is facing (NORTH, EAST, SOUTH, WEST)
     * @param size The field size (e.g., 3 or 5). Must be >= 1
     * @return List of all air blocks in the defined area
     */
    public static List<Block> getAirBlocks(Block clickedBlock, BlockFace facing, int size) {
        if (size < 1) throw new IllegalArgumentException("Size must be at least 1");

        List<Block> result = new ArrayList<>();

        BlockFace right = getRightOf(facing); // from player perspective

        for (int dz = 0; dz < size; dz++) {        // forward direction (rows)
            for (int dx = 0; dx < size; dx++) {    // right direction (columns)
                Block b = clickedBlock
                        .getRelative(right, dx)
                        .getRelative(facing, dz);

                if (b.getType() == Material.AIR) {
                    result.add(b);
                }
            }
        }

        return result;
    }

    /**
     * Returns the direction to the player's right based on their facing direction.
     */
    private static BlockFace getRightOf(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> BlockFace.EAST; // Fallback
        };
    }

    /**
     * Returns a map of available items and their count; null if there are not enough.
     * @param inventory The inventory of the player
     * @param allowedMaterials Which materials should be searched for
     * @param requiredBlockAmount Which amount of blocks (not materials!) is needed
     * @return Map of available items and their count; null if not enough exist
     */
    public static Map<Material, Integer> getAvailableItems(PlayerInventory inventory, Material[] allowedMaterials,
                                                            int requiredBlockAmount) {
        int totalAmount = 0; // Total amount of relevant blocks the player has
        Map<Material, Integer> availableMaterial = new HashMap<>();
        for (var material : allowedMaterials) {
            var inventoryItemStacks = inventory.all(material);
            if (!inventoryItemStacks.isEmpty()) {
                for (var stack : inventoryItemStacks.values()) {
                    totalAmount += stack.getAmount();
                    int newAmount = Objects.requireNonNullElse(availableMaterial.get(stack.getType()), 0)
                            + stack.getAmount();
                    availableMaterial.put(stack.getType(), newAmount);
                }
            }
        }
        if (totalAmount < requiredBlockAmount) {
            return null;
        }
        return availableMaterial;
    }

    /**
     * (Re)places the given blocks with a random Material from the given Materials.
     * Attention: This does not yield any resources, the blocks will be overwritten!
     * @param blocks A list of blocks to (re)place
     * @param inventory The inventory of the player
     * @param availableMaterial A map of the available material and their count
     */
    public static void placeBlocks(List<Block> blocks, PlayerInventory inventory,
                                    Map<Material, Integer> availableMaterial) {
        var items = availableMaterial.keySet().toArray();
        for (var block : blocks) {
            // Get random item, check if it is the last one, if yes remove it, if not decrement it
            Random generator = new Random();
            Material randomItem = (Material) items[generator.nextInt(items.length)];
            int remainingAmount = availableMaterial.get(randomItem);
            if (remainingAmount == 1) {
                availableMaterial.remove(randomItem);
                items = availableMaterial.keySet().toArray();
            } else {
                availableMaterial.put(randomItem, remainingAmount - 1);
            }

            ItemStack removeStack = new ItemStack(randomItem, 1);
            if (randomItem == Material.LAVA_BUCKET) {randomItem = Material.LAVA;}
            else if (randomItem == Material.WATER_BUCKET) {randomItem = Material.WATER;}
            block.setType(randomItem);
            inventory.removeItemAnySlot(removeStack);
        }
    }

}
