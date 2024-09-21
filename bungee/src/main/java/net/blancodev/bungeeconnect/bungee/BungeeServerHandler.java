package net.blancodev.bungeeconnect.bungee;

import lombok.RequiredArgsConstructor;
import net.blancodev.bungeeconnect.common.ServerDataHandler;
import net.blancodev.bungeeconnect.common.data.ServerData;

import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * ServerDataHandler implementation for BungeeCord
 */
@RequiredArgsConstructor
public class BungeeServerHandler implements ServerDataHandler {

    private final BungeeConnect plugin;

    @Override
    public void onServerExpire(String serverName, ServerData lastKnownData) {
        // Remove the server from the proxy
        this.plugin.getProxy().getServers().remove(serverName);
        this.plugin.getProxy().getLogger().log(Level.INFO, "Server " + serverName + " has expired");
    }

    @Override
    public void onServerUpdate(ServerData oldData, ServerData newData) {
        // If the information has changed
        if (!newData.equals(oldData)) {
            // Override the registered server in the proxy
            this.plugin.getProxy().getServers().put(
                    newData.getServerName(),
                    this.plugin.getProxy().constructServerInfo(
                            newData.getServerName(),
                            InetSocketAddress.createUnresolved(newData.getIp(), newData.getPort()),
                            newData.getMotd(),
                            newData.isRestricted()
                    )
            );

            this.plugin.getProxy().getLogger().log(Level.INFO, "Server " + newData.getServerName() + " has been updated");
        }
    }
}
