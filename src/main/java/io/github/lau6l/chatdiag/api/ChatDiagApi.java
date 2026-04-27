package io.github.lau6l.chatdiag.api;

import io.github.lau6l.chatdiag.dialog.Dialog;
import io.github.lau6l.chatdiag.dialog.DialogExecutor;
import io.github.lau6l.chatdiag.dialog.DialogLoader;
import io.github.lau6l.chatdiag.dialog.Dialogs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChatDiagApi {
    private static final ChatDiagApi INSTANCE = new ChatDiagApi();

    private ChatDiagApi() {
    }

    public static ChatDiagApi api() {
        return INSTANCE;
    }

    public Dialog getDialog(Identifier id) {
        return Dialogs.getDialog(id);
    }

    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players) {
        return DialogExecutor.startDialog(id, players);
    }

    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        return DialogExecutor.startDialog(dialog, players);
    }

    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return DialogExecutor.startDialogWithFuture(id, players, future);
    }

    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return DialogExecutor.startDialogWithFuture(dialog, players, future);
    }

    public String serializeDialog(Dialog dialog) {
        return DialogLoader.serializeDialog(dialog);
    }

    public Dialog deserializeDialog(String json) {
        return DialogLoader.deserializeDialog(json);
    }
}
