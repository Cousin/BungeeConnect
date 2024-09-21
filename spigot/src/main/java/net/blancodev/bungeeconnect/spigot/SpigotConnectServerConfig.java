package net.blancodev.bungeeconnect.spigot;

import lombok.Getter;

/**
 * Configuration for this server instance
 * %uuid% generates a random UUID
 */
@Getter
public class SpigotConnectServerConfig {

    private String serverName = "mc%uuid%";
    private String motd = "My BungeeConnect Server";
    private boolean restricted = false;

}
