package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static io.github.lau6l.chatdiag.dialog.Dialog.opt;

/**
 * Represents a sound effect entry attached to a dialog or line.
 *
 * @param id the sound's identifier
 * @param pitch the sound's pitch
 */
public record Sound(Identifier id, float pitch, @Nullable Vec3d position) {
    public static final Codec<Sound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(Sound::id),
                    Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(Sound::pitch),
                    Vec3d.CODEC.optionalFieldOf("pos").forGetter(opt(Sound::position))
            ).apply(instance, Sound::new)
    );

    // this optional constructor and the use of opt() are here to simplify dialog structure to be nullable and digestible by the codec
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Sound(Identifier id, float pitch, Optional<Vec3d> position) {
        this(id, pitch, (Vec3d) null);
    }

    public Sound(Identifier id, float pitch) {
        this(id, pitch, (Vec3d) null);
    }

    public Sound(Identifier id) {
        this(id, 1, (Vec3d) null);
    }

    @Override
    public @NonNull String toString() {
        return "Sound{" +
                "id='" + id + '\'' +
                ", pitch=" + pitch +
                '}';
    }
}
