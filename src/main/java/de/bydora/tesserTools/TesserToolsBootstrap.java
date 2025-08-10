package de.bydora.tesserTools;

import de.bydora.tesserTools.commands.CommandReallife;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class TesserToolsBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands -> {
            commands.registrar().register(CommandReallife.createCommand().build(),
                    "Helps you into the real life", Collections.singleton("reallife"));
        });
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new TesserTools();
    }
}
