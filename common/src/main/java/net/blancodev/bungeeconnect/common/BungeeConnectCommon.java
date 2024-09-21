package net.blancodev.bungeeconnect.common;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.blancodev.bungeeconnect.common.config.RedisConnectionConfig;
import net.blancodev.bungeeconnect.common.data.PlayerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Common class for implementing modules to hook into
 */
public class BungeeConnectCommon {

    /**
     * The Redis pubsub channel for server data to be published to
     */
    public static final String PUBSUB_CHANNEL = "bungeeConnectServerData";

    /**
     * The Redis key prefix for player data
     */
    public static final String PLAYERDATA_KEY = "bungeeConnectPlayerData.";

    /**
     * The singleton instance of the pubsub handler for server data
     */
    @Getter
    private static final ServerDataPubSub serverDataPubSub = new ServerDataPubSub();

    /**
     * Called by implementing modules to subscribe to the pubsub from their Jedis connection
     * @param jedis an opened Jedis connection
     */
    public static void initPubSub(Jedis jedis) {
        jedis.subscribe(serverDataPubSub, PUBSUB_CHANNEL);
    }

    /**
     * Queries the Redis store for a players data object by their UUID
     * @param jedis the open Jedis connection
     * @param uuid the UUID of the player
     * @param clazz the class of the player data object, to be deserialized from JSON
     * @return the deserialized player data object, or null if not found
     */
    public static <Data extends PlayerData> Data getPlayerData(Jedis jedis, UUID uuid, Class<Data> clazz) {
        String json = jedis.get(PLAYERDATA_KEY + uuid);
        if (json == null) {
            return null;
        }

        return GsonHelper.GSON.fromJson(json, clazz);
    }

    /**
     * Queries the Redis store for a players data object by their username
     * @param jedis the open Jedis connection
     * @param username the username of the player
     * @param clazz the class of the player data object, to be deserialized from JSON
     * @return the deserialized player data object, or null if not found
     */
    public static <Data extends PlayerData> Data getPlayerData(Jedis jedis, String username, Class<Data> clazz) {
        String json = jedis.get(PLAYERDATA_KEY + username.toLowerCase());
        if (json == null) {
            return null;
        }

        return GsonHelper.GSON.fromJson(json, clazz);
    }

    /**
     * Creates a JedisPool from a RedisConnectionConfig
     * @param coreConfig the configuration object
     * @return a configured JedisPool
     */
    public static JedisPool createJedisPool(RedisConnectionConfig coreConfig) {
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(400);
        config.setMaxTotal(400);

        final JedisPool jedisPool;

        if (coreConfig.getRedisPassword().trim().isEmpty()) {
            jedisPool = new JedisPool(config, coreConfig.getRedisHost(), coreConfig.getRedisPort());
        } else {
            jedisPool = new JedisPool(config, coreConfig.getRedisHost(), coreConfig.getRedisPort(), 10_000, coreConfig.getRedisPassword());
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }

        return jedisPool;
    }

    /**
     * Automatically creates, loads and deserializes a configuration object from a file
     * @param configurableModule the module to load the configuration for
     * @param name the name of the configuration file
     * @param clazz the class of the configuration object, to be deserialized from JSON
     * @return the deserialized configuration object
     */
    public static <T> T loadConfig(ConfigurableModule configurableModule, String name, Class<T> clazz) throws IOException, InstantiationException, IllegalAccessException {
        if (!configurableModule.getConfigurationFolder().exists()) {
            configurableModule.getConfigurationFolder().mkdir();
        }

        final T configObject;

        final File configFile = new File(configurableModule.getConfigurationFolder(), name + ".json");
        if (!configFile.exists()) {
            configFile.createNewFile();
            configObject = clazz.newInstance();
        } else {
            configObject = GsonHelper.GSON.fromJson(Files.readString(configFile.toPath()), clazz);
        }

        Files.writeString(configFile.toPath(), GsonHelper.PRETTY_GSON.toJson(configObject));

        return configObject;
    }

}
