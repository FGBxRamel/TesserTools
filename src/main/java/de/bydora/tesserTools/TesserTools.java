package de.bydora.tesserTools;

import de.bydora.tesserTools.cache.BoostLevelCache;
import de.bydora.tesserTools.commands.CommandReallife;
import de.bydora.tesserTools.commands.CommandShowEnch;
import de.bydora.tesserTools.enchantment.enchantments.*;
import de.bydora.tesserTools.enchantment.listeners.BlockPlaceListener;
import de.bydora.tesserTools.enchantment.listeners.EntityPickupEvent;
import de.bydora.tesserTools.enchantment.listeners.PlayerDropItemListener;
import de.bydora.tesserTools.enchantment.listeners.PrepareAnvilListener;
import de.bydora.tesserTools.listeners.BlockDropItemListener;
import de.bydora.tesserTools.listeners.InvMoveItemListener;
import de.bydora.tesserTools.listeners.PlayerInteractListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@SuppressWarnings("rawtypes")
public final class TesserTools extends JavaPlugin {

    private final PluginManager pm = getServer().getPluginManager();
    private final List<String> enchantmentIDs = new ArrayList<>();
    private final Map<String, CustomEnchantment> enchantmentMap = new HashMap<>();
    private BoostLevelCache boostLevelCache;

    @Override
    public void onEnable() {
        this.boostLevelCache = new BoostLevelCache();
        this.registerListeners();
        this.registerEnchantments();
        this.getLogger().info("TesserTools is enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners() {
        Listener[] listeners = new Listener[] {
                new InvMoveItemListener(this),
                new de.bydora.tesserTools.enchantment.listeners.PlayerInteractListener(),
                new PlayerInteractListener(),
                new BlockDropItemListener(),
                new BlockPlaceListener(),
                new PlayerDropItemListener(),
                new EntityPickupEvent(),
                new PrepareAnvilListener(),
        };
        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    @SuppressWarnings("rawtypes")
    private void registerEnchantments() {
        final CustomEnchantment[] enchantments = new CustomEnchantment[] {
                new Abholzung(),
                new AreaBreak(),
                new VeinMiner(),
                new Harvester(),
                new Magnetic(),
                new Lifesteal(),
                new Protection(),
                new SwiftSneak(),
                new Unbreaking(),
                new ProjectileProtection(),
                new Thorns(),
                new FireProtection(),
                new BlastProtection(),
                new DeepMine(),
                new Pathing(),
                new AreaFill(),
                new SpaceFill(),
        };
        for (CustomEnchantment enchantment : enchantments) {
            this.enchantmentIDs.add(enchantment.getID());
            this.enchantmentMap.put(enchantment.getID(), enchantment);
            pm.registerEvents(enchantment, this);
        }
    }

    public List<String> getEnchantmentIDs() {
        return enchantmentIDs;
    }

    public Map<String, CustomEnchantment> getEnchantmentMap() {
        return enchantmentMap;
    }

    public BoostLevelCache getBoostLevelCache() {
        return boostLevelCache;
    }
}
