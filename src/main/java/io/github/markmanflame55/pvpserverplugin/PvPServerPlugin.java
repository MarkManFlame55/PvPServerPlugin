package io.github.markmanflame55.pvpserverplugin;

import io.github.markmanflame55.pvpserverplugin.commands.wlCommand;
import io.github.markmanflame55.pvpserverplugin.events.InvulnerabilityManager;
import io.github.markmanflame55.pvpserverplugin.events.SculkNerf;
import io.github.markmanflame55.pvpserverplugin.events.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PvPServerPlugin extends JavaPlugin {

    static PvPServerPlugin plugin;
    public Logger LOGGER = Bukkit.getLogger();
    public int sculkNerf = 0;

    PluginManager manager = this.getServer().getPluginManager();


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        plugin = this;

        getCommand("wl").setExecutor(new wlCommand());

        manager.registerEvents(new InvulnerabilityManager(), this);
        manager.registerEvents(new WhitelistManager(), this);
        manager.registerEvents(new SculkNerf(), this);

        InvulnerabilityManager.startChecking();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static PvPServerPlugin getPlugin() {
        return plugin;
    }
}
