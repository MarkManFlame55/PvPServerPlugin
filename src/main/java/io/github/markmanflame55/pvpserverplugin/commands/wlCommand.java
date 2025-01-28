package io.github.markmanflame55.pvpserverplugin.commands;

import io.github.markmanflame55.pvpserverplugin.PvPServerPlugin;
import io.github.markmanflame55.pvpserverplugin.events.InvulnerabilityManager;
import io.github.markmanflame55.pvpserverplugin.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class wlCommand implements CommandExecutor, TabCompleter {

    /*

        /wl add <Player>
        /wl remove <Player>
        /wl addstaff <Player>
        /wl removestaff <Player>
        /wl get_invulnerability <Player>
        /wl set_invulnerability <Player> <int>
        /wl help

    */

    PvPServerPlugin plugin = PvPServerPlugin.getPlugin();
    FileConfiguration config = this.plugin.getConfig();


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {

            // Subcomando guia para los staff del Evento (no se cuantos habrá pero al menos pueden hacer cosas)
            if (args[0].equals("help")) {
                List<Component> helpMessage = new ArrayList<>();
                helpMessage.add(Text.miniMessage("<gold>==================="));
                helpMessage.add(commandHelper("/wl add <Player>", "Añade un jugador a la Whitelist del evento"));
                helpMessage.add(commandHelper("/wl remove <Player>", "Elimina a un jugador de la whitelist del evento"));
                helpMessage.add(commandHelper("/wl addstaff <Player>", "Añade a un jugador como Staff del evento"));
                helpMessage.add(commandHelper("/wl removestaff <Player>", "Elimina a un jugador como Staff del Evento"));
                helpMessage.add(commandHelper("/wl get_invulnerability <Player>", "Muesta los segundos de invulnerabilidad restantes del Jugador"));
                helpMessage.add(commandHelper("/wl set_invulnerability <Player> <int>", "Cambia los segundos de invulnerabilidad restantes del Jugador"));
                helpMessage.add(commandHelper("/wl help", "Mostrar esta lista"));
                for (Component message : helpMessage) {
                    commandSender.sendMessage(message);
                }
            } else {
                commandSender.sendMessage(Text.miniMessage("<red>Argumentos Incorrectos! Mira /wl help para mas informacion sobre los comandos!"));
                if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
            }
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "add" -> {

                    // Añadir jugadores a la whitelist.

                    String playerName = args[1]; // Recibo el string para poder añadir jugadores sin que esten online.
                    List<String> whitelist = this.config.getStringList("players.whitelist");
                    if (!whitelist.contains(playerName)) {
                        whitelist.add(playerName); // Añadimos el nuevo nombre a la whitelist

                        Player target = Bukkit.getPlayer(playerName);
                        if (target != null) {
                            // Caso muy extremo, por si un jugador que (por alguna razón que desconozco) esta conectado de antes y no forma parte del evento,
                            // poder añadirlo y que empiece a jugar sin tener que relogear.

                            target.getPersistentDataContainer().set(InvulnerabilityManager.INVULNERABILITY_KEY, PersistentDataType.INTEGER, this.config.getInt("players-invulnerability-time"));
                        }


                        this.config.set("players.whitelist", whitelist);
                        this.plugin.saveConfig(); // Guardamos la nueva whitelist, sin necesidad de reinicio de server.
                        commandSender.sendMessage(Text.miniMessage("<green>Se ha registrado a " + playerName + " al Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>" + playerName + " ya esta apuntado en el Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                }
                case "remove" -> {

                    // Borrar a un jugador de la whitelist
                    // FIXME No creo que sea necesario, pero al borrar un jugador de la whitelist, no se le resetea el contador de invulnerabilidad. Estaria bien borrarlo de alguna manera pero creo que ahora mismo complicaria mas las cosas.

                    String playerName = args[1]; // Recibimos el String para poder recibir jugadores que no esten conectados.
                    List<String> whitelist = this.config.getStringList("players.whitelist");
                    if (whitelist.contains(playerName)) {
                        whitelist.remove(playerName); // Borramos el jugador de la whitelist
                        this.config.set("players.whitelist", whitelist);
                        this.plugin.saveConfig(); // Y guardamos la nueva whitelist.
                        commandSender.sendMessage(Text.miniMessage("<green>Se ha eliminado a " + playerName + " del Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>" + playerName + " no esta registrado en el Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                }
                case "addstaff" -> {

                    // Añadir miembros del Staff al evento.
                    // IMPORTANTE: Añado esto en mente de que tal vez estemos algunos jugadores en espectador UNICAMENTE mirando y moderando el servidor.
                    // Este comando es la forma de añadir staffs a la lista. Los miembros del staff tienen bypass de la whitelist y el plugin no les cuenta
                    // como jugadores del evento.

                    String playerName = args[1]; // Recibir String para aceptar jugadores desconectados
                    List<String> staffWhitelist = this.config.getStringList("players.staff-whitelist");
                    if (!staffWhitelist.contains(playerName)) {
                        staffWhitelist.add(playerName); // Añadimos el nuevo staff a la whitelist
                        this.config.set("players.staff-whitelist", staffWhitelist);
                        this.plugin.saveConfig(); // Y guardamos la nueva whitelist de Staffs.
                        commandSender.sendMessage(Text.miniMessage("<green>Se ha agregado a " + playerName + " como Staff del Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>" + playerName + " ya es un Staff del Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                }
                case "removestaff" -> {

                    // Quitar miembros de la lista de Staffs.
                    // Si algun jugador staff luego quiere participar en el evento, hay que borrarle de la lista de Staffs.

                    String playerName = args[1]; // Recibir String para aceptar jugadore conectados.
                    List<String> staffWhitelist = this.config.getStringList("players.staff-whitelist");
                    if (staffWhitelist.contains(playerName)) {
                        staffWhitelist.remove(playerName); // Eliminar el staff de la lista.
                        this.config.set("players.staff-whitelist", staffWhitelist);
                        this.plugin.saveConfig(); // Guardar la nueva lista
                        commandSender.sendMessage(Text.miniMessage("<green>Se ha eliminado a " + playerName + " como Staff del Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>" + playerName + " no es un Staff del Evento!"));
                        if (commandSender instanceof Player player) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                }
                case "get_invulnerability" -> {

                    // Mostrar el tiempo de Invulnerabilidad restando de un jugador

                    Player player = Bukkit.getPlayer(args[1]); // En este comando, si que tiene que ser un jugador conectado.
                    if (player != null) { // Comprobar que el jugador esta conectado
                        if (player.getPersistentDataContainer().has(InvulnerabilityManager.INVULNERABILITY_KEY)) {

                            // Para que sea mas comodo para el staff, en vez de mostrar que quedan "1234 segundos", hago que diga "05m43s"

                            int totalSeconds = player.getPersistentDataContainer().get(InvulnerabilityManager.INVULNERABILITY_KEY, PersistentDataType.INTEGER);
                            int minutesLeft = (totalSeconds % 3600) / 60;
                            int secondsLeft = totalSeconds % 60;

                            String minutesText;
                            String secondsText;

                            // Para que quede mas bonito y en caso de que sean 5 minutos y 3 segundso no diga "5m3s" si no que diga "05m03s"
                            if (minutesLeft < 10) {
                                minutesText = "0" + minutesLeft;
                            } else {
                                minutesText = Integer.toString(minutesLeft);
                            }

                            if (secondsLeft < 10) {
                                secondsText = "0" + secondsLeft;
                            } else {
                                secondsText = Integer.toString(secondsLeft);
                            }

                            commandSender.sendMessage(Text.miniMessage("<aqua>Invulnerabilidad Restante de " + player.getName() + ": <white>" + minutesText + "m" + secondsText + "s"));
                            if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                        } else {
                            commandSender.sendMessage(Text.miniMessage("<red>Que Raro! Parece que " + player.getName() + " nunca tuvo invulnerabilidad!"));
                            if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                        }
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>Ese jugador no existe o no esta conectado!"));
                        if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                }
                default -> {
                    commandSender.sendMessage(Text.miniMessage("<red>Argumentos Incorrectos! Mira /wl help para mas informacion sobre los comandos!"));
                    if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                }
            }
        }
        if (args.length == 3) {
            if (args[0].equals("set_invulnerability")) {

                // Cambiar el tiempo restante de Invulnerabilidad a un jugador.
                // Por si hay que reiniciarselo o ha perdido algo de tiempo por culpa del server :P

                Player player = Bukkit.getPlayer(args[1]); // Obtener el jugador
                if (player != null) { // Y comprobar si esta conectado.
                    if (isNumber(args[2])) { // Asegurarme de que el 3º parametro del comando sea un entero positivo.
                        int newSeconds = Integer.parseInt(args[2]);
                        if (newSeconds < 0) {
                            commandSender.sendMessage(Text.miniMessage("<red>El tiempo introducido no es valido! Introduce un numero entero positivo!"));
                        }
                        // Y aplicar el nuevo tiempo.
                        player.getPersistentDataContainer().set(InvulnerabilityManager.INVULNERABILITY_KEY, PersistentDataType.INTEGER, newSeconds);
                        commandSender.sendMessage(Text.miniMessage("<green>" + player.getName() + " tiene ahora "  + newSeconds + " segundos restantes de Invulnerabilidad!"));
                        if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
                    } else {
                        commandSender.sendMessage(Text.miniMessage("<red>El tiempo introducido no es valido! Introduce un numero entero positivo!"));
                        if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                    }
                } else {
                    commandSender.sendMessage(Text.miniMessage("<red>Ese jugador no existe o no esta conectado!"));
                    if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
                }
            } else {
                commandSender.sendMessage(Text.miniMessage("<red>Argumentos Incorrectos! Mira /wl help para mas informacion sobre los comandos!"));
                if (commandSender instanceof Player sender) sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.1f);
            }
        }
        return true;
    }

    private Component commandHelper(String command, String description) {
        return Text.miniMessage("<gray>- <gold><b>" + command + "</b><white>: " + description);
    }

    private boolean isNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // Me aseguro de que cada subcomando muestra las tabulaciones que le corresponde.
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1 && commandSender instanceof Player) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[0], List.of("add", "remove", "addstaff", "removestaff", "get_invulnerability", "set_invulnerability", "help"), completions);
            Collections.sort(completions);
            return completions;
        }
        if (strings.length == 2 && commandSender instanceof Player) {
            if (strings[0].equals("help")) {
                return List.of("");
            }
            if (strings[0].equals("remove")) {
                return this.config.getStringList("players.whitelist");
            }
            if (strings[0].equals("removestaff")) {
                return this.config.getStringList("players.staff-whitelist");
            }
        }
        if (strings.length == 3 && commandSender instanceof Player) {
            if (strings[0].equals("set_invulnerability")) {
                return List.of("<new_seconds>");
            }
        }
        return null;
    }
}
