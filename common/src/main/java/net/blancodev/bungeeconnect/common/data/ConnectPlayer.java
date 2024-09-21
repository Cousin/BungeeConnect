package net.blancodev.bungeeconnect.common.data;

import java.util.UUID;

/**
 * Interface representing a connected player
 */
public interface ConnectPlayer {

    UUID getUuid();
    String getUsername();
    String getDisplayName();
    String getServer();
    String getIp();

}
