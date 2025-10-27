package de.bydora.tesserTools.gamemodes.listeners;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class GameModeChangeListener implements Listener {

    final static Component noPermissionComp = text()
            .content("No permission for specified gamemode!")
            .color(color(0xAA0000))
            .build();
    final static LuckPerms luckPerms = LuckPermsProvider.get();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getCause() == PlayerGameModeChangeEvent.Cause.DEFAULT_GAMEMODE
            || !event.getPlayer().isOnline()
            || isChangeAllowed(event.getPlayer(), event.getNewGameMode())
        ) {
            return;
        }
        event.setCancelled(true);
        event.cancelMessage(noPermissionComp);
    }

    /**
     * Returns whether the player is allowed to change to the given gamemode.
     * @param player The player to check
     * @param newMode The gamemode the player wants to change to
     * @return `true` if change is allowed, `false` otherwise.
     */
    private static boolean isChangeAllowed(Player player, GameMode newMode) {
        final String gamemode = switch (newMode) {
            case CREATIVE -> "creative";
            case SURVIVAL -> "survival";
            case ADVENTURE -> "adventure";
            case SPECTATOR -> "spectator";
        };
        return luckPerms.getPlayerAdapter(Player.class).getUser(player)
                .getCachedData().getPermissionData()
                .checkPermission("tessertools.gamemode." + gamemode)
                .asBoolean();
    }


}
