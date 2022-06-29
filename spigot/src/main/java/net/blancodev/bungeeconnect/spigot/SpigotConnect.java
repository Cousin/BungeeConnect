package net.blancodev.bungeeconnect.spigot;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.BungeeConnectCommon;
import net.blancodev.bungeeconnect.common.config.ConfigurableModule;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public final class SpigotConnect extends JavaPlugin implements ConfigurableModule<SpigotConnectConfig> {

    private JedisPool jedisPool;
    private SpigotConnectConfig spigotConnectConfig;

    private String serverName;
    private String detectedHostname;

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

        startPinging();

    }

    private void startPinging() {
        new BukkitRunnable() {
            @Override
            public void run() {
                final ServerData serverData = new ServerData(
                    detectedHostname,
                    getSpigotConnectConfig().getIp(),
                    getServer().getPort(),
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
}
