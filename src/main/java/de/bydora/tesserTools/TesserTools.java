package de.bydora.tesserTools;

import de.bydora.tesserTools.commands.CommandEnchant;
import de.bydora.tesserTools.enchantment.enchantments.*;
import de.bydora.tesserTools.enchantment.listeners.BlockPlaceListener;
import de.bydora.tesserTools.enchantment.listeners.PlayerDropItemListener;
import de.bydora.tesserTools.listeners.BlockDropItemListener;
import de.bydora.tesserTools.listeners.InvMoveItemListener;
import de.bydora.tesserTools.listeners.PlayerInteractListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TesserTools extends JavaPlugin {

    private final PluginManager pm = getServer().getPluginManager();
    private List<String> enchantmentIDs = new ArrayList<String>();
    private Map<String, CustomEnchantment> enchantmentMap = new HashMap<>();

    @Override
    public void onEnable() {
        this.registerCommands();
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
                new InvMoveItemListener(),
                new de.bydora.tesserTools.enchantment.listeners.PlayerInteractListener(),
                new PlayerInteractListener(),
                new BlockDropItemListener(),
                new BlockPlaceListener(),
                new PlayerDropItemListener()
        };
        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    private void registerCommands() {
        // this.getCommand("tesbug").setExecutor(new CommandTesbug());
        this.getCommand("enchant").setExecutor(new CommandEnchant());
    }

    @SuppressWarnings("rawtypes")
    private void registerEnchantments() {
        final CustomEnchantment[] enchantments = new CustomEnchantment[] {
                new Abholzung(),
                new AreaBreak(),
                new VeinMiner(),
                new Harvester(),
                new Magnetic(),
                new FastAttack(),
                new Lifesteal(),
                new Protection(),
                new SwiftSneak(),
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
}
