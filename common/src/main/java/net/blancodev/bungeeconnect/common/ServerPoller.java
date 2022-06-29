package net.blancodev.bungeeconnect.common;

import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.common.util.GsonHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public abstract class ServerPoller extends Thread {

    private final JedisPool jedisPool;
    private final long refreshRateMs;

    private final Map<String, ServerData> serverDataMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        while (true) {
            try (Jedis jedis = jedisPool.getResource()) {

                Set<String> keys = jedis.keys(BungeeConnectCommon.SERVER_DATA_KEY + "*");
                for (String key : keys) {
                    String serverName = key.replace(BungeeConnectCommon.SERVER_DATA_KEY, "");

                    final ServerData serverData;
                    try {
                        serverData = GsonHelper.GSON.fromJson(jedis.get(key), ServerData.class);
                    } catch (JsonSyntaxException exception) {
                        continue;
                    }

                    final ServerData oldData = serverDataMap.put(serverName, serverData);
                    onServerUpdate(oldData, serverData);
                }

                serverDataMap.keySet().removeIf(key -> {
                   if (!keys.contains(BungeeConnectCommon.SERVER_DATA_KEY + key)) {
                       onServerExpire(key, serverDataMap.get(key));
                       return true;
                   }

                   return false;
                });

            }

            try {
                Thread.sleep(refreshRateMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onServerExpire(String serverName, ServerData lastKnownData);
    public abstract void onServerUpdate(ServerData oldData, ServerData newData);

}
