package dev.avrg.nazuna;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

public class NazunaServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegister.class);
    public String host;
    public int port;
    public String type;

    public void registerServer(ProxyServer proxy) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(this.host.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        StringBuilder hashText = new StringBuilder(no.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        String hash = hashText.toString();
        String name = this.type + "-" + hash.substring(0, 10);
        proxy.registerServer(new ServerInfo(name, new InetSocketAddress(this.host, this.port)));
        LOGGER.info("Registering new server:" + name);
    }

    public  void deregisterServer (ProxyServer proxy) {
        Optional<RegisteredServer> server = proxy.getAllServers().stream().filter((s) -> {
            return s.getServerInfo().getAddress().getHostName().equals(this.host)
                    && s.getServerInfo().getAddress().getPort() == this.port;
        }).findFirst();
        if (server.isEmpty()) return;
        proxy.unregisterServer(server.get().getServerInfo());
        LOGGER.info("Unregistering server:" + server.get().getServerInfo().getName());
    }
}
