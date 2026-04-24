package io.github.lau6l.chatdiag.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;

public class CodecHelper {
    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec) {
        return listOrSingle(entryCodec, entryCodec.listOf());
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, entryCodec).xmap((either) -> (List)either.map((list) -> list, List::of), (list) -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}
