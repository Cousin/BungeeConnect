package net.blancodev.bungeeconnect.bungee;

import net.blancodev.bungeeconnect.common.ServerPoller;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.md_5.bungee.api.ProxyServer;
import redis.clients.jedis.JedisPool;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class BungeeServerPoller extends ServerPoller {

    private final ProxyServer proxyServer;

    public BungeeServerPoller(ProxyServer proxyServer, JedisPool jedisPool, long refreshRateMs) {
        super(jedisPool, refreshRateMs);
        this.proxyServer = proxyServer;
    }

    @Override
    public void onServerExpire(String serverName, ServerData lastKnownData) {
        this.proxyServer.getServers().remove(serverName);
        proxyServer.getLogger().log(Level.INFO, "Server " + serverName + " has expired");
    }

    @Override
    public void onServerUpdate(ServerData oldData, ServerData newData) {
        if (oldData == null || (!oldData.getIp().equals(newData.getIp()) || oldData.getPort() != newData.getPort())) {
            this.proxyServer.getServers().put(
                    newData.getServerName(),
                    this.proxyServer.constructServerInfo(
                            newData.getServerName(),
                            InetSocketAddress.createUnresolved(newData.getIp(), newData.getPort()),
                            newData.getMotd(),
                            newData.isRestricted()
                    )
            );
            proxyServer.getLogger().log(Level.INFO, "Server " + newData.getServerName() + " has been updated");
        }
    }
}
