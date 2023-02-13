package dev.avrg.nazuna;

import com.google.gson.Gson;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

public class MessageSync extends JedisPubSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegister.class);
    private final ProxyServer proxy;
    private final Gson gson;
    public MessageSync(ProxyServer proxy) {
        this.proxy = proxy;
        this.gson = new Gson();
    }

    public void onMessage(String channel, String message) {
        if (!channel.equals("message")) {
            LOGGER.warn("Received invalid channel from Redis: " + channel + ", " + message);
            return;
        }

        this.proxy.getAllPlayers().forEach(p -> p.sendMessage(Component.text(message)));
    }
}
