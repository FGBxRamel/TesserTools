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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.bydora.tesserTools.enchantment.enchantments.AreaFill.*;

public class SpaceFill extends CustomEnchantment<PlayerInteractEvent>{

    private final static String id = "tessertools:tiefenauffuellung";
    private final static String displayName = "Tiefenauffüllung";
    private final static int maxLevel = 4;
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

    public SpaceFill() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_SPACE_FILL.getKey());
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
        int level = getEnchantmentLevel(event.getItem());
        var player = event.getPlayer();
        var originBlock = event.getClickedBlock();
        var blocks = new ArrayList<>(Arrays.asList(originBlock, originBlock.getRelative(BlockFace.UP)));
        if (level == 2 || level == 4) {blocks.add(originBlock.getRelative(BlockFace.UP, 2));}

        for (var block : blocks) {
            runAreaFill(level, player, block);
        }

    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        final boolean hasAreaFill = new SpaceFill().getEnchantmentLevel(item) > 0;
        return Arrays.stream(enchantableItems).toList().contains(item.getType()) && !hasAreaFill;
    }

    private void runAreaFill(int level, Player player, Block clickedBlock) {
        var airBlocks = getAirBlocks(clickedBlock, player.getFacing(), level > 2 ? 5 : 3);
        var availableItems = getAvailableItems(player.getInventory(), fillBlocks, airBlocks.size());
        if (Objects.isNull(availableItems)) {
            ResourceBundle l18 = ResourceBundle.getBundle("translations.tools", player.locale());
            player.sendMessage(l18.getString("areaEnchNotEnoughItems"));
            return;
        }
        placeBlocks(airBlocks, player.getInventory(), availableItems);
    }
}
