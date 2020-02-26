package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;

/**
 * Contains utility methods that extends functionality of native {@link String}
 */
public class Strings {

    /**
     * Returns a string that is substring of the {@code s}. The substring is trimmed
     * of {@code charCount} last characters
     * <p>Example:
     * <blockquote><pre>
     * Strings.substringTrim("hamburger", 4) returns "hambu"
     * </pre></blockquote>
     * @param s the string to produce substring from
     * @param charCount how many characters to remove from the {@code s}
     * @return the specified substring
     * @throws IndexOutOfBoundsException if the {@code charCount} is negative, or larger
     *         than {@code s} length
     */
    @NotNull public static String substringTrim(@NotNull String s, int charCount)
            throws IndexOutOfBoundsException {
        if (charCount < 0)
            throw new StringIndexOutOfBoundsException(charCount);
        int l = s.length();
        if (charCount > l)
            throw new StringIndexOutOfBoundsException(charCount);
        return s.substring(0, l - charCount);
    }
}
