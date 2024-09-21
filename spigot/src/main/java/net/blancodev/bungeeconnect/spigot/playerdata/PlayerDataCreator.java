package net.blancodev.bungeeconnect.spigot.playerdata;

import net.blancodev.bungeeconnect.common.data.ConnectPlayer;
import net.blancodev.bungeeconnect.common.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Interface for creating player data objects
 */
public interface PlayerDataCreator {

    PlayerData createData(ConnectPlayer playerObject);
    ConnectPlayer createPlayer(Player basePlayer);

}
