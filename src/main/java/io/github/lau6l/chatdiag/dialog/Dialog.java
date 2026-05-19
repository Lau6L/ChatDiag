package io.github.lau6l.chatdiag.dialog;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A full chat dialog definition. Loaded from {@link DialogLoader}.
 * <p>
 * A dialog contains:
 * <ul>
 *     <li>A sequence of lines</li>
 *     <li>A line prefix</li>
 *     <li>A line suffix</li>
 *     <li>Words per Minute</li>
 *     <li>A minimum delay between lines</li>
 *     <li>A list of sounds</li>
 *     <li>An identifier for the next dialog in a dialog chain</li>
 *     <li>A {@link CommandContainer} to execute a command</li>
 * </ul>
 * <p>
 * A line can be represented as either a {@link String} or a {@link DialogLine}. {@code DialogLine}s
 * are complex versions of a string line, with custom prefix, suffix, delay, sound, and command attributes, as well as
 * override options.
 * <p>
 * Chained dialogs share the same future, which will only complete when the entire series of dialogs
 * is complete.
 *
 * @see DialogLine
 * @see Sound
 * @see CommandContainer
 * @see DialogLoader
 * @see DialogExecutor
 */
public record Dialog (List<Either<String, DialogLine>> lines, double wpm, @Nullable String prefix, @Nullable String suffix, @Nullable List<Sound> sound, @Nullable Identifier nextDialog, @Nullable CommandContainer nextCommand, int minDelay) {
    /** An empty dialog used when data loading fails. */
    public static final Dialog EMPTY = new Dialog(List.of(), 1, (String) null, null, null, null, null, 0);

    public static final Codec<Dialog> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.either(Codec.STRING, DialogLine.CODEC).listOf().fieldOf("lines").forGetter(Dialog::lines),
                    Codec.DOUBLE.optionalFieldOf("delay_multiplier", 1.0).forGetter(Dialog::wpm),
                    Codec.STRING.optionalFieldOf("prefix").forGetter(opt(Dialog::prefix)),
                    Codec.STRING.optionalFieldOf("suffix").forGetter(opt(Dialog::suffix)),
                    Codecs.listOrSingle(Sound.CODEC).optionalFieldOf("sound").forGetter(opt(Dialog::sound)),
                    Identifier.CODEC.optionalFieldOf("next_dialog").forGetter(opt(Dialog::nextDialog)),
                    Codec.STRING.optionalFieldOf("next_command").forGetter(opt(dialog ->
                            dialog.nextCommand == null ?
                                    null
                                    : dialog.nextCommand.command)),
                    Codec.INT.optionalFieldOf("minimum_delay", 50).forGetter(Dialog::minDelay)
            ).apply(instance, Dialog::new)
    );

    public static <O, A> Function<O, Optional<A>> opt(Function<O, A> nonOptionalGetter) {
        return nonOptionalGetter.andThen(Optional::ofNullable);
    }
    // this optional constructor and the use of opt() are here to simplify dialog structure to be nullable and digestible by the codec
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Dialog(List<Either<String, DialogLine>> lines, double delayMultiplier, Optional<String> prefix, Optional<String> suffix, Optional<List<Sound>> sound, Optional<Identifier> nextDialog, Optional<String> nextCommand, int minDelay) {
        this(lines, delayMultiplier, prefix.orElse(null), suffix.orElse(null), sound.orElse(null), nextDialog.orElse(null), new CommandContainer(nextCommand.orElse(null)), minDelay);
    }

    /**
     * Returns a given string, or an empty string if the value is {@code null}.
     *
     * @param string the string to check
     * @return the original string, or {@code ""}
     */
    public static @NotNull String orBlank(@Nullable String string) {
        return string == null ? "" : string;
    }

    /**
     * Populates this dialog's {@link Dialog#nextCommand} with a {@link ServerCommandSource} if present and returns itself.
     *
     * @param source the command source
     * @return this dialog
     */
    public Dialog withSource(ServerCommandSource source) {
        if (nextCommand != null) {
            nextCommand.source(source);
        }
        return this;
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
     * Applies prefix and suffix texts to a line, overriding settings if the line is a {@link DialogLine}.
     *
     * @param index the line index
     * @return the rendered line text string
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
                ", wpm=" + wpm +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", sound=" + sound +
                ", nextDialog=" + nextDialog +
                ", nextCommand=" + nextCommand +
                ", minDelay=" + minDelay +
                '}';
    }
}