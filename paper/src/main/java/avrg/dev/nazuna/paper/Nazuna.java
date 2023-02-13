package avrg.dev.nazuna.paper;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPooled;

public final class Nazuna extends JavaPlugin {

    private JedisPooled redis;

    @Override
    public void onEnable() {
        this.redis = new JedisPooled("redis", 6379);
        this.getLogger().info("POD_IP: " + System.getenv("POD_IP"));
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            String payload = this.getServerPayload();
            this.redis.publish("server-create", payload);
            this.getLogger().info("Send redis payload: " + payload);
        }, 1);
    }

    @Override
    public void onDisable() {
        this.redis.publish("server-delete", this.getServerPayload());
        this.redis.close();
    }

    private String getServerPayload() {
        String podId = System.getenv("POD_IP");
        boolean inKubePod = !(podId == null || podId.equals(""));
        String ip = inKubePod ? podId : Bukkit.getIp().equals("") ? "localhost" : Bukkit.getIp();
        int port = Bukkit.getPort();
        return String.format("{\"host\":\"%s\", \"port\": %s}", ip, port);
    }
}
