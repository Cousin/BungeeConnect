package net.blancodev.bungeeconnect.spigot;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.config.RedisConnectionConfig;

/**
 * Configuration for the Spigot Connect plugin
 */
@Getter
public class SpigotConnectConfig implements RedisConnectionConfig {

    private String redisHost = "localhost";
    private String redisPassword = "password";
    private int redisPort = 6379;

    private String serverHostname = "%detect%";
    private String ip = "127.0.0.1";

    private long refreshRateMs = 1000L;

}
