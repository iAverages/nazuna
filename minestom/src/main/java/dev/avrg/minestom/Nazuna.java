package dev.avrg.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class Nazuna {
    private static final Logger LOGGER = LoggerFactory.getLogger(Nazuna.class);

    private static JedisPooled redis;

    public static void main(String[] args) {
        Nazuna.redis = new JedisPooled("redis", 6379);

        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit ->
                unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
        AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty()) return;

            final Runtime runtime = Runtime.getRuntime();
            final TickMonitor tickMonitor = lastTick.get();
            final long ramUsage = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

            final Component header = Component.newline()
                    .append(Component.newline()).append(Component.text("Players: " + players.size()))
                    .append(Component.newline()).append(Component.newline())
                    .append(Component.text("RAM USAGE: " + ramUsage + " MB", NamedTextColor.GRAY).append(Component.newline())
                            .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms", NamedTextColor.GRAY))).append(Component.newline());
            Audiences.players().sendPlayerListHeader(header);

        }, TaskSchedule.tick(10), TaskSchedule.tick(10));

        globalEventHandler.addListener(PlayerChatEvent.class, event -> {
            Nazuna.redis.publish("message", String.format("<%s>: %s", event.getPlayer().getUsername(), event.getMessage()));
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);

        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.scheduleNextTick(() -> {
            String payload = Nazuna.getServerPayload();
            Nazuna.redis.publish("server-create", payload);
            LOGGER.info("Send redis payload: " + payload);
        });

        String velocityToken = System.getenv("VELOCITY_FORWARDING_SECRET");
        if (velocityToken != null && !velocityToken.isEmpty()) {
            LOGGER.info("Found Velocity forwarding secret, enabling Velocity support");
            VelocityProxy.enable(velocityToken);
        }

        MinecraftServer.getSchedulerManager().buildShutdownTask(Nazuna::onShutdown);
    }


    private static void onShutdown() {
        String payload = Nazuna.getServerPayload();
        Nazuna.redis.publish("server-delete", payload);
        Nazuna.redis.close();
    }

    private static String getMessageSyncFormat(String user, String message) {
        return String.format("{\"username\":\"%s\",\"message\":\"%s\"", user, message);
    }

    private static String getServerPayload() {
        String podId = System.getenv("POD_IP");
        boolean inKubePod = !(podId == null || podId.equals(""));
        String ip = inKubePod ? podId : "localhost";
        int port = 25565;
        return String.format("{\"host\":\"%s\", \"port\": %s, \"type\":\"lobby\"}", ip, port);
    }

}
