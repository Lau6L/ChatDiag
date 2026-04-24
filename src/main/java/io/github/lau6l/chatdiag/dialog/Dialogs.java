package io.github.lau6l.chatdiag.dialog;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

/**
 * Collection of all loaded dialogs.
 */
public class Dialogs {
    public static Map<Identifier, Dialog> dialogs;

    public static Set<Identifier> getDialogIds() {
        return dialogs.keySet();
    }
    public static Dialog getDialog(Identifier id) {
        return dialogs.get(id);
    }

    public static void setDialogs(Map<Identifier, Dialog> newDialogs) {
        dialogs = newDialogs;
    }
}
