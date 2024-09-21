package net.blancodev.bungeeconnect.common.config;

/**
 * Interface representing a configuration for a Redis connection
 */
public interface RedisConnectionConfig {

    String getRedisHost();
    String getRedisPassword();
    int getRedisPort();

}
