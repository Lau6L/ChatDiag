package io.github.lau6l.chatdiag.dialog;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class CommandContainer {
    public final String command;
    private ServerCommandSource source;
    private CommandDispatcher<ServerCommandSource> dispatcher;

    public CommandContainer(String command, ServerCommandSource source, CommandDispatcher<ServerCommandSource> dispatcher) {
        this.command = command;
        this.source = source;
        this.dispatcher = dispatcher;
    }

    public CommandContainer(String command) {
        this.command = command;
        this.source = null;
        dispatcher = null;
    }

    public ServerCommandSource source() {
        return source;
    }

    public void source(ServerCommandSource source) {
        this.source = source;
    }

    public CommandDispatcher<ServerCommandSource> dispatcher() {
        return dispatcher;
    }

    public void dispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.dispatcher = dispatcher;
    }
}
