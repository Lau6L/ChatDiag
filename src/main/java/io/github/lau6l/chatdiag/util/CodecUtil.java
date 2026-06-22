package io.github.lau6l.chatdiag.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CodecUtil {
    /** Helper method for codecs, turns a nullable variable function into an {@link Optional} object. */
    public static <O, A> Function<O, Optional<A>> opt(@NotNull Function<O, A> nonOptionalFunction) {
        return nonOptionalFunction.andThen(Optional::ofNullable);
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec) {
        return listOrSingle(entryCodec, entryCodec.listOf());
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, entryCodec).xmap((either) -> (List)either.map((list) -> list, List::of), (list) -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}
