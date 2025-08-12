package de.bydora.tesserTools;

import de.bydora.tesserTools.commands.CommandReallife;
import de.bydora.tesserTools.commands.CommandTesbug;
import de.bydora.tesserTools.enchantment.enchantments.*;
import de.bydora.tesserTools.enchantment.util.EnchantDef;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class TesserToolsBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // Register Commands
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands -> {
            commands.registrar().register(CommandReallife.createCommand().build(),
                    "Helps you into the real life", Collections.singleton("reallife"));
            commands.registrar().register(CommandTesbug.createCommand().build(),
                    "Tessertools debugging command");
        });

        // Registering Enchantments
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(
                event -> {
                    for (var def : getEnchantDefs()) {
                        event.registry().register(
                                EnchantmentKeys.create(Key.key(def.safeId())),
                                b -> b.description(def.description())
                                        .supportedItems(def.supportedItems())
                                        .anvilCost(def.anvilCost())
                                        .maxLevel(def.maxLevel())
                                        .weight(def.weight())
                                        .activeSlots(def.activeSlots())
                                        .exclusiveWith(def.exclusiveWith())
                                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4,4))
                                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4,4))
                        );
                    }
                }
        ));
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new TesserTools();
    }

    private static EnchantDef[] getEnchantDefs() {
        return new EnchantDef[] {
                Abholzung.def(),
                AreaBreak.def(),
                VeinMiner.def(),
                Harvester.def(),
                Magnetic.def(),
                Lifesteal.def(),
                Protection.def(),
                SwiftSneak.def(),
                Unbreaking.def(),
                ProjectileProtection.def(),
                Thorns.def(),
                FireProtection.def(),
                BlastProtection.def(),
                DeepMine.def(),
                Pathing.def(),
                AreaFill.def(),
                SpaceFill.def(),
        };
    }
}
