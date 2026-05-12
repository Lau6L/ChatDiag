package io.github.lau6l.chatdiag.dialog;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Collection of all loaded dialogs.
 */
public class Dialogs {
    private static Map<Identifier, Dialog> dialogs;

    public static Set<Identifier> getIds() {
        return dialogs.keySet();
    }
    public static Dialog get(Identifier id) {
        return dialogs.get(id);
    }
    public static Dialog get(Identifier id, ServerCommandSource source) {
        return dialogs.get(id).withSource(source);
    }

    public static void setDialogs(Map<Identifier, Dialog> newDialogs) {
        dialogs = new HashMap<>(newDialogs);
    }
}
