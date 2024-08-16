package de.bydora.tesserTools;

import de.bydora.tesserTools.listeners.BlockDropItemListener;
import de.bydora.tesserTools.listeners.InvMoveItemListener;
import de.bydora.tesserTools.listeners.PlayerInteractListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TesserTools extends JavaPlugin {

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        this.getLogger().info("TesserTools is enabled");
        // this.getCommand("tesbug").setExecutor(new CommandTesbug());
        this.registerListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InvMoveItemListener(), this);
        pm.registerEvents(new PlayerInteractListener(), this);
        pm.registerEvents(new BlockDropItemListener(), this);
    }
}
