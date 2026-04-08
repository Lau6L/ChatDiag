package io.github.lau6l.chatdiag.dialog;

import io.github.lau6l.chatdiag.ChatDiag;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of dialog identifiers. Registry-like.
 * <p>
 * Dialogs are collected during startup and frozen on server start.
 */
public class Dialogs {
    private static List<Identifier> dialogs = new ArrayList<>();

    private static Identifier register(String value) {
        return register(ChatDiag.of(value));
    }
    public static Identifier register(Identifier id) {
        dialogs.add(id);
        return id;
    }
    public static void freeze() {
        dialogs = List.copyOf(dialogs);
    }
    public static List<Identifier> getDialogs() {
        return dialogs;
    }
}
