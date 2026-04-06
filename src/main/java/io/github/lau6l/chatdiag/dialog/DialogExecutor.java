package io.github.lau6l.chatdiag.dialog;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.util.Schedulable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DialogExecutor {
    // 80 WPM with delayMultiplier == 1
    public static final float BASE_DELAY = 15;
    public static final int MIN_DELAY = (int) (20 * 2.5);

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players) {
        return startDialog(DialogLoader.loadDialog(id), players);
    }

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(DialogLine line, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
        sendDialog(line, players, future);
        return future;
    }

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
        sendDialog(dialog, players, 0, future);
        return future;
    }

    private static void sendDialog(Dialog dialog, Collection<ServerPlayerEntity> players, int i, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        dialog.get(i).map(
                str -> {
                    sendString(dialog.line(i), players, dialog.sound());
                    return str;
                },
                line -> {
                    sendLine(line, players);
                    return line;
                }
        );

        if (i + 1 < dialog.lines().size()) {
            int delay = (int) (Math.max(MIN_DELAY, dialog.words(i) * BASE_DELAY) * dialog.delayMultiplier());
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

    private static void sendDialog(DialogLine line, Collection<ServerPlayerEntity> players, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        sendLine(line, players);

        future.complete(players);
    }

    private static void sendString(String line, Collection<ServerPlayerEntity> players, Sound sound) {
        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(line), false);
            if (sound != null)
                player.playSound(
                        SoundEvent.of(sound.id()),
                        16,
                        sound.pitch()
                );
        }
    }

    private static void sendLine(DialogLine line, Collection<ServerPlayerEntity> players) {
        List<Sound> sounds = line.sound();
        boolean hasSound = sounds != null;

        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(line.get()), false);
            if (hasSound)
                for (Sound sound : sounds)
                    player.playSound(
                            SoundEvent.of(sound.id()),
                            16,
                            sound.pitch());
        }
    }
}

