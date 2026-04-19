package de.bydora.tesserTools.enchantment.enchantments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.bydora.tesserTools.enchantment.enums.EnchantmentSpaceKeys;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import de.bydora.tesserTools.enchantment.util.EquipmentGroups;
import de.bydora.tesserTools.enchantment.util.MaterialArrayMerger;
import de.bydora.tesserTools.enchantment.util.RegistrySets;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Map.entry;
import static net.kyori.adventure.text.Component.text;

public class Decapitater extends CustomEnchantment<EntityDeathEvent> {

    private final static String id = "tessertools:decapitater";
    private final static String displayName = "Enthaupter";
    private final static int maxLevel = 2;
    private final static int minLevel = 1;
    private final static Material[] enchantableItems = MaterialArrayMerger.merge(EquipmentGroups.AXES,
            EquipmentGroups.SWORDS);
    private final static Map<Integer, Integer> levelChances = Map.ofEntries(
            entry(1, 5),
            entry(2, 75)
    );
    private final static Map<EntityType, String> dropHeadEncodings = Map.ofEntries(
            entry(EntityType.BEE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlhYzE2ZjI5NmI0NjFkMDVlYTA3ODVkNDc3MDMzZTUyNzM1OGI0ZjMwYzI2NmFhMDJmMDIwMTU3ZmZjYTczNiJ9fX0="),
            entry(EntityType.SNOW_GOLEM, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FhM2UxN2VmMWIyOWE0Yjg3ZmE0M2RlZTFkYjEyYzQxZmQzOWFhMzg3ZmExM2FmMmEwNzliNWIzNzhmZGU4YiJ9fX0="),
            entry(EntityType.ALLAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2MwMzg5MTc3ZGJhYTkyZjBkNWZmZGY4NDg4NjJjN2Y5YjM2ZGYyMjJmYmZkNzM3ZTI2MzlkYzMwNTllMGNmMyJ9fX0="),
            entry(EntityType.FROG, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUwZDEwNzNkNDFmMTkzNDA1ZDk1YjFkOTQxZjlmZTFhN2ZmMDgwZTM4MTU1ZDdiYjc4MGJiYmQ4ZTg2ZjcwZCJ9fX0="),
            entry(EntityType.FOX, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdlMDA0MzExMWJjNTcwOTA4NTYyNTkxNTU1NzFjNzkwNmU3MDcwNDZkZjA0MWI4YjU3MjcwNGM0NTFmY2Q4MiJ9fX0=")
    );
    private final static Map<EntityType, Material> vanillaHeads = Map.ofEntries(
            entry(EntityType.SKELETON, Material.SKELETON_SKULL),
            entry(EntityType.ZOMBIE, Material.ZOMBIE_HEAD),
            entry(EntityType.PIGLIN, Material.PIGLIN_HEAD),
            entry(EntityType.CREEPER, Material.CREEPER_HEAD),
            entry(EntityType.WITHER, Material.WITHER_SKELETON_SKULL)
    );


    public Decapitater() {
        super(id, maxLevel, displayName, minLevel, enchantableItems, EnchantmentSpaceKeys.ENCH_DECAPITATER.getKey());
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
    public void enchantmentEvent(EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player player
            && getEnchantmentLevel(player.getInventory().getItemInMainHand()) > 0) {
            ItemStack head = getHeadItem(event.getEntityType(), player);
            if (Objects.isNull(head)) {return;}
            Entity eventEntity = event.getEntity();
            int itemLevel = getEnchantmentLevel(player.getInventory().getItemInMainHand());
            Random random = new Random();
            if (random.nextInt(0, 100) < levelChances.get(itemLevel)) {
                // Success!
                eventEntity.getWorld().dropItem(eventEntity.getLocation(), head);
            }
        }
    }

    private static ItemStack getHeadItem(@NotNull EntityType entityType, @NotNull OfflinePlayer player) {
        if (dropHeadEncodings.containsKey(entityType)) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(player);
            PlayerProfile headProfile = headMeta.getPlayerProfile();
            assert headProfile != null;

            // Set the textures of the skull
            headProfile.setProperty(new ProfileProperty("textures",
                    dropHeadEncodings.get(entityType))
            );
            headMeta.setPlayerProfile(headProfile);
            final Component name = text()
                    .content(getEntityName(entityType))
                    .append(text(" Head"))
                    .build();
            headMeta.customName(name);
            head.setItemMeta(headMeta);
            return head;

        } else if (vanillaHeads.containsKey(entityType)) {
            return new ItemStack(vanillaHeads.get(entityType), 1);
        } else {return null;}
    }

    private static String getEntityName(@NotNull EntityType entityType) {
        return entityType.toString().charAt(0)
                + entityType.toString().substring(1).toLowerCase(Locale.getDefault());
    }
}
