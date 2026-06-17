package io.github.lau6l.chatdiag.dialog;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.network.SoundS2CPayload;
import io.github.lau6l.chatdiag.util.Schedulable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Sends dialogs as individual lines to one or more server players.
 * <p>
 * Schedules delayed delivery between dialog lines for monologue pacing.
 *
 * @see Dialog
 * @see DialogLoader
 */
public class DialogExecutor {
    /**
     * Starts a dialog event.
     *
     * @param id a dialog's namespaced id
     * @param players the players to show this dialog to
     * @param source a command source to execute, only needed if the given dialog includes {@link CommandContainer}s.
     * @return a future, which will complete when the dialog has finished.
     */
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players, ServerCommandSource source) {
        return startDialog(Dialogs.get(id, source), players);
    }

    /**
     * Starts a dialog event. A source should be included within the given dialog through {@link Dialog#withSource(ServerCommandSource)}
     *
     * @param dialog an in-memory dialog
     * @param players the players to show this dialog to
     * @return a future, which will complete when the dialog has finished.
     */
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
        sendDialog(dialog, players, 0, future);
        return future;
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
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialogWithFuture(Identifier id, Collection<ServerPlayerEntity> players, ServerCommandSource source, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        return startDialogWithFuture(Dialogs.get(id).withSource(source), players, future);
    }

    /**
     * Starts a chained dialog event. A source should be included within the given dialog through {@link Dialog#withSource(ServerCommandSource)}
     *
     * @param dialog an in-memory dialog
     * @param players the players to show this dialog to
     * @param future a completable future, which may be from a previous dialog
     * @return the given {@code future}, which will complete when all dialogs in the chain have finished.
     */
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialogWithFuture(Dialog dialog, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        if (dialog == null) {
            future.complete(players);
            return future;
        }
        sendDialog(dialog, players, 0, future);
        return future;
    }

    /**
     * Sends the dialog line at the given index and schedules the next line, if one exists.
     *
     * @param dialog the dialog being sent
     * @param players the target players
     * @param i the current line index
     * @param future the dialog's future
     */
    private static void sendDialog(Dialog dialog, Collection<ServerPlayerEntity> players, int i, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        if (i >= dialog.lines().size()) {
            if (dialog.nextDialog() != null) {
                startDialogWithFuture(
                        dialog.nextDialog(),
                        players,
                        dialog.nextCommand() == null ? null : dialog.nextCommand().source(),
                        future);
            } else {
                future.complete(players);
            }
            executeCommand(dialog.nextCommand());
            return;
        }

        int delay = executeLineAndGetDelay(dialog, players, i);

        new Schedulable(
                () -> sendDialog(
                        dialog,
                        players,
                        i + 1,
                        future
                ),
                delay
        )
                .schedule()
                .exceptionally((e) -> {
                    ChatDiag.LOGGER.error("Error executing dialog:", e);
                    return true;
                });
    }

    /**
     * Sends the line at the given dialog index and returns a delay for the next line to be sent.
     *
     * @param dialog a dialog
     * @param players the players to send this line to
     * @param i the dialog index to send
     * @return a delay based on the line's words and WPM
     */
    private static int executeLineAndGetDelay(Dialog dialog, Collection<ServerPlayerEntity> players, int i) {
        return dialog.get(i).map(
                str -> {
                    sendString(dialog.line(i), players, dialog.sound());
                    return getSimpleDelay(dialog, i);
                },
                line -> {
                    sendLine(line, players, dialog.sound(), dialog.prefix(), dialog.suffix());
                    if (line.command() != null) {
                        executeCommand(line.command());
                    }
                    return line.delay() == -1 ?
                            getSimpleDelay(dialog, i)
                            : line.delay();
                }
        );
    }

    /**
     * Executes a given {@link CommandContainer}. The container should have a source already set.
     *
     * @param commandContainer the command and source to execute
     */
    private static void executeCommand(@Nullable CommandContainer commandContainer) {
        if (commandContainer == null || commandContainer.command == null) return;
        try {
            ServerCommandSource source = commandContainer.source();
            source.getDispatcher()
                    .execute(
                            commandContainer.command,
                            source
                    );
        } catch (CommandSyntaxException e) {
            ChatDiag.LOGGER.error("There was an error trying to execute dialog command:", e);
        }
    }

    /**
     * Returns the given line's delay, excluding any custom {@link DialogLine#delay()}.
     */
    private static int getSimpleDelay(Dialog dialog, int i) {
        return (int) Math.max(dialog.minDelay(), dialog.words(i) * 60 * 20 / dialog.wpm());
    }

    /**
     * Sends a single line to the given players, as a plain string.
     *
     * @param line the text to send
     * @param players the target players
     * @param sounds sounds to play when the message appears
     */
    public static void sendString(String line, Collection<ServerPlayerEntity> players, @Nullable List<Sound> sounds) {
        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(line), false);
        }
        if (sounds != null) {
            for (Sound sound : sounds) {
                playSound(players, sound);
            }
        }
    }

    /**
     * Sends a single line to the given players, as a {@code DialogLine}. Applies
     * overrides.
     *
     * @param line the dialog line to send
     * @param players the target players
     * @param defaultSounds the dialog-level fallback sounds
     * @param prefix the parent dialog's prefix
     * @param suffix the parent dialog's suffix
     */
    public static void sendLine(DialogLine line, Collection<ServerPlayerEntity> players, @Nullable List<Sound> defaultSounds, @Nullable String prefix, @Nullable String suffix) {
        List<Sound> sounds = line.sound();
        boolean hasSound = sounds != null;

        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(line.get(prefix, suffix)), false);
        }
        if (defaultSounds != null && !line.replaceSound()) {
            for (Sound defaultSound : defaultSounds) {
                playSound(players, defaultSound);
            }
        }
        if (hasSound) {
            for (Sound sound : sounds) {
                playSound(players, sound);
            }
        }
    }

    /**
     * Sends a single line to the given players, as a {@code DialogLine}.
     *
     * @param line the dialog line to send
     * @param players the target players
     */
    public static void sendLine(DialogLine line, Collection<ServerPlayerEntity> players) {
        sendLine(line, players, null, null, null);
    }

    private static void playSound(Collection<ServerPlayerEntity> players, Sound sound) {
        List<Either<Vec3d, String>> pos = sound.positions();

        if (pos == null) {
            playSoundsNoPos(players, sound);
        } else {
            playSoundsPos(players, sound, computePositions(pos));
        }
    }

    private static final EntityArgumentType ENTITY_ARGUMENT_TYPE = EntityArgumentType.entities();
    private static ServerCommandSource SOURCE;

    public static void initialize(MinecraftServer server) {
        SOURCE = new ServerCommandSource(
                CommandOutput.DUMMY,
                Vec3d.ZERO,
                Vec2f.ZERO,
                server.getOverworld(),
                LeveledPermissionPredicate.GAMEMASTERS,
                "",
                Text.empty(),
                server,
                null
        );
    }

    private static List<Vec3d> computePositions(List<Either<Vec3d, String>> pos) {
        List<Vec3d> computedPositions = new ArrayList<>();
        for (Either<Vec3d, String> position : pos) {
            position.map(
                    computedPositions::add,
                    entitySelector -> {
                        try {
                            computedPositions.addAll(
                                    ENTITY_ARGUMENT_TYPE
                                            .parse(new StringReader(entitySelector))
                                            .getEntities(SOURCE)
                                            .stream()
                                            .map(Entity::getEntityPos)
                                            .toList()
                            );
                        } catch (Exception ignored) {
                        }
                        return null;
                    }
            );
        }
        return computedPositions;
    }

    private static void playSoundsNoPos(Collection<ServerPlayerEntity> players, Sound sound) {
        SoundS2CPayload payload = new SoundS2CPayload(sound);
        SoundEvent soundEvent = SoundEvent.of(sound.id());

        for (ServerPlayerEntity player : players) {
            if (ServerPlayNetworking.canSend(player, SoundS2CPayload.ID)) {
                ServerPlayNetworking.send(
                        player,
                        payload
                );
            } else {
                playSound(
                        player,
                        player.getEntityPos(),
                        soundEvent,
                        sound.pitch(),
                        sound.volume()
                );
            }
        }
    }

    public static void playSoundsPos(Collection<ServerPlayerEntity> players, Sound sound, List<Vec3d> positions) {
        SoundEvent soundEvent = SoundEvent.of(sound.id());

        for (ServerPlayerEntity player : players) {
            for (Vec3d position : positions) {
                playSound(
                        player,
                        position,
                        soundEvent,
                        sound.pitch(),
                        sound.volume()
                );
            }
        }
    }

    private static void playSound(ServerPlayerEntity player, Vec3d pos, SoundEvent soundEvent, float pitch, float volume) {
        player.getEntityWorld()
                .playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        soundEvent,
                        SoundCategory.MASTER,
                        volume,
                        pitch
                );
    }
}

