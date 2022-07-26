package net.blancodev.bungeeconnect.spigot;

import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.ServerDataPubSub;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.blancodev.bungeeconnect.common.data.PlayerData;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import net.blancodev.bungeeconnect.spigot.playerdata.PlayerDataCreator;
import net.blancodev.bungeeconnect.spigot.playerdata.SpigotPlayerDataCreator;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
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

    private String serverName;
    private String detectedHostname;

    private Protocol protocol;

    private int port;

    @Setter
    private PlayerDataCreator playerDataCreator = new SpigotPlayerDataCreator();

    private final ServerDataPubSub serverDataPubSub = BungeeConnectCommon.getServerDataPubSub();

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

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Jedis jedis = jedisPool.getResource()) {
                    BungeeConnectCommon.initPubSub(jedis);
                }
            }
        }.runTaskAsynchronously(this);

        startPinging();
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
                    jedis.publish(BungeeConnectCommon.PUBSUB_CHANNEL, GsonHelper.GSON.toJson(serverData));

                    for (Player online : getServer().getOnlinePlayers()) {
                        final String uuidKey = BungeeConnectCommon.PLAYERDATA_KEY + online.getUniqueId();
                        final String nameKey = BungeeConnectCommon.PLAYERDATA_KEY + online.getName().toLowerCase();

                        final PlayerData playerData = playerDataCreator.createData(playerDataCreator.createPlayer(online));
                        final String json = GsonHelper.GSON.toJson(playerData);

                        jedis.set(uuidKey, json);
                        jedis.set(nameKey, json);
                        jedis.expire(uuidKey, 5);
                        jedis.expire(nameKey, 5);
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
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

    private <T> T getPrivateField(Class<?> objectClass, Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = objectClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

}
