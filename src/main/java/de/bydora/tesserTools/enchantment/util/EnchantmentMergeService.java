package de.bydora.tesserTools.enchantment.util;

import de.bydora.tesserTools.enchantment.enchantments.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public final class EnchantmentMergeService {

    public record Config(
            int reqLevelVanilla,
            int reqLevelCustom,
            int usedLevelVanilla,
            int usedLevelCustom
    ) {}

    private final Config cfg;
    private final Map<Enchantment, CustomEnchantment<?>> advVanillaMap; // ENH_VANILLA_ENCH_MAP
    private final Map<String, CustomEnchantment<?>> customEnchantments; // CUSTOM_ENCHANTMENTS
    private final Set<Class<? extends CustomEnchantment<?>>> excludedCustoms; // EXCLUDED_ENCHANTMENTS

    public EnchantmentMergeService(
            @NotNull Config cfg,
            @NotNull Map<Enchantment, CustomEnchantment<?>> advVanillaMap,
            @NotNull Map<String, CustomEnchantment<?>> customEnchantments,
            @NotNull Set<Class<? extends CustomEnchantment<?>>> excludedCustoms
    ) {
        this.cfg = Objects.requireNonNull(cfg);
        this.advVanillaMap = Objects.requireNonNull(advVanillaMap);
        this.customEnchantments = Objects.requireNonNull(customEnchantments);
        this.excludedCustoms = Objects.requireNonNull(excludedCustoms);
    }

    /**
     * Merge source item enchants onto target item entity. Levels are deducted from player if successful.
     * @param sourceItem The source to take the enchants from
     * @param targetItem The item the enchants should be put onto
     * @param strict Whether checks for conflicts should be run
     * @param player The player to deduct levels from
     * @return True if merged and paid, false otherwise
     */
    public boolean merge(Item sourceItem, Item targetItem, boolean strict, Player player) {
        ItemStack source = sourceItem.getItemStack();
        ItemStack target = targetItem.getItemStack();

        if (strict && hasAnyConflict(source, target)) {
            return false;
        }

        MergePlan plan = buildMergePlan(source, target);
        if (plan.isEmpty()) return false;

        if (!hasEnoughLevel(player, plan.totalUsedLevels)) return false;

        ItemStack merged = applyPlan(new ItemStack(target), plan);
        targetItem.setItemStack(merged);
        deductLevel(player, plan.totalUsedLevels);
        return true;
    }

    // ---------- Planning ----------

    private MergePlan buildMergePlan(ItemStack source, ItemStack target) {
        MergePlan plan = new MergePlan();

        Map<Enchantment, Integer> srcVanilla = getVanillaEnchantments(source);
        Map<Enchantment, Integer> tgtVanilla = getVanillaEnchantments(target);
        planVanilla(srcVanilla, tgtVanilla, plan);

        planCustoms(source, target, plan);
        return plan;
    }

    private void planVanilla(Map<Enchantment, Integer> src, Map<Enchantment, Integer> tgt, MergePlan plan) {
        for (Enchantment ench : src.keySet()) {
            int srcLvl = src.getOrDefault(ench, 0);
            int tgtLvl = tgt.getOrDefault(ench, 0);

            CustomEnchantment<?> adv = advVanillaMap.get(ench); // advanced vanilla?

            if (adv != null) {
                if (tgtLvl == srcLvl && adv.getMaxLevel() > tgtLvl && tgtLvl > 0) {
                    int newLvl = tgtLvl + 1;
                    plan.vanillaTargets.put(ench, newLvl);
                    plan.totalUsedLevels += (newLvl > ench.getMaxLevel()) ? cfg.usedLevelCustom() : cfg.usedLevelVanilla();
                } else {
                    int newLvl = Math.max(srcLvl, tgtLvl);
                    if (newLvl > 0 && newLvl != tgtLvl) {
                        newLvl = Math.min(newLvl, adv.getMaxLevel());
                        plan.vanillaTargets.put(ench, newLvl);
                        plan.totalUsedLevels += (newLvl > ench.getMaxLevel()) ? cfg.usedLevelCustom() : cfg.usedLevelVanilla();
                    }
                }
            } else {
                if (tgtLvl == srcLvl && ench.getMaxLevel() > tgtLvl && tgtLvl > 0) {
                    int newLvl = tgtLvl + 1;
                    plan.vanillaTargets.put(ench, newLvl);
                    plan.totalUsedLevels += cfg.usedLevelVanilla();
                } else {
                    int newLvl = Math.max(srcLvl, tgtLvl);
                    if (newLvl > 0 && newLvl != tgtLvl) {
                        newLvl = Math.min(newLvl, ench.getMaxLevel());
                        plan.vanillaTargets.put(ench, newLvl);
                        plan.totalUsedLevels += cfg.usedLevelVanilla();
                    }
                }
            }
        }
    }

    private void planCustoms(ItemStack source, ItemStack target, MergePlan plan) {
        for (CustomEnchantment<?> ce : customEnchantments.values()) {
            if (excludedCustoms.contains(ce.getClass())) continue;

            int srcLvl = ce.getEnchantmentLevel(source);
            int tgtLvl = ce.getEnchantmentLevel(target);

            if (tgtLvl == srcLvl && tgtLvl > 0 && ce.getMaxLevel() > tgtLvl) {
                int newLvl = tgtLvl + 1;
                plan.customTargets.put(ce, newLvl);
                plan.totalUsedLevels += cfg.usedLevelCustom();
                continue;
            }
            if (srcLvl > tgtLvl) {
                int newLvl = Math.min(srcLvl, ce.getMaxLevel());
                plan.customTargets.put(ce, newLvl);
                plan.totalUsedLevels += cfg.usedLevelCustom();
            }
        }
    }

    // ---------- Conflicts ----------

    private boolean hasAnyConflict(ItemStack source, ItemStack target) {
        Map<Enchantment, Integer> srcVan = getVanillaEnchantments(source);
        Map<Enchantment, Integer> tgtVan = getVanillaEnchantments(target);
        boolean targetIsBook = target.getType() == Material.ENCHANTED_BOOK;

        for (Enchantment e : srcVan.keySet()) {
            if (!targetIsBook && !e.canEnchantItem(target)) return falseConflict();
            for (Enchantment existing : tgtVan.keySet()) {
                if (e != existing && e.conflictsWith(existing)) return falseConflict();
            }
        }
        for (CustomEnchantment<?> ce : customEnchantments.values()) {
            if (excludedCustoms.contains(ce.getClass())) continue;
            int levelOnSource = ce.getEnchantmentLevel(source);
            if (levelOnSource > 0 && !targetIsBook && !ce.canEnchantItem(target)) {
                return falseConflict();
            }
        }
        return false;
    }
    private static boolean falseConflict() { return true; }

    // ---------- Apply ----------

    private ItemStack applyPlan(ItemStack target, MergePlan plan) {
        // Vanilla first
        for (Map.Entry<Enchantment, Integer> e : plan.vanillaTargets.entrySet()) {
            applyVanillaEnchantment(target, e.getKey(), e.getValue());
        }
        // Customs
        for (Map.Entry<CustomEnchantment<?>, Integer> e : plan.customTargets.entrySet()) {
            applyCustomEnchantment(target, e.getKey(),4, e.getValue());
        }
        return target;
    }

    // ---------- Helpers ----------

    private static boolean hasEnoughLevel(@Nullable Player player, int required) {
        return player != null && player.getLevel() >= required;
    }
    private static void deductLevel(@Nullable Player player, int amount) {
        if (player != null && amount > 0) player.setLevel(Math.max(0, player.getLevel() - amount));
    }

    private static Map<Enchantment, Integer> getVanillaEnchantments(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK) {
            var meta = (EnchantmentStorageMeta) item.getItemMeta();
            return meta.getStoredEnchants();
        }
        return item.getEnchantments();
    }

    private static ItemStack applyVanillaEnchantment(ItemStack stack, Enchantment ench, int level) {
        if (stack.getType() == Material.BOOK) stack = new ItemStack(Material.ENCHANTED_BOOK);
        if (stack.getType() == Material.ENCHANTED_BOOK) {
            var meta = (EnchantmentStorageMeta) stack.getItemMeta();
            meta.removeStoredEnchant(ench);
            meta.addStoredEnchant(ench, level, true);
            stack.setItemMeta(meta);
        } else {
            stack.addUnsafeEnchantment(ench, level);
        }
        return stack;
    }

    @SuppressWarnings("SameParameterValue")
    private static ItemStack applyCustomEnchantment(ItemStack stack, CustomEnchantment<?> ench, int chargeLevel, int level) {
        if (chargeLevel <= 0) return stack;
        return ench.enchantItem(stack, level);
    }

    // ---------- DTO ----------

    private static final class MergePlan {
        final Map<Enchantment, Integer> vanillaTargets = new HashMap<>();
        final Map<CustomEnchantment<?>, Integer> customTargets = new LinkedHashMap<>();
        int totalUsedLevels = 0;
        boolean isEmpty() { return vanillaTargets.isEmpty() && customTargets.isEmpty(); }
    }
}
