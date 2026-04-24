package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lau6l.chatdiag.util.CodecHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static io.github.lau6l.chatdiag.dialog.Dialog.opt;
import static io.github.lau6l.chatdiag.dialog.Dialog.orBlank;

/**
 * Represents a single complex line inside a dialog.
 * <p>
 * A line can override its parent dialog's prefix, suffix, and sound behavior.
 *
 * @see Dialog
 */
public record DialogLine(String line, boolean replacePrefix, boolean replaceSuffix, boolean replaceSound, @Nullable String prefix, @Nullable String suffix, int delay, @Nullable List<Sound> sound) {
    public static final Codec<DialogLine> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("line").forGetter(DialogLine::line),
                    Codec.BOOL.optionalFieldOf("replace_prefix", false).forGetter(DialogLine::replacePrefix),
                    Codec.BOOL.optionalFieldOf("replace_suffix", false).forGetter(DialogLine::replaceSuffix),
                    Codec.BOOL.optionalFieldOf("replace_sound", false).forGetter(DialogLine::replaceSound),
                    Codec.STRING.optionalFieldOf("prefix").forGetter(opt(DialogLine::prefix)),
                    Codec.STRING.optionalFieldOf("suffix").forGetter(opt(DialogLine::suffix)),
                    Codec.INT.optionalFieldOf("delay", -1).forGetter(DialogLine::delay),
                    CodecHelper.listOrSingle(Sound.CODEC).optionalFieldOf("sound").forGetter(opt(DialogLine::sound))
            ).apply(instance, DialogLine::new)
    );

    // this optional constructor and the use of opt() are here to simplify dialog structure to be nullable and digestible by the codec
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DialogLine(String line, boolean replacePrefix, boolean replaceSuffix, boolean replaceSound, Optional<String> prefix, Optional<String> suffix, int delay, Optional<List<Sound>> sound) {
        this(line, replacePrefix, replaceSuffix, replaceSound, prefix.orElse(null), suffix.orElse(null), delay, sound.orElse(null));
    }
    /**
     * Returns this dialog line text, applying overrides to its parent dialog's prefix and suffix.
     *
     * @param prefix the dialog-level prefix
     * @param suffix the dialog-level suffix
     * @return the rendered line text
     */
    public String get(@Nullable String prefix, @Nullable String suffix) {
        return (replacePrefix ? "" : orBlank(prefix))
                + get()
                + (replaceSuffix ? "" : orBlank(suffix));
    }

    private String get() {
        return orBlank(this.prefix) + line + orBlank(this.suffix);
    }
}
