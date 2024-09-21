package net.blancodev.bungeeconnect.spigot.playerdata;

import net.blancodev.bungeeconnect.common.data.ConnectPlayer;
import net.blancodev.bungeeconnect.common.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Default implementation of the PlayerDataCreator interface for Spigot
 */
public class SpigotPlayerDataCreator implements PlayerDataCreator {

    @Override
    public PlayerData createData(ConnectPlayer spigotConnectPlayer) {
        return new PlayerData(
                spigotConnectPlayer.getServer(),
                spigotConnectPlayer.getUuid(),
                spigotConnectPlayer.getUsername(),
                spigotConnectPlayer.getDisplayName(),
                spigotConnectPlayer.getIp()
        );
    }

    @Override
    public SpigotConnectPlayer createPlayer(Player player) {
        return new SpigotConnectPlayer(player);
    }
}
