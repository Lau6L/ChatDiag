package io.github.lau6l.chatdiag.dialog;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record Dialog (List<Either<String, DialogLine>> lines, int delayMultiplier, @Nullable String prefix, @Nullable String suffix, @Nullable Sound sound) {
    public static final Dialog EMPTY = new Dialog(List.of(), 1, "", "", null);

    public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.either(Codec.STRING, DialogLine.CODEC).listOf().fieldOf("lines").forGetter(Dialog::lines),
                    Codec.INT.optionalFieldOf("delay_multiplier", 1).forGetter(Dialog::delayMultiplier),
                    Codec.STRING.optionalFieldOf("prefix", null).forGetter(Dialog::prefix),
                    Codec.STRING.optionalFieldOf("suffix", null).forGetter(Dialog::suffix),
                    Sound.CODEC.optionalFieldOf("sound", null).forGetter(Dialog::sound)
            ).apply(instance, Dialog::new)
    );

    public static @NonNull String orBlank(@Nullable String string) {
        return string == null ? "" : string;
    }

    public Either<String, DialogLine> get(int index) {
        return lines.get(index);
    }

    public String line(int index) {
        return lines.get(index).map(
                str -> orBlank(prefix) + str + orBlank(suffix),
                line -> line.get(prefix, suffix)
        );
    }

    public int words(int index) {
        String l = lines.get(index).map(
                str -> str,
                DialogLine::line
        );
        boolean inWord = false;
        int wordCount = 0;
        for (int i = 0; i < l.length(); i++) {
            if (Character.isWhitespace(l.charAt(i))) {
                inWord = false;
            } else if (!inWord) {
                wordCount++;
                inWord = true;
            }
        }
        return wordCount;
    }

    @Override
    public @NonNull String toString() {
        return "Dialog{" +
                "lines=" + lines +
                ", delayMultiplier=" + delayMultiplier +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", sound=" + sound +
                '}';
    }
}