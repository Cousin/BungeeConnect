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
import net.blancodev.bungeeconnect.spigot.serverdata.ServerDataCreator;
import net.blancodev.bungeeconnect.spigot.serverdata.SpigotServerDataCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Main class for the Spigot plugin
 *
 * The SpigotConnect plugin is responsible for sending server and player data to the Redis store
 * Other modules can then query Redis for this data
 */
@Getter
public final class SpigotConnect extends JavaPlugin implements ConfigurableModule {

    private JedisPool jedisPool;
    private SpigotConnectConfig spigotConnectConfig;
    private SpigotConnectServerConfig spigotConnectServerConfig;

    private String serverName;
    private String detectedHostname;

    private int port;

    /**
     * The creator for player data objects, which can be overridden by implementing modules
     */
    @Setter
    private PlayerDataCreator playerDataCreator = new SpigotPlayerDataCreator();

    /**
     * The creator for server data objects, which can be overridden by implementing modules
     */
    @Setter
    private ServerDataCreator serverDataCreator = new SpigotServerDataCreator();

    private final ServerDataPubSub serverDataPubSub = BungeeConnectCommon.getServerDataPubSub();

    @Override
    public void onEnable() {
        // Load the configuration files
        try {
            this.spigotConnectConfig = BungeeConnectCommon.loadConfig(this, "config", SpigotConnectConfig.class);
            this.spigotConnectServerConfig = BungeeConnectCommon.loadConfig(this, "server", SpigotConnectServerConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        // Initialize the Jedis pool
        this.jedisPool = BungeeConnectCommon.createJedisPool(spigotConnectConfig);

        // Detect the hostname
        try {
            this.detectedHostname = spigotConnectConfig.getServerHostname()
                    .replace("%detect%", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        // Format and cache the server name
        this.serverName = spigotConnectServerConfig.getServerName()
                .replace("%uuid%", UUID.randomUUID().toString().split("-")[0])
                .replace("%hostname%", detectedHostname);

        // Attempt to find running port
        try {
            this.port = getCommonPort();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        // Initialize the pubsub handler
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Jedis jedis = jedisPool.getResource()) {
                    BungeeConnectCommon.initPubSub(jedis);
                }
            }
        }.runTaskAsynchronously(this);

        // Start updating server and player data
        startPinging();
    }

    /**
     * Creates and runs task which publishes server and player data
     */
    private void startPinging() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Create server data object for this running server instance
                ServerData serverData = serverDataCreator.createServerData(SpigotConnect.this, getServer());

                try (Jedis jedis = jedisPool.getResource()) {
                    // Send server data to the pubsub channel
                    jedis.publish(BungeeConnectCommon.PUBSUB_CHANNEL, GsonHelper.GSON.toJson(serverData));

                    // Update player data for all online players
                    for (Player online : getServer().getOnlinePlayers()) {
                        final String uuidKey = BungeeConnectCommon.PLAYERDATA_KEY + online.getUniqueId();
                        final String nameKey = BungeeConnectCommon.PLAYERDATA_KEY + online.getName().toLowerCase();

                        final PlayerData playerData = playerDataCreator.createData(playerDataCreator.createPlayer(online));
                        final String json = GsonHelper.GSON.toJson(playerData);

                        // Set player data, and expire after 5 seconds
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

    /**
     * Attempts to find the running port for this Spigot server instance
     *
     * We can not just query the config for the port,
     * in situations of configured port 0 (random open port) or containerized environments,
     * the port may not be known until runtime
     *
     * Modified version from https://github.com/WesJD/Bukkit-Connect/blob/master/src/main/java/lilypad/bukkit/connect/ConnectPlugin.java
     */
    private int getCommonPort() {
        try {
            final Object minecraftServer =
                    super.getServer().getClass().getMethod("getServer").invoke(super.getServer());

            final Object serverConnection = Arrays.stream(minecraftServer.getClass().getMethods())
                    .filter(method -> method.getReturnType().getSimpleName().equals("ServerConnectionListener"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Unable to find ServerConnection"))
                    .invoke(minecraftServer);

            return Arrays.stream(serverConnection.getClass().getDeclaredFields())
                    .filter(field -> field.getType().equals(List.class))
                    .peek(field -> field.setAccessible(true))
                    .map(field -> {
                        try {
                            return (List) field.get(serverConnection);
                        } catch (IllegalAccessException exception) {
                            System.err.println("Failed to get list from ServerConnection field \"" + field.getName() + "\", " +
                                    "defaulting to empty list");
                            exception.printStackTrace();
                            return Collections.emptyList();
                        }
                    })
                    .filter(list -> !list.isEmpty() && ChannelFuture.class.isAssignableFrom(list.get(0).getClass()))
                    .map(list -> (ChannelFuture) list.get(0))
                    .map(channelFuture -> ((InetSocketAddress) channelFuture.channel().localAddress()).getPort())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Failed to find common port"));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new IllegalStateException("Failed to resolve channelFuture list", exception);
        }
    }

    private <T> T getPrivateField(Class<?> objectClass, Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = objectClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

}
