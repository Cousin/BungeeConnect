package net.blancodev.bungeeconnect.spigot.serverdata;

import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.spigot.SpigotConnect;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.util.Collections;
import java.util.stream.Collectors;

public class SpigotServerDataCreator implements ServerDataCreator {

    @Override
    public ServerData createServerData(SpigotConnect spigotConnect, Server server) {
        return new ServerData(
                spigotConnect.getDetectedHostname(),
                spigotConnect.getSpigotConnectConfig().getIp(),
                spigotConnect.getPort(),
                spigotConnect.getServerName(),
                spigotConnect.getSpigotConnectServerConfig().getMotd(),
                spigotConnect.getSpigotConnectServerConfig().isRestricted(),
                server.getOnlinePlayers().size(),
                server.getMaxPlayers(),
                server.hasWhitelist(),
                server.hasWhitelist() ? server.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).collect(Collectors.toSet()) : Collections.emptySet()
        );
    }

}
