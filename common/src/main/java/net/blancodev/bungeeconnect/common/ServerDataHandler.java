package net.blancodev.bungeeconnect.common;

import net.blancodev.bungeeconnect.common.data.ServerData;

/**
 * Interface for handling server data updates
 */
public interface ServerDataHandler {

    /**
     * Called when a server's data is updated
     * @param oldData the old data, or null if server was just added
     * @param newData the new data
     */
    void onServerUpdate(ServerData oldData, ServerData newData);

    /**
     * Called when a server's data is removed due to expiration, or other explicit removal
     * @param serverName the name of the server
     * @param lastKnownData the last known data for the server
     */
    void onServerExpire(String serverName, ServerData lastKnownData);

}
