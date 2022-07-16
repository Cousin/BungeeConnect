package net.blancodev.bungeeconnect.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.Getter;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerDataPubSub extends JedisPubSub {

    @Getter
    private final Map<String, ServerData> serverDataMap = new ConcurrentHashMap<>();

    @Getter
    private final Set<ServerDataHandler> serverDataHandlers = new HashSet<>(List.of(new ServerDataHandler() {
        @Override
        public void onServerUpdate(ServerData oldData, ServerData newData) {

        }

        @Override
        public void onServerExpire(String serverName, ServerData lastKnownData) {

        }
    }));

    @Getter
    private final Cache<String, ServerData> serverDataCache = Caffeine.newBuilder()
            .scheduler(Scheduler.systemScheduler())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .evictionListener((k, v, cause) -> serverDataHandlers.forEach(h -> h.onServerExpire((String) k, (ServerData) v)))
            .build();

    @Override
    public final void onMessage(String channel, String message) {
        ServerData serverData = GsonHelper.GSON.fromJson(message, ServerData.class);
        serverDataHandlers.forEach(h -> h.onServerUpdate(serverDataCache.getIfPresent(serverData.getServerName()), serverData));
        serverDataCache.put(serverData.getServerName(), serverData);
    }
}
