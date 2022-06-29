package net.blancodev.bungeeconnect.bungee;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;

@Getter
public class BungeeConnect extends Plugin implements ConfigurableModule<BungeeConnectConfig> {

    private BungeeConnectConfig bungeeConnectConfig;
    private JedisPool jedisPool;
    private BungeeServerPoller bungeeServerPoller;

    @Override
    public void onEnable() {
        try {
            this.bungeeConnectConfig = BungeeConnectCommon.loadConfig(this, BungeeConnectConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            getProxy().stop();
            return;
        }

        this.jedisPool = BungeeConnectCommon.createJedisPool(bungeeConnectConfig);

        this.bungeeServerPoller = new BungeeServerPoller(getProxy(), jedisPool, bungeeConnectConfig.getPollRefreshRate());
        this.bungeeServerPoller.start();
    }

    @Override
    public File getConfigurationFolder() {
        return getDataFolder();
    }
}
