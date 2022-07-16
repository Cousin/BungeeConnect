package net.blancodev.bungeeconnect.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.Getter;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerDataPubSub extends JedisPubSub {

    @Getter
    private final Set<ServerDataHandler> serverDataHandlers = new HashSet<>();

    @Getter
    private final Cache<String, ServerData> serverDataCache = Caffeine.newBuilder()
            .scheduler(Scheduler.systemScheduler())
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, ServerData>) (k, v, cause) -> {
                if (cause.wasEvicted()) {
                    serverDataHandlers.forEach(handler -> handler.onServerExpire(k, v));
                }
            })
            .build();

    @Override
    public final void onMessage(String channel, String message) {
        ServerData serverData = GsonHelper.GSON.fromJson(message, ServerData.class);
        serverDataHandlers.forEach(h -> h.onServerUpdate(serverDataCache.getIfPresent(serverData.getServerName()), serverData));
        serverDataCache.put(serverData.getServerName(), serverData);
    }
}
