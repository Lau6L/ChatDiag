package io.github.lau6l.chatdiag;

import io.github.lau6l.chatdiag.api.ChatDiagApi;
import io.github.lau6l.chatdiag.command.ChatDiagCommand;
import io.github.lau6l.chatdiag.dialog.Dialog;
import io.github.lau6l.chatdiag.dialog.DialogExecutor;
import io.github.lau6l.chatdiag.dialog.DialogLoader;
import io.github.lau6l.chatdiag.dialog.Dialogs;
import io.github.lau6l.chatdiag.util.Scheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChatDiag implements ModInitializer, ChatDiagApi {
    public static final String MOD_ID = "chatdiag";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ChatDiag...");

        Scheduler.initialize();
        registerCommands();
        DialogLoader.registerReloadListener();
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ChatDiagCommand.register(dispatcher)
        );
    }

    public static Identifier of(String value) {
        return Identifier.of(MOD_ID, value);
    }

    private static final ChatDiagApi INSTANCE = new ChatDiag();
    public static ChatDiagApi api() {
        return INSTANCE;
    }

    @Override
    public Dialog getDialog(Identifier id) {
        return Dialogs.getDialog(id);
    }

    @Override
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players) {
        return DialogExecutor.startDialog(id, players);
    }

    @Override
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        return DialogExecutor.startDialog(dialog, players);
    }

    @Override
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return DialogExecutor.startDialogWithFuture(id, players, future);
    }

    @Override
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return DialogExecutor.startDialogWithFuture(dialog, players, future);
    }

    @Override
    public String serializeDialog(Dialog dialog) {
        return DialogLoader.serializeDialog(dialog);
    }

    @Override
    public Dialog deserializeDialog(String json) {
        return DialogLoader.deserializeDialog(json);
    }
}
