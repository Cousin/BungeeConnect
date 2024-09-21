package net.blancodev.bungeeconnect.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.blancodev.bungeeconnect.common.ServerDataHandler;
import net.blancodev.bungeeconnect.common.data.ServerData;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.logging.Level;

/**
 * ServerDataHandler implementation for Velocity
 */
public class VelocityServerHandler implements ServerDataHandler {

    private final VelocityConnect plugin;
    private final ProxyServer proxyServer;

    public VelocityServerHandler(VelocityConnect plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
    }

    @Override
    public void onServerExpire(String serverName, ServerData lastKnownData) {
        expireServer(lastKnownData.getServerName());
    }

    @Override
    public void onServerUpdate(ServerData oldData, ServerData newData) {
        // If the information has changed
        if (!newData.equals(oldData)) {
            // If the server names are the same, yet core info has changed, remove the old server
            if (oldData != null) {
                expireServer(oldData.getServerName());
            }

            // Register the new server
            this.proxyServer.registerServer(new ServerInfo(newData.getServerName(), InetSocketAddress.createUnresolved(newData.getIp(), newData.getPort())));
            plugin.getLogger().log(Level.INFO, "Server " + newData.getServerName() + " has been updated");
        }
    }

    /**
     * Removes server from proxy server list
     */
    private void expireServer(String name) {
        Optional<RegisteredServer> serverInfo = this.proxyServer.getServer(name);
        if (serverInfo.isPresent()) {
            this.proxyServer.unregisterServer(serverInfo.get().getServerInfo());
            plugin.getLogger().log(Level.INFO, "Server " + name + " has expired");
        }
    }

}
