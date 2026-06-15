package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static io.github.lau6l.chatdiag.util.CodecUtil.opt;

/**
 * Represents a sound effect entry attached to a dialog or line.
 *
 * @param id the sound's identifier
 * @param pitch the sound's pitch
 */
public record Sound(Identifier id, float pitch, float volume, @Nullable Vec3d position) {
    public static final Codec<Sound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(Sound::id),
                    Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(Sound::pitch),
                    Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(Sound::volume),
                    Vec3d.CODEC.optionalFieldOf("pos").forGetter(opt(Sound::position))
            ).apply(instance, Sound::new)
    );
    public static final PacketCodec<RegistryByteBuf, Sound> PACKET_CODEC_NO_POS = PacketCodec.tuple(
            Identifier.PACKET_CODEC, Sound::id,
            PacketCodecs.FLOAT, Sound::pitch,
            PacketCodecs.FLOAT, Sound::volume,
            (id, pitch, volume) -> new Sound(id, pitch, volume, (Vec3d) null)
    );

    // this optional constructor and the use of opt() are here to simplify dialog structure to be nullable and digestible by the codec
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Sound(Identifier id, float pitch, float volume, Optional<Vec3d> position) {
        this(id, pitch, volume, position.orElse(null));
    }
}
