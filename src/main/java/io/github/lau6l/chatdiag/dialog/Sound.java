package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.NonNull;

public record Sound(String id, float pitch) {
    public static final Codec<Sound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("id").forGetter(Sound::id),
                    Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(Sound::pitch)
            ).apply(instance, Sound::new)
    );

    @Override
    public @NonNull String toString() {
        return "Sound{" +
                "id='" + id + '\'' +
                ", pitch=" + pitch +
                '}';
    }
}
