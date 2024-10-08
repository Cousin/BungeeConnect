package net.blancodev.bungeeconnect.bungee;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.ServerDataPubSub;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;

/**
 * Main class for the BungeeConnect plugin
 * The BungeeConnect plugin will listen for server updates, and automatically register them into the proxy
 */
@Getter
public class BungeeConnect extends Plugin implements ConfigurableModule {

    private BungeeConnectConfig bungeeConnectConfig;
    private JedisPool jedisPool;

    private final ServerDataPubSub serverDataPubSub = BungeeConnectCommon.getServerDataPubSub();

    @Override
    public void onEnable() {
        try {
            this.bungeeConnectConfig = BungeeConnectCommon.loadConfig(this, "config", BungeeConnectConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            getProxy().stop();
            return;
        }

        this.jedisPool = BungeeConnectCommon.createJedisPool(bungeeConnectConfig);

        // Initialize the pubsub
        getProxy().getScheduler().runAsync(this, () -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                BungeeConnectCommon.initPubSub(jedis);
            }
        });

        // Register the server handler
        BungeeConnectCommon.getServerDataPubSub().getServerDataHandlers().add(new BungeeServerHandler(this));
    }

    @Override
    public File getConfigurationFolder() {
        return getDataFolder();
    }
}
