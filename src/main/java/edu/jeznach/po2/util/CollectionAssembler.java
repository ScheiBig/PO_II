package edu.jeznach.po2.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides type-safe methods for assembling vararg parameters to collections.
 */
public class CollectionAssembler {

    /**
     * Assembles {@link Map} object from given parameters.
     * @param keyClass the {@link Class} modeling type used as a {@link Map} key type
     * @param valueClass the {@link Class} modeling type used as a {@link Map} value type
     * @param pairs the array/varargs of {@link Pair Pairs} to populate {@link Map} with
     * @param <K> the type of a {@link Map} key
     * @param <V> the type of a {@link Map} value
     * @return Assembled {@link Map} object
     * @throws KeyCastException if a {@link Pair#key key} of one of {@link Pair Pairs} is not instance of class represented by {@code keyClass}
     * @throws ValueCastException if a {@link Pair#value value} of one of {@link Pair Pairs} is not instance of class represented by {@code valueClass}
     */
    public static <K, V> Map<K, V> map(@NotNull Class<K> keyClass,
                                       @NotNull Class<V> valueClass,
                                       Pair<?, ?> ... pairs)
            throws KeyCastException, ValueCastException {
        Map<K, V> ret = new HashMap<>();
        for (Pair<?, ?> pair : pairs) {
            if (!keyClass.isInstance(pair.key)) throw new KeyCastException();
            if (!valueClass.isInstance(pair.value)) throw new ValueCastException();
            ret.put((K)pair.key, (V)pair.value);
        }
        return ret;
    }

    /**
     * Used to indicate that vararg parameter {@link Pair#key key} failed type-safe instance check
     */
    public static class KeyCastException extends ClassCastException {}
    /**
     * Used to indicate that vararg parameter {@link Pair#value value} failed type-safe instance check
     */
    public static class ValueCastException extends ClassCastException {}
}
