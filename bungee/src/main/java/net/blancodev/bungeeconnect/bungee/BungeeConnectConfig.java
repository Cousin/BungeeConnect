package net.blancodev.bungeeconnect.bungee;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.config.RedisConnectionConfig;

@Getter
public class BungeeConnectConfig implements RedisConnectionConfig {

    private String redisHost = "localhost";
    private String redisPassword = "password";
    private int redisPort = 6379;

    private long pollRefreshRate = 1000L;

}
