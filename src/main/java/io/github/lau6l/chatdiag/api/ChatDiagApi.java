package io.github.lau6l.chatdiag.api;

import io.github.lau6l.chatdiag.dialog.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChatDiagApi {
    private static final ChatDiagApi INSTANCE = new ChatDiagApi();

    private ChatDiagApi() {
    }

    /**
     * Retrieves the ChatDiag API
     */
    public static ChatDiagApi api() {
        return INSTANCE;
    }

    /**
     * Returns a dialog by namespaced id.
     */
    public Dialog getDialog(Identifier id) {
        return Dialogs.get(id);
    }

    /**
     * Starts a dialog event.
     *
     * @param id a dialog's namespaced id
     * @param players the players to show this dialog to
     * @param source a command source to execute, only needed if the given dialog includes {@link CommandContainer}s.
     * @return a future, which will complete when the dialog has finished.
     */
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, ServerCommandSource source) {
        return DialogExecutor.startDialog(id, players, source);
    }

    /**
     * Starts a dialog event. A source should be included within the given dialog through {@link Dialog#withSource(ServerCommandSource)}
     *
     * @param dialog an in-memory dialog
     * @param players the players to show this dialog to
     * @return a future, which will complete when the dialog has finished.
     */
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        return DialogExecutor.startDialog(dialog, players);
    }

    /**
     * Starts a chained dialog event.
     *
     * @param id the dialog's namespaced id
     * @param players the players to show this dialog to
     * @param source a command source to execute, only needed if the given dialog includes {@link CommandContainer}s.
     * @param future a completable future, which may be from a previous dialog
     * @return the given {@code future}, which will complete when all dialogs in the chain have finished.
     */
    public CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, ServerCommandSource source, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return DialogExecutor.startDialogWithFuture(id, players, source, future);
    }

    /**
     * Starts a chained dialog event. A source should be included within the given dialog through {@link Dialog#withSource(ServerCommandSource)}
     *
     * @param dialog am in-memory dialog
     * @param players the players to show this dialog to
     * @param future a completable future, which may be from a previous dialog
     * @return the given {@code future}, which will complete when all dialogs in the chain have finished.
     */
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
