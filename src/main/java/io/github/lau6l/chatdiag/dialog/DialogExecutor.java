package io.github.lau6l.chatdiag.dialog;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.util.Schedulable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
     * Constant representing a base delay between lines.
     * As a reference, when a {@link Dialog#delayMultiplier}'s is
     * 1, this normalizes each line to 80 Words Per Minute.
     */
    public static final float BASE_DELAY = 15;

    /**
     * Minimum tick delay between lines. Equivalent to 2.5 seconds.
     * <p>
     * If a line is complex (i.e. {@link DialogLine}), its {@link DialogLine#delay}
     * attribute can override this.
     */
    public static final int MIN_DELAY = (int) (20 * 2.5);

    /**
     * Loads a dialog from data and starts sending it to the given players.
     *
     * @param id the dialog identifier
     * @param players the target players
     * @return the resulting dialog's future
     */
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players) {
        return startDialog(DialogLoader.loadDialog(id), players);
    }

    /**
     * Starts sending an in-memory dialog to the given players.
     *
     * @param dialog the dialog to send
     * @param players the target players
     * @return the dialog's future
     */
    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
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
        dialog.get(i).map(
                str -> {
                    sendString(dialog.line(i), players, dialog.sound());
                    return str;
                },
                line -> {
                    sendLine(line, players, dialog.sound(), dialog.prefix(), dialog.suffix());
                    return line;
                }
        );

        if (i + 1 < dialog.lines().size()) {
            int delay = dialog.get(i).map(
                    str -> (int) (Math.max(MIN_DELAY, dialog.words(i) * BASE_DELAY) * dialog.delayMultiplier()),
                    DialogLine::delay
            );
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
        } else future.complete(players);
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
            if (sounds != null) playSounds(player, sounds);
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
            if (defaultSounds != null && !line.overrideSound()) playSounds(player, defaultSounds);
            if (hasSound) playSounds(player, sounds);
        }
    }

    private static void playSounds(ServerPlayerEntity player, List<Sound> sounds) {
        for (Sound sound : sounds) {
            player.playSound(
                    SoundEvent.of(sound.id()),
                    16,
                    sound.pitch());
        }
    }
}

