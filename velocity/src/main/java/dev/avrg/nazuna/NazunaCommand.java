package dev.avrg.nazuna;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;

public abstract class NazunaCommand implements SimpleCommand {

    protected Nazuna plugin;
    protected ProxyServer proxy;

    public NazunaCommand(Nazuna plugin, ProxyServer proxy) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.register();
    }

    public void register() {
        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("dan")
                .plugin(this)
                .build();
        commandManager.register(commandMeta, this);
    }

}
