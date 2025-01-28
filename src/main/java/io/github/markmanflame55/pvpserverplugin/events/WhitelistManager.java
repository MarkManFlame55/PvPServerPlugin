package io.github.markmanflame55.pvpserverplugin.events;

import io.github.markmanflame55.pvpserverplugin.PvPServerPlugin;
import io.github.markmanflame55.pvpserverplugin.text.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WhitelistManager implements Listener {

    PvPServerPlugin plugin = PvPServerPlugin.getPlugin();
    FileConfiguration config = this.plugin.getConfig();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Primero asegurarme de que la Whitelist este en la config :P
        if (this.config.contains("players.whitelist") && this.config.contains("players.staff_whitelist")) {

            List<String> staffWhitelist = this.config.getStringList("players.staff_whitelist");
            List<String> whitelist = this.config.getStringList("players.whitelist");

            // Si el tamaño de la Whitelist de los jugadores es 100, tengo que evitar que nuevos jugadores entren, ya que los cupos se han llenado
            if (whitelist.size() >= 100) {
                if (staffWhitelist.contains(player.getName())) return; // *A no ser que sea un Staff, entonces puedes entrar y salir sin problemas!
                // Kickear al jugador recien conectado (porque no se otra forma de "cancelar" el que el jugador se meta al servidor)
                player.kick(Text.miniMessage("<red>Lo sentimos, ya se ha llenado el cupo de jugadores."), PlayerKickEvent.Cause.KICK_COMMAND);
                return; // Si la whitelist se ha llenado, lo que hay de aqui para abajo me da igual a si que cortamos aqui.
            }

            // Si el codigo ha llegado hasta aqui es porque aun quedan cupos disponibles. Y si un jugador nuevo se mete habrá que darle uno de los cupos.
            // (A no ser que no queramos, pero con los comandos se pueden borrar de la whitelist)
            if (!whitelist.contains(player.getName())) {
                whitelist.add(player.getName()); // Añado al jugador en caso de que no estuviera antes.
            }

            // A cualquier jugador que entre, le ponemos el contador en caso de que no lo tenga ya.
            if (this.config.contains("players.invulnerability-time")) {
                if (!player.getPersistentDataContainer().has(InvulnerabilityManager.INVULNERABILITY_KEY)) {
                    player.getPersistentDataContainer().set(InvulnerabilityManager.INVULNERABILITY_KEY, PersistentDataType.INTEGER, this.config.getInt("players.invulnerability-time"));
                }
            } else {
                // En caso de que se haya borrado ese parametro de la configuracion, se lo pongo en 0 y es como si no tuviera invulnerabilidad
                // (Escribiendo esto me doy cuenta de que si no lo encuentra creo que lo pondria 0 igual por eso de ser un int, pero bueno, asi al menos me aseguro :P)
                if (!player.getPersistentDataContainer().has(InvulnerabilityManager.INVULNERABILITY_KEY)) {
                    player.getPersistentDataContainer().set(InvulnerabilityManager.INVULNERABILITY_KEY, PersistentDataType.INTEGER, 0);
                    this.plugin.LOGGER.warning("player.invulnerability-time no encontrado en config.yml, Se ha establecido como valor 0!");
                }
            }

        } else {
            // Vuelvo a crear las listas en caso de que las borraran, eso si, sin jugadores :(.
            this.config.set("player.whitelist", new ArrayList<>());
            this.config.set("player.staff-whitelist", new ArrayList<>());
            this.plugin.saveConfig();
        }
    }
}
