package dev.avrg.nazuna;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DanCommand extends NazunaCommand {
    public DanCommand(Nazuna plugin, ProxyServer proxy) {
        super(plugin, proxy);
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        switch (args[0]) {
            case "tab":
                String checkName = args[1];
                Optional<Player> optionalPlayer = this.proxy.getPlayer(checkName);
                if (optionalPlayer.isEmpty()) {
                    invocation.source().sendMessage(Component.text("Cannot find player " + checkName));
                    return;
                }
                StringBuilder namesBuilder = new StringBuilder();
                Player player = optionalPlayer.get();
                player.getTabList().getEntries().forEach(e -> {
                    namesBuilder.append(e.getProfile().getName());
                });
                invocation.source().sendMessage(Component.text(namesBuilder.toString()));
                break;
            case "stop":
                this.proxy.shutdown(Component.text("Dan said stop"));
        }
    }

    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        List<String> list = new ArrayList<>();
        if (invocation.arguments().length < 2) {
            list = new ArrayList<>(List.of("tab", "stop"));
        } else if (invocation.arguments().length < 3) {
            if (Objects.equals(invocation.arguments()[0], "tab")) {
                list = this.proxy.getAllPlayers()
                        .stream()
                        .map(Player::getUsername)
                        .collect(Collectors.toList());
            }
        }

        return CompletableFuture.completedFuture(list);
    }
}
