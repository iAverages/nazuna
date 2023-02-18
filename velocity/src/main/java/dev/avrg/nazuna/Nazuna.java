package dev.avrg.nazuna;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPooled;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(
        id = "nazuna",
        name = "Nazuna",
        version = BuildConstants.VERSION
)
public class Nazuna {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", System.getenv().getOrDefault("APP_ENV", "INFO"));
    }

    @Inject
    private Logger logger;
    private final ProxyServer proxy;
    private JedisPooled redis;

    @Inject
    public Nazuna(ProxyServer server) {
        this.proxy = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.redis = new JedisPooled("redis", 6379);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> this.redis.subscribe(new ServerRegister(this.proxy), "server-create", "server-delete"));
        executor.execute(() -> this.redis.subscribe(new MessageSync(this.proxy), "message"));
        this.proxy.getEventManager().register(this, new GlobalTabList(this, this.proxy));
        new DanCommand(this, this.proxy);
    }

    @Subscribe
    public void onPlayerChooseInitialServerEvent(PlayerChooseInitialServerEvent e) {
        Collection<RegisteredServer> servers = this.proxy.getAllServers();
        Optional<RegisteredServer> server = servers
                .stream()
                .skip((int) (servers.size() * Math.random()))
                .findFirst();
        if (server.isEmpty()) {
            e.getPlayer().disconnect(Component.text("Failed to find lobby.").color(NamedTextColor.RED));
            return;
        }
        e.setInitialServer(server.get());
    }
}
