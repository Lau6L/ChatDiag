package io.github.lau6l.chatdiag.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.lau6l.chatdiag.dialog.Dialog;
import io.github.lau6l.chatdiag.dialog.DialogLoader;

public class DialogArgumentType implements ArgumentType<Dialog> {
    public DialogArgumentType() {
    }

    public static DialogArgumentType dialog() {
        return new DialogArgumentType();
    }

    public static <S> Dialog getDialog(CommandContext<S> context, String name) {
        return context.getArgument(name, Dialog.class);
    }

    public Dialog parse(StringReader stringReader) {
        String string = readString(stringReader);
        return DialogLoader.deserializeDialog(string);
    }

    private static String readString(StringReader reader) {
        int i = reader.getCursor();
        while (reader.canRead()) {
            reader.skip();
        }
        return reader.getString().substring(i, reader.getCursor());
    }
}
