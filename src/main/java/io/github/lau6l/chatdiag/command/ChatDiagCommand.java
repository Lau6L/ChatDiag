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
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
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
                                .executes(context -> execute(
                                        EntityArgumentType.getPlayers(context, "players"),
                                        MessageArgumentType.getMessage(context, "line").getString(),
                                        null
                                ))
                                .then(
                                        CommandManager.argument("sound", IdentifierArgumentType.identifier())
                                                .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                                                .executes(context -> execute(
                                                        EntityArgumentType.getPlayers(context, "players"),
                                                        MessageArgumentType.getMessage(context, "line").getString(),
                                                        List.of(new Sound(
                                                                IdentifierArgumentType.getIdentifier(context, "sound")
                                                        ))
                                                ))
                                                .then(
                                                        CommandManager.argument("pitch", FloatArgumentType.floatArg(0, 2))
                                                                .executes(context -> execute(
                                                                        EntityArgumentType.getPlayers(context, "players"),
                                                                        MessageArgumentType.getMessage(context, "line").getString(),
                                                                        List.of(new Sound(
                                                                                IdentifierArgumentType.getIdentifier(context, "sound"),
                                                                                FloatArgumentType.getFloat(context, "pitch")
                                                                        ))
                                                                ))
                                                                .then(
                                                                        CommandManager.argument("position", Vec3ArgumentType.vec3())
                                                                                .executes(context -> execute(
                                                                                        EntityArgumentType.getPlayers(context, "players"),
                                                                                        MessageArgumentType.getMessage(context, "line").getString(),
                                                                                        List.of(new Sound(
                                                                                                IdentifierArgumentType.getIdentifier(context, "sound"),
                                                                                                FloatArgumentType.getFloat(context, "pitch"),
                                                                                                Vec3ArgumentType.getVec3(context, "position")
                                                                                        ))
                                                                                ))
                                                                )
                                                )
                                )
                );
    }

    private static int execute(Collection<ServerPlayerEntity> players, String line, List<Sound> sound) {
        try {
            DialogExecutor.sendLine(
                    new DialogLine(
                            line,
                            sound
                    ),
                    players
            );
            return 1;
        } catch (Exception e) {
            ChatDiag.LOGGER.error("Error executing created dialog:", e);
            throw e;
        }
    }

    public static class DialogSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            Dialogs.getDialogIds().forEach((d) -> builder.suggest(d.toString()));
            return builder.buildFuture();
        }
    }
}
