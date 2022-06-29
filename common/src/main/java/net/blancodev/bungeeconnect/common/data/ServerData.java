package net.blancodev.bungeeconnect.common.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ServerData {

    private String hostname;
    private String ip;
    private int port;

    private String serverName;

    private String motd;

    private boolean restricted;

    private int players;
    private int maxPlayers;

    private boolean whitelisted;
    private Set<UUID> whitelistedUuids;

}
