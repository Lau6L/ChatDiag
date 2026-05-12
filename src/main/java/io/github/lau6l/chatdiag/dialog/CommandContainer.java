package io.github.lau6l.chatdiag.dialog;

import net.minecraft.server.command.ServerCommandSource;

public class CommandContainer {
    public final String command;
    private ServerCommandSource source;

    public CommandContainer(String command, ServerCommandSource source) {
        this.command = command;
        this.source = source;
    }

    public CommandContainer(String command) {
        this.command = command;
        this.source = null;
    }

    public ServerCommandSource source() {
        return source;
    }

    public void source(ServerCommandSource source) {
        this.source = source;
    }
}
