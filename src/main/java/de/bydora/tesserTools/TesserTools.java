package de.bydora.tesserTools;

import de.bydora.tesserTools.commands.CommandTesbug;
import de.bydora.tesserTools.listeners.InvMoveItemListener;
import de.bydora.tesserTools.listeners.PlayerInteractListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TesserTools extends JavaPlugin {

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        this.getLogger().info("TesserTools is enabled");
        this.getCommand("tesbug").setExecutor(new CommandTesbug());
        getServer().getPluginManager().registerEvents(new InvMoveItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
