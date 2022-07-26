package net.blancodev.bungeeconnect.common.data;

import java.util.UUID;

public interface ConnectPlayer {

    UUID getUuid();
    String getUsername();
    String getDisplayName();
    String getServer();
    String getIp();

}
