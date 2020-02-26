package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Contains utility methods that extend functionality of native {@link Throwable}
 */
@SuppressWarnings("SpellCheckingInspection")
public class Throwables {

    /**
     * Calls {@link Throwable#printStackTrace(PrintStream)} to internal {@link StringWriter},
     * retrieving {@code stackTrace} as {@code String}
     * @param throwable the {@link Throwable} to get {@code stackTrace} of
     * @return the {@code stackTrace} string of provided exception
     */
    public static @NotNull String getStackTrace(@NotNull Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Functional interfaces that sopport throwing exceptions in their functional methods.
     */
    public static class Funcionables {

        /**
         * Represents a supplier of results.
         * <p>There is no requirement that a new or distinct result be returned each time the supplier is invoked.
         * This is a functional interface whose functional method is get() that can throw {@code Exceptions}.
         * @param <T> the type of results supplied by this supplier
         */
        @FunctionalInterface
        public interface Supplier<T> {

            /**
             * Gets a result.
             * @return a results
             * @throws Exception when unhandled {@code Exception} is thrown in this method
             */
            @Nullable T get() throws Exception;
        }

        @FunctionalInterface
        public interface Runnable {

            void run() throws Exception;
        }
    }
}
