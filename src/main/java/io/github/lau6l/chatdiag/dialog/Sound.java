package io.github.lau6l.chatdiag.dialog;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static io.github.lau6l.chatdiag.util.CodecUtil.opt;

/**
 * Represents a sound effect entry attached to a dialog or line.
 *
 * @param id the sound's identifier
 * @param pitch the sound's pitch
 * @param volume the sound's volume
 * @param positions the sound's positions. If null, plays a client-side non-localized sound; plays at the specified
 *                  positions otherwise. Can be {@link Either} a {@link Vec3d} or an Entity Selector string, which
 *                  will be computed on dialog execution.
 */
public record Sound(Identifier id, float pitch, float volume, @Nullable List<Either<Vec3d, String>> positions) {
    public static final Codec<Either<Vec3d, String>> POS_CODEC = Codec.either(Vec3d.CODEC, Codec.STRING);
    public static final Codec<Sound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(Sound::id),
                    Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(Sound::pitch),
                    Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(Sound::volume),
                    Codecs.listOrSingle(POS_CODEC).optionalFieldOf("pos").forGetter(opt(Sound::positions))
            ).apply(instance, Sound::new)
    );
    public static final PacketCodec<RegistryByteBuf, Sound> PACKET_CODEC_NO_POS = PacketCodec.tuple(
            Identifier.PACKET_CODEC, Sound::id,
            PacketCodecs.FLOAT, Sound::pitch,
            PacketCodecs.FLOAT, Sound::volume,
            (id, pitch, volume) -> new Sound(id, pitch, volume, (List<Either<Vec3d, String>>) null)
    );

    // this optional constructor and the use of CodecUtil.opt() are here to simplify dialog structure to be nullable and digestible by the codec
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Sound(Identifier id, float pitch, float volume, Optional<List<Either<Vec3d, String>>> positions) {
        this(id, pitch, volume, positions.orElse(null));
    }
}
