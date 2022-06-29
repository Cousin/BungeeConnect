# BungeeConnect
A Bungee and Spigot side plugin to easily and dynamically connect new Spigot instances to your Bungee proxy

BungeeCord can be frustrating having to manually update your proxy config.yml everytime you want to add a new server.
With BungeeConnect, all you have to do is start up your Spigot server with the 'SpigotConnect' plugin and your Bungee proxy (with the 'BungeeConnect' plugin) will automatically detect this server and add it.

Not only does BungeeConnect dynamically load your servers, it also polls them in Redis allowing you the developer to query your servers from either the spigot or bungee side.

BungeeCord usage
```java
BungeeConnect bungeeConnect = (BungeeConnect) getProxy().getPluginManager().getPlugin("BungeeConnect");
if (bungeeConnect == null) {
    getProxy().stop("Missing BungeeConnect, shutting down");
    return;
}

Map<String, ServerData> serverData = bungeeConnect.getBungeeServerPoller().getServerDataMap();
System.out.println("Lobby Server Online: " + serverData.get("lobby").getPlayers())
```

Spigot usage
```java
SpigotConnect spigotConnect = (SpigotConnect) getServer().getPluginManager().getPlugin("SpigotConnect");
if (spigotConnect == null) {
    System.err.println("Missing SpigotConnect, shutting down");
    getServer().shutdown();
    return;
}

Map<String, ServerData> serverData = spigotConnect.getServerPoller().getServerDataMap();
System.out.println("Lobby Server Online: " + serverData.get("lobby").getPlayers())
```