package io.github.lau6l.chatdiag.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.dialog.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class ChatDiagCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("chatdiag")
                        .requires(CommandManager.requirePermissionLevel(1))
                        .then(
                                CommandManager.argument("players", EntityArgumentType.players())
                                        .then(getIdDialogBranch())
                                        .then(getDataDialogBranch())
                        )
        );

        dispatcher.register(CommandManager.literal("cdiag").redirect(literalCommandNode));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> getIdDialogBranch() {
        return CommandManager.literal("id")
                .then(
                        CommandManager.argument("dialog_id", IdentifierArgumentType.identifier())
                                .suggests(new DialogSuggestionProvider())
                                .executes(context -> {
                                    try {
                                        DialogExecutor.startDialog(
                                                IdentifierArgumentType.getIdentifier(context, "dialog_id"),
                                                EntityArgumentType.getPlayers(context, "players")
                                        );
                                        return 1;
                                    } catch (Exception e) {
                                        ChatDiag.LOGGER.error("Error executing stored dialog:", e);
                                        return 0;
                                    }
                                })
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> getDataDialogBranch() {
        return CommandManager.literal("data")
                .then(
                        CommandManager.argument("dialog_data", DialogArgumentType.dialog())
                                .executes(context -> {
                                    try {
                                        DialogExecutor.startDialog(
                                                DialogArgumentType.getDialog(context, "dialog_data"),
                                                EntityArgumentType.getPlayers(context, "players")
                                        );
                                        return 1;
                                    } catch (Exception e) {
                                        ChatDiag.LOGGER.error("Error executing custom dialog:", e);
                                        return 0;
                                    }
                                })
                );
    }

    public static class DialogSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            Dialogs.getDialogIds().forEach((d) -> builder.suggest(d.toString()));
            return builder.buildFuture();
        }
    }
}
