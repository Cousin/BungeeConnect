package net.blancodev.bungeeconnect.common;

import net.blancodev.bungeeconnect.common.data.ServerData;

import java.util.Map;

public interface ServerDataHandler {

    void onServerUpdate(ServerData oldData, ServerData newData);
    void onServerExpire(String serverName, ServerData lastKnownData);
    Map<String, ServerData> getServerData();

}
