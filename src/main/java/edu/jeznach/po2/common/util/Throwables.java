package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Contains utility methods that extend functionality of native {@link Throwable}
 */
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
}
