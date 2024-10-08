package net.blancodev.bungeeconnect.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.ServerDataPubSub;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Main class for the Velocity plugin
 * The VelocityConnect plugin will listen for server updates, and automatically register them into the proxy
 */
@Getter
@Plugin(id = "velocityconnect", name = "VelocityConnect", version = "1.0.0-SNAPSHOT",
        url = "https://github.com/Cousin/BungeeConnect", description = "Velocity module for BungeeConnect", authors = { "Executive" })
public class VelocityConnect implements ConfigurableModule {

    private VelocityConnectConfig velocityConnectConfig;
    private JedisPool jedisPool;

    private final ServerDataPubSub serverDataPubSub = BungeeConnectCommon.getServerDataPubSub();

    private final ProxyServer server;
    private final Logger logger;

    private final Path dataDirectory;

    @Inject
    public VelocityConnect(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        try {
            this.velocityConnectConfig = BungeeConnectCommon.loadConfig(this, "config", VelocityConnectConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            server.shutdown();
            return;
        }

        this.jedisPool = BungeeConnectCommon.createJedisPool(velocityConnectConfig);

        logger.info("VelocityConnect enabled");
    }

    /**
     * Subscribe to the ProxyInitializeEvent
     */
    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        // Initialize the PubSub
        getServer().getScheduler().buildTask(this, () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                BungeeConnectCommon.initPubSub(jedis);
            }
        }).schedule();

        // Register the server data handler
        BungeeConnectCommon.getServerDataPubSub().getServerDataHandlers().add(new VelocityServerHandler(this, server));
    }

    @Override
    public File getConfigurationFolder() {
        return dataDirectory.toFile();
    }
}
