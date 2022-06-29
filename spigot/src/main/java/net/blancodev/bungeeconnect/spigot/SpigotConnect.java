package net.blancodev.bungeeconnect.spigot;

import io.netty.channel.ChannelFuture;
import lombok.Getter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.ServerPoller;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public final class SpigotConnect extends JavaPlugin implements ConfigurableModule<SpigotConnectConfig> {

    private JedisPool jedisPool;
    private SpigotConnectConfig spigotConnectConfig;

    private ServerPoller serverPoller;

    private String serverName;
    private String detectedHostname;

    private Protocol protocol;

    private int port;

    @Override
    public void onEnable() {
        try {
            this.spigotConnectConfig = BungeeConnectCommon.loadConfig(this, SpigotConnectConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        this.jedisPool = BungeeConnectCommon.createJedisPool(spigotConnectConfig);

        try {
            this.detectedHostname = spigotConnectConfig.getServerHostname()
                    .replace("%detect%", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        this.serverName = spigotConnectConfig.getServerName()
                .replace("%uuid%", UUID.randomUUID().toString().split("-")[0])
                .replace("%hostname%", detectedHostname);

        this.protocol = detectProtocol();

        try {
            this.port = injectAndFindPort(getServer());
        } catch (Exception e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        startPinging();

        this.serverPoller = new ServerPoller(jedisPool, getSpigotConnectConfig().getRefreshRateMs()) {
            @Override
            public void onServerExpire(String s, ServerData serverData) {

            }

            @Override
            public void onServerUpdate(ServerData serverData, ServerData serverData1) {

            }
        };
    }

    private void startPinging() {
        new BukkitRunnable() {
            @Override
            public void run() {
                final ServerData serverData = new ServerData(
                    detectedHostname,
                    getSpigotConnectConfig().getIp(),
                    port,
                    getServerName(),
                    getSpigotConnectConfig().getMotd(),
                    getSpigotConnectConfig().isRestricted(),
                    getServer().getOnlinePlayers().size(),
                    getServer().getMaxPlayers(),
                    getServer().hasWhitelist(),
                    getServer().hasWhitelist() ? getServer().getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId).collect(Collectors.toSet()) : Collections.emptySet()
                );

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.set(BungeeConnectCommon.SERVER_DATA_KEY + getServerName(), GsonHelper.GSON.toJson(serverData));
                    jedis.expire(BungeeConnectCommon.SERVER_DATA_KEY + getServerName(), 5);
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    @Override
    public File getConfigurationFolder() {
        return getDataFolder();
    }

    private Protocol detectProtocol() {
        String version = super.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        for (Protocol protocol : Protocol.values()) {
            if (protocol.name().equals(version.toUpperCase())) {
                getLogger().info("Detected protocol version as " + protocol.name());
                return protocol;
            }
        }

        return null;
    }

    /*
        Method for finding the true port of the server, useful for when utilizing port 0
        Derived from Lilypads Bukkit-Connect
     */
    private int injectAndFindPort(Server server) throws Exception {
        Method serverGetHandle = server.getClass().getDeclaredMethod("getServer");
        Object minecraftServer = serverGetHandle.invoke(server);
        // Get Server Connection
        Method serverConnectionMethod = null;
        for(Method method : minecraftServer.getClass().getSuperclass().getDeclaredMethods()) {
            if(!method.getReturnType().getSimpleName().equals("ServerConnection")) {
                continue;
            }
            serverConnectionMethod = method;
            break;
        }
        Object serverConnection = serverConnectionMethod.invoke(minecraftServer);
        // Get ChannelFuture List
        List<ChannelFuture> channelFutureList = getPrivateField(serverConnection.getClass(), serverConnection, protocol.getChannelFuturesField());
        // Iterate ChannelFutures
        int commonPort = 0;
        for(ChannelFuture channelFuture : channelFutureList) {
            commonPort = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort();
        }
        return commonPort;
    }

    private  <T> T getPrivateField(Class<?> objectClass, Object object, String fieldName) throws Exception {
        Field field = objectClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

}
