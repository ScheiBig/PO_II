package edu.jeznach.po2.common.util;


import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the three-arity specialization of {@link Function}.
 * <p>This is a {@link java.util.function functional interface}
 * whose functional method is {@link #apply(Object, Object, Object)}.
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <R> the type of the result of the function
 * @see Function
 * @see BiFunction
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    /**
     * Applies this function to the given arguments.
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    R apply(T t, U u, V v);
}
