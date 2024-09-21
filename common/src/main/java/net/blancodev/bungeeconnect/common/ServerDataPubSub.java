package net.blancodev.bungeeconnect.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Pubsub handler for server data updates
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ServerDataPubSub extends JedisPubSub {

    /**
     * Set of registered server data handlers, to each be called when a server update occurs
     */
    private final Set<ServerDataHandler> serverDataHandlers = new HashSet<>();

    /**
     * The class to deserialize server data objects into
     * This can be set by implementing modules to extend the server data object
     */
    @Setter
    private Class<? extends ServerData> serverDataClass = ServerData.class;

    /**
     * The cache of server data objects
     * Server data is updated at a frequency quicker than the cache expires,
     * if eviction occurs, we can assume the server has gone offline
     */
    private final Cache<String, ServerData> serverDataCache = Caffeine.newBuilder()
            .scheduler(Scheduler.systemScheduler())
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, ServerData>) (k, v, cause) -> {
                if (cause.wasEvicted()) {
                    serverDataHandlers.forEach(handler -> handler.onServerExpire(k, v));
                }
            })
            .build();

    /**
     * Handles the message from the pubsub channel
     */
    @Override
    public void onMessage(String channel, String message) {
        ServerData serverData = GsonHelper.GSON.fromJson(message, serverDataClass);
        serverDataHandlers.forEach(h -> h.onServerUpdate(serverDataCache.getIfPresent(serverData.getServerName()), serverData));
        serverDataCache.put(serverData.getServerName(), serverData);
    }
}
