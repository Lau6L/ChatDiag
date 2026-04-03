package io.github.lau6l.chatdiag.dialog;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.util.Schedulable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.Strings;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DialogExecutor {
    // 80 WPM with delayMultiplier == 1
    public static final float BASE_DELAY = 15;
    public static final int MIN_DELAY = (int) (20 * 2.5);

    public static CompletableFuture<Collection<ServerPlayerEntity>> startDialog(Dialog dialog, Collection<ServerPlayerEntity> players) {
        CompletableFuture<Collection<ServerPlayerEntity>> future = new CompletableFuture<>();
        sendDialog(dialog, players, 0, future);
        return future;
    }

    private static void sendDialog(Dialog dialog, Collection<ServerPlayerEntity> players, int i, CompletableFuture<Collection<ServerPlayerEntity>> future) {
        for (ServerPlayerEntity player : players) {
            player.sendMessageToClient(Text.of(dialog.line(i)), false);
            if (Strings.isNotBlank(dialog.soundId()))
                player.playSound(
                        SoundEvent.of(Identifier.of(dialog.soundId())),
                        16,
                        dialog.soundPitch());
        }

        int delay = (int) (Math.max(MIN_DELAY, dialog.wordsInLine(i) * BASE_DELAY) * dialog.delayMultiplier());

        if (i + 1 < dialog.lines().size()) {
            new Schedulable(() ->
                    sendDialog(
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

