package net.blancodev.bungeeconnect.spigot;

import lombok.Getter;
import net.blancodev.bungeeconnect.common.config.RedisConnectionConfig;

@Getter
public class SpigotConnectConfig implements RedisConnectionConfig {

    private String redisHost = "localhost";
    private String redisPassword = "password";
    private int redisPort = 6719;

    private String serverName = "mc%uuid%";
    private String serverHostname = "%detect%";

}
