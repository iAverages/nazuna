package dev.avrg.nazuna;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class GlobalTabList {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTabList.class);
    private final ProxyServer proxy;
    private final Nazuna plugin;


    public GlobalTabList(Nazuna plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxy = proxyServer;
        this.proxy.getScheduler()
                .buildTask(plugin, () -> {
                    LOGGER.debug("Updating tab list ping for {} players", this.proxy.getPlayerCount());
                    this.proxy.getAllPlayers().forEach(player1 -> {
                        this.proxy.getAllPlayers().forEach(player2 -> {
                            if (player1.getUniqueId().equals(player2.getUniqueId())) return;
                            TabList tabList = player1.getTabList();
                            if (!tabList.containsEntry(player2.getUniqueId())) {
                                TabListEntry entry = TabListEntry.builder()
                                        .profile(player2.getGameProfile())
                                        .tabList(tabList)
                                        .build();
                                entry.setLatency((int) player2.getPing());
                                tabList.addEntry(entry);
                            }
                        });
                    });
                })
                .repeat(Duration.ofSeconds(30L))
                .schedule();
    }

    @Subscribe
    public void connect(ServerConnectedEvent event) {
        this.updatePlayerTabList(event.getPlayer());
    }

    @Subscribe
    public void disconnect(DisconnectEvent event) {
        this.proxy.getAllPlayers().forEach(player -> {
            player.getTabList().removeEntry(event.getPlayer().getUniqueId());
        });
    }

    private void updatePlayerTabList(Player joiningPlayer) {

        this.proxy.getScheduler()
                .buildTask(this.plugin, () -> {
                    TabList joiningPlayerTabList = joiningPlayer.getTabList();

                    this.proxy.getAllPlayers().forEach(player -> {
                        if (player.getUniqueId().equals(joiningPlayer.getUniqueId())) return;

                        // Add joiningPlayer to tab list of everyone online player
                        TabList playerTabList = player.getTabList();
                        if (!playerTabList.containsEntry(joiningPlayer.getUniqueId())) {
                            TabListEntry playerEntry = TabListEntry.builder()
                                    .profile(joiningPlayer.getGameProfile())
                                    .tabList(playerTabList)
                                    .build();
                            playerTabList.addEntry(playerEntry);
                        }

                        // Add every online player to tab list of joining player
                        if (!joiningPlayerTabList.containsEntry(player.getUniqueId())) {
                            joiningPlayerTabList.addEntry(
                                    TabListEntry.builder()
                                            .profile(player.getGameProfile())
                                            .tabList(joiningPlayer.getTabList())
                                            .build()
                            );
                        }
                    });
                })
                // 1 second delay so players actually get added correctly when changing servers
                .delay(Duration.ofSeconds(1L))
                .schedule();
    }
}
