package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record Dialog (List<String> lines, int delayMultiplier, String prefix, String suffix, @Nullable Sound sound) {
    public static final Dialog EMPTY = new Dialog(List.of(), 1, "", "", null);
    public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.listOf().fieldOf("lines").forGetter(Dialog::lines),
                    Codec.INT.optionalFieldOf("delay_multiplier", 1).forGetter(Dialog::delayMultiplier),
                    Codec.STRING.optionalFieldOf("prefix", "").forGetter(Dialog::prefix),
                    Codec.STRING.optionalFieldOf("suffix", "").forGetter(Dialog::suffix),
                    Sound.CODEC.optionalFieldOf("sound", null).forGetter(Dialog::sound)
            ).apply(instance, Dialog::new)
    );

    public static Dialog createLine(String line, String prefix, String suffix, String soundId, float soundPitch) {
        return new Dialog(
                List.of(line),
                1,
                prefix,
                suffix,
                new Sound(
                        soundId,
                        soundPitch
                )
        );
    }
    public static Dialog createLine(String line, String prefix, String suffix, Sound sound) {
        return new Dialog(
                List.of(line),
                1,
                prefix,
                suffix,
                sound
        );
    }

    public static @NonNull String orBlank(@Nullable String string) {
        return string == null ? "" : string;
    }

    public String line(int index) {
        return orBlank(prefix) + lines.get(index) + orBlank(suffix);
    }

    public int wordsInLine(int index) {
        String l = lines.get(index);
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