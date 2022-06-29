package net.blancodev.bungeeconnect.bungee;

import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;

public class BungeeConnect extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getServers().put("tester", getProxy().constructServerInfo(
            "tester", InetSocketAddress.createUnresolved("127.0.0.1", 25568), "motd", false
        ));
    }

}
