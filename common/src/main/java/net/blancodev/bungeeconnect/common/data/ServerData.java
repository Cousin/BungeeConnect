package net.blancodev.bungeeconnect.common.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerData {

    private String hostname;
    private int port;

    private String serverName;

    private int players;
    private int maxPlayers;

}
