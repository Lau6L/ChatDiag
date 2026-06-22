package io.github.lau6l.chatdiag.client;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.dialog.Sound;
import io.github.lau6l.chatdiag.network.SoundS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class ChatDiagClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChatDiag.LOGGER.info("Initializing ChatDiag client...");

        ClientPlayNetworking.registerGlobalReceiver(SoundS2CPayload.ID, (payload, context) -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            Sound sound = payload.sound();
            player.getEntityWorld().playSoundClient(
                    SoundEvent.of(
                            sound.id()
                    ),
                    SoundCategory.MASTER,
                    sound.pitch(),
                    sound.volume()
            );
        });
    }
}
