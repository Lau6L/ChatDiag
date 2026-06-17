package io.github.lau6l.chatdiag.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class CodecUtil {
    /** Helper method for codecs, turns a nullable variable function into an {@link Optional} object. */
    public static <O, A> Function<O, Optional<A>> opt(@NotNull Function<O, A> nonOptionalFunction) {
        return nonOptionalFunction.andThen(Optional::ofNullable);
    }
}
