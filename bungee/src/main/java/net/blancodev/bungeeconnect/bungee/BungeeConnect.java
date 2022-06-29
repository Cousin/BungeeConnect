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

    @Override
    public void onEnable() {
        try {
            this.bungeeConnectConfig = BungeeConnectCommon.loadConfig(this, BungeeConnectConfig.class);
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            getProxy().stop();
            return;
        }

        this.jedisPool = BungeeConnectCommon.createJedisPool(bungeeConnectConfig);

        new BungeeServerPoller(getProxy(), jedisPool, bungeeConnectConfig.getPollRefreshRate()).start();
    }

    @Override
    public File getConfigurationFolder() {
        return getDataFolder();
    }
}
