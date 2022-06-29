package net.blancodev.bungeeconnect.common.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ServerData {

    private String hostname;
    private int port;

    private String serverName;

    private int players;
    private int maxPlayers;

    private boolean whitelisted;
    private Set<UUID> whitelistedUuids;

}
