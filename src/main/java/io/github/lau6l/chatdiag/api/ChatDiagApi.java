package io.github.lau6l.chatdiag.api;

import io.github.lau6l.chatdiag.dialog.Dialog;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ChatDiagApi {
    Dialog getDialog(Identifier id);
    CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players);
    CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players);
    CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future);
    CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future);
    String serializeDialog(Dialog dialog);
    Dialog deserializeDialog(String json);
}
