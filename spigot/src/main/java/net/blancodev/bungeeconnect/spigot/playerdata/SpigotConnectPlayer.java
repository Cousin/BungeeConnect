package net.blancodev.bungeeconnect.spigot.playerdata;

import lombok.RequiredArgsConstructor;
import net.blancodev.bungeeconnect.common.data.ConnectPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class SpigotConnectPlayer implements ConnectPlayer {

    private final Player player;

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public String getUsername() {
        return player.getName();
    }

    @Override
    public String getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    public String getServer() {
        return player.getServer().getName();
    }

    @Override
    public String getIp() {
        return player.getAddress().getAddress().getHostAddress();
    }

}
