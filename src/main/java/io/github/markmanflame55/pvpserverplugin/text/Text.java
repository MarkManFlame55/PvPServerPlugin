package io.github.markmanflame55.pvpserverplugin.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Text {

    public static Component miniMessage(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
