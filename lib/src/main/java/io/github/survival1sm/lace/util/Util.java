package io.github.survival1sm.lace.util;

import java.util.Collection;
import java.util.stream.Stream;

public class Util {

    /**
     * Null safe stream.
     *
     * @param collection a {@link Collection} of {@link T}
     * @return type {@link Stream} of {@link T}
     */
    public static <T> Stream<T> nullSafeStream(Collection<T> collection) {
        if (collection == null) {
            return Stream.empty();
        } else {
            return collection.stream();
        }
    }
}
