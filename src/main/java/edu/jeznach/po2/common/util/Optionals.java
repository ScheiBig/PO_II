package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Contains utility methods that extend functionality of native {@link Optional}
 */
public class Optionals {


    /**
     * Returns an {@link Optional} with value retrieved from specified {@link Supplier},
     * if that value is {@code null}, or calling {@link Supplier#get()} results in {@code Exception},
     * then return is {@link Optional#empty()}
     * @param valueSupplier the {@code Supplier} of value to enclose in {@code Optional}
     * @param <T> the class of the value
     * @return {@code Optional} with provided value if calling {@code Supplier.get()} produces non-null
     *         value and no {@code Exception}, otherwise an empty {@code Optional}
     */
    @SuppressWarnings("ConstantConditions")
    public static <T> @NotNull Optional<T> ofThrowable(@Nullable Supplier<T> valueSupplier) {
        try {
            return Optional.ofNullable(valueSupplier.get());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
     * <br><br>
     * <p>This mocks java 9 {@code Optional#ifPresentOrElse(Consumer, Runnable)}
     * @param optional the optional to perform actions on
     * @param ifPresent the action to be performed, if a value is present
     * @param ifEmpty the empty-based action to be performed, if no value is present
     * @param <T> the class of the value
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void ifPresentOrElse(Class<T> type,
                                           Optional<T> optional,
                                           Consumer<T> ifPresent,
                                           Runnable ifEmpty) {
        if (optional.isPresent()) {
            ifPresent.accept(type.cast(optional.get()));
        } else {
            ifEmpty.run();
        }
    }

    /**
     * Represents a supplier of results.
     * <p></p>There is no requirement that a new or distinct result be returned each time the supplier is invoked.
     * This is a functional interface whose functional method is get() that can throw {@code Exceptions}.
     * @param <T> the type of results supplied by this supplier
     */
    public interface Supplier<T>{

        /**
         * Gets a result.
         * @return a results
         * @throws Exception when unhandled {@code Exception} is thrown in this method
         */
        @Nullable T get() throws Exception;
    }
}
