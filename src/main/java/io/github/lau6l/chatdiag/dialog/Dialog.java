package io.github.lau6l.chatdiag.dialog;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A full chat dialog definition. Loaded from {@link DialogLoader}.
 * <p>
 * A dialog contains a sequence of lines, a delay multiplier that controls relative pacing between
 * lines, optional global prefix and suffix texts, and an optional sound list.
 * <p>
 * A line can be represented as either a {@link String} or a {@link DialogLine}. {@code DialogLine}s
 * are complex versions of a string line, with custom prefix, suffix, and sound attributes, as well as
 * override options.
 *
 * @see DialogLine
 * @see Sound
 * @see DialogLoader
 * @see DialogExecutor
 */
public record Dialog (List<Either<String, DialogLine>> lines, int delayMultiplier, @Nullable String prefix, @Nullable String suffix, @Nullable List<Sound> sound) {
    /** An empty dialog used when data loading fails. */
    public static final Dialog EMPTY = new Dialog(List.of(), 1, "", "", null);

    public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.either(Codec.STRING, DialogLine.CODEC).listOf().fieldOf("lines").forGetter(Dialog::lines),
                    Codec.INT.optionalFieldOf("delay_multiplier", 1).forGetter(Dialog::delayMultiplier),
                    Codec.STRING.optionalFieldOf("prefix", null).forGetter(Dialog::prefix),
                    Codec.STRING.optionalFieldOf("suffix", null).forGetter(Dialog::suffix),
                    Codecs.listOrSingle(Sound.CODEC).optionalFieldOf("sound", null).forGetter(Dialog::sound)
            ).apply(instance, Dialog::new)
    );

    /**
     * Returns a given string, or an empty string if the value is {@code null}.
     *
     * @param string the string to check
     * @return the original string, or {@code ""}
     */
    public static @NonNull String orBlank(@Nullable String string) {
        return string == null ? "" : string;
    }

    /**
     * Returns the line at the given index.
     *
     * @param index the line index
     * @return the corresponding line entry
     */
    public Either<String, DialogLine> get(int index) {
        return lines.get(index);
    }

    /**
     * Applies prefix and suffix texts to a line, obeying {@link DialogLine} overriding settings if the line is complex.
     *
     * @param index the line index
     * @return the rendered line text
     */
    public String line(int index) {
        return lines.get(index).map(
                str -> orBlank(prefix) + str + orBlank(suffix),
                line -> line.get(prefix, suffix)
        );
    }

    /**
     * Counts the number of words in the selected line. Does not count prefixes or suffixes.
     * <p>
     * This is used to calculate the delay before the next line is sent.
     *
     * @param index the line index
     * @return the number of whitespace-delimited substrings
     */
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