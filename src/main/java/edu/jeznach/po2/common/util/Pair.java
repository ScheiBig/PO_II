package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a two-element tuple, with naming following {@link Map.Entry} structure.
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class Pair<K, V> {

    /**
     * Key of this {@code Pair}
     */
    public final K key;
    /**
     * Value of this {@code Pair}
     */
    public final V value;

    /**
     * Creates pair of given arguments.
     * @param key key of this {@code Pair}
     * @param value key of this {@code Pair}
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }


    /**
     * Creates pair of given arguments.
     * @param key key of this {@code Pair}
     * @param value key of this {@code Pair}
     * @return Created {@code Pair}
     */
    public static <K, V> @NotNull Pair<K, V> of(K key, V value) { return new Pair<>(key, value); }

    /**
     * Transforms this {@code Pair} to {@link Map.Entry}.
     * @return Implementation of {@link Map.Entry} using this {@code Pair} fields as initial values;
     */
    public @NotNull Map.Entry<K, V> toMapEntry() {
        return new Map.Entry<K, V>() {

            private K k = key;
            private V v = value;

            @Override
            public K getKey() {
                return k;
            }

            @Override
            public V getValue() {
                return v;
            }

            @Override
            public V setValue(V v) {
                V t = this.v;
                this.v = v;
                return t;
            }
        };
    }
}
