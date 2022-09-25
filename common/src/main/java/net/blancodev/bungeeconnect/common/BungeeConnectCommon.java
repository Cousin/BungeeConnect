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

public class BungeeConnectCommon {

    public static final String PUBSUB_CHANNEL = "bungeeConnectServerData";
    public static final String PLAYERDATA_KEY = "bungeeConnectPlayerData.";

    @Getter
    private static final ServerDataPubSub serverDataPubSub = new ServerDataPubSub();

    public static void initPubSub(Jedis jedis) {
        jedis.subscribe(serverDataPubSub, PUBSUB_CHANNEL);
    }

    public static <Data extends PlayerData> Data getPlayerData(Jedis jedis, UUID uuid, Class<Data> clazz) {
        String json = jedis.get(PLAYERDATA_KEY + uuid);
        if (json == null) {
            return null;
        }

        return GsonHelper.GSON.fromJson(json, clazz);
    }

    public static <Data extends PlayerData> Data getPlayerData(Jedis jedis, String username, Class<Data> clazz) {
        String json = jedis.get(PLAYERDATA_KEY + username.toLowerCase());
        if (json == null) {
            return null;
        }

        return GsonHelper.GSON.fromJson(json, clazz);
    }

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
