package io.github.lau6l.chatdiag.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatDiagCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("chatdiag")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(
                                CommandManager.argument("players", EntityArgumentType.players())
                                        .then(getStoredDialogBranch())
                                        .then(getCreatedDialogBranch())
                        )
        );

        dispatcher.register(CommandManager.literal("cdiag").redirect(literalCommandNode));
    }
    private static LiteralArgumentBuilder<ServerCommandSource> getStoredDialogBranch() {
        return CommandManager.literal("dialog")
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
    private static LiteralArgumentBuilder<ServerCommandSource> getCreatedDialogBranch() {
        return CommandManager.literal("line")
                .then(
                        CommandManager.argument("line", MessageArgumentType.message())
                                .then(
                                        CommandManager.argument("sound", IdentifierArgumentType.identifier())
                                                .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                                                .then(
                                                        CommandManager.argument("pitch", FloatArgumentType.floatArg(0, 2))
                                                                .executes(context -> {
                                                                    try {
                                                                        DialogExecutor.sendLine(
                                                                                new DialogLine(
                                                                                        MessageArgumentType.getMessage(context, "line").getString(),
                                                                                        false,
                                                                                        false,
                                                                                        false,
                                                                                        null,
                                                                                        null,
                                                                                        -1,
                                                                                        List.of(new Sound(
                                                                                                IdentifierArgumentType.getIdentifier(context, "sound"),
                                                                                                FloatArgumentType.getFloat(context, "pitch")
                                                                                        ))
                                                                                ),
                                                                                EntityArgumentType.getPlayers(context, "players"),
                                                                                null,
                                                                                null,
                                                                                null
                                                                        );
                                                                        return 1;
                                                                    } catch (Exception e) {
                                                                        ChatDiag.LOGGER.error("Error executing created dialog:", e);
                                                                        throw e;
                                                                    }
                                                                })
                                                )
                                )
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
