package net.blancodev.bungeeconnect.spigot.serverdata;

import net.blancodev.bungeeconnect.common.data.ServerData;
import net.blancodev.bungeeconnect.spigot.SpigotConnect;
import org.bukkit.Server;

/**
 * Interface for creating server data objects
 */
@FunctionalInterface
public interface ServerDataCreator {

    ServerData createServerData(SpigotConnect spigotConnect, Server server);

}
