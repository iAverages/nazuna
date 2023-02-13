package dev.avrg.nazuna;

import com.google.gson.Gson;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ServerRegister extends JedisPubSub {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegister.class);
    private final ProxyServer proxy;
    private final Gson gson;

    public ServerRegister(ProxyServer proxy) {
        this.proxy = proxy;
        this.gson = new Gson();
    }

    public void onMessage(String channel, String message) {
        NazunaServer server = this.gson.fromJson(message, NazunaServer.class);
        switch (channel) {
            case "server-create":
                try {
                    server.registerServer(this.proxy);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "server-delete":
                server.deregisterServer(this.proxy);
                break;
            default:
                LOGGER.warn("Received invalid channel from Redis: " + channel + ", " + message);
        }

    }
}
