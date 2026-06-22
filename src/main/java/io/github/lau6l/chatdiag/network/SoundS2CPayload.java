package io.github.lau6l.chatdiag.network;

import io.github.lau6l.chatdiag.ChatDiag;
import io.github.lau6l.chatdiag.dialog.Sound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SoundS2CPayload(Sound sound) implements CustomPayload {
    public static final Identifier SOUND_PAYLOAD_ID = ChatDiag.of("sound");
    public static final CustomPayload.Id<SoundS2CPayload> ID = new Id<>(SOUND_PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, SoundS2CPayload> CODEC = PacketCodec.tuple(
            Sound.PACKET_CODEC_NO_POS, SoundS2CPayload::sound,
            SoundS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
