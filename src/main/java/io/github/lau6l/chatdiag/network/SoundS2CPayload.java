package io.github.lau6l.chatdiag.network;

import io.github.lau6l.chatdiag.ChatDiag;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SoundS2CPayload(String soundId, int pitch) implements CustomPayload {
    public static final Identifier SOUND_PAYLOAD_ID = ChatDiag.of("sound");
    public static final CustomPayload.Id<SoundS2CPayload> ID = new Id<>(SOUND_PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, SoundS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SoundS2CPayload::soundId,
            PacketCodecs.INTEGER, SoundS2CPayload::pitch,
            SoundS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
