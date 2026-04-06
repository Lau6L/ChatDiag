package io.github.lau6l.chatdiag.dialog;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.github.lau6l.chatdiag.dialog.Dialog.orBlank;

public record DialogLine(String line, boolean overridePrefix, boolean overrideSuffix, @Nullable String prefix, @Nullable String suffix, int delay, @Nullable List<Sound> sound) {
    public static final Codec<DialogLine> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("line").forGetter(DialogLine::line),
                    Codec.BOOL.optionalFieldOf("override_prefix", false).forGetter(DialogLine::overridePrefix),
                    Codec.BOOL.optionalFieldOf("override_suffix", false).forGetter(DialogLine::overrideSuffix),
                    Codec.STRING.optionalFieldOf("prefix", null).forGetter(DialogLine::prefix),
                    Codec.STRING.optionalFieldOf("suffix", null).forGetter(DialogLine::suffix),
                    Codec.INT.optionalFieldOf("delay", -1).forGetter(DialogLine::delay),
                    Codecs.listOrSingle(Sound.CODEC).optionalFieldOf("sound", null).forGetter(DialogLine::sound)
            ).apply(instance, DialogLine::new)
    );

    public String get(String prefix, String suffix) {
        return (overridePrefix ? orBlank(prefix) : "")
                + get()
                + (overrideSuffix ? orBlank(suffix) : "");
    }

    public String get() {
        return orBlank(this.prefix) + line + orBlank(this.suffix);
    }
}
