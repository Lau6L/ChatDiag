package io.github.lau6l.chatdiag.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class CodecUtil {
    public static <O, A> Function<O, Optional<A>> opt(@NotNull Function<O, A> nonOptionalGetter) {
        return nonOptionalGetter.andThen(Optional::ofNullable);
    }
}
