package io.github.lau6l.chatdiag.dialog;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.util.Schedulable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DialogExecutor {
    // 80 WPM with delayMultiplier == 1
    public static final float BASE_DELAY = 15;
    public static final int MIN_DELAY = (int) (20 * 2.5);

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Identifier id, Collection<ServerPlayerEntity> players) {
        return startDialog(DialogLoader.loadDialog(id), players);
    }

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
        sendDialog(dialog, players, 0, future);
        return future;
    }

    private static void sendDialog(Dialog dialog, Collection<ServerPlayerEntity> players, int i, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        Sound sound = dialog.sound();
        boolean hasSound = sound != null;

        Identifier soundId = hasSound ? Identifier.of(sound.id()) : null;
        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(dialog.line(i)), false);
            if (hasSound)
                player.playSound(
                        SoundEvent.of(soundId),
                        16,
                        sound.pitch());
        }

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
}

