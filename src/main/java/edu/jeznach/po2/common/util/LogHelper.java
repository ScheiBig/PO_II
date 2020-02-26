package edu.jeznach.po2.common.util;

import edu.jeznach.po2.common.log.Log;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static edu.jeznach.po2.common.util.Throwables.*;

/**
 * Provides functionality to perform unsafe (throwing exceptions) actions and log any possible
 * exceptions. This allows to wrap calls in lambdas / method references to distinct them from
 * normal try/catch blocks (that should be used to resolve exceptions in non-terminating way),
 * omit calling logging methods every time, allow easily changing logging implementation without
 * refactoring every usage.
 */
public class LogHelper {

    private Log log;

    public LogHelper(Log log) {
        this.log = log;
    }

    /**
     * Tries to perform unsafe action, catching and logging any possible exception.
     * <br><br>
     * <p>This is closing method that fires {@code unsafeAction}
     * @param unsafeAction the action to perform
     * @param errorMessage the message to add to log entry if exception was thrown from
     *                     {@code unsafeAction}
     * @return {@code false} if action was successfully performed, {@code true} if exception
     *         was cached from it
     */
    public boolean expect(Funcionables.Runnable unsafeAction, String errorMessage) {
        try {
            unsafeAction.run();
            return false;
        } catch (Exception e) {
            log.exception(errorMessage, e);
            return true;
        }
    }

    /**
     * Tries to perform unsafe action, catching and logging any possible exception. This
     * method returns {@link LogHelperWrapper} that allows to specify action performed in
     * catch block.
     * @param unsafeAction the action to perform
     * @param errorMessage the message to add to log entry if exception was thrown from
     *                     {@code unsafeAction}
     * @return wrapper of expecting action, that should be resolved
     */
    public LogHelperWrapper expecting(Funcionables.Runnable unsafeAction, String errorMessage) {
        return new LogHelperWrapper(unsafeAction, errorMessage, log);
    }

    public static class LogHelperWrapper {

        private final Funcionables.Runnable unsafeAction;
        private final String errorMessage;
        private final Log log;

        private LogHelperWrapper(Funcionables.Runnable unsafeAction,
                                 String errorMessage,
                                 Log log) {
            this.unsafeAction = unsafeAction;
            this.errorMessage = errorMessage;
            this.log = log;
        }

        /**
         * Perform specified action if {@code unsafeAction} throws exception. Action will
         * be performed after exception was logged.
         * <br><br>
         * <p>This is closing method that fires {@code unsafeAction}
         * @param resolveAction the action to perform in catch block
         * @return wrapper of resolving action and of wrapper of expecting action,
         * that should be finalized
         */
        public boolean resolve(Runnable resolveAction) {
            try {
                unsafeAction.run();
                return false;
            } catch (Exception e) {
                log.exception(errorMessage, e);
                resolveAction.run();
                return true;
            }
        }


        /**
         * Perform specified action if {@code unsafeAction} throws exception. Action will
         * be performed after exception was logged.
         * <br><br>
         * <p>This is closing method that fires {@code unsafeAction}
         * @param resolveAction the action to perform in catch block
         * @return {@code false} if action was successfully performed, {@code true} if exception
         *         was cached from it
         */
        public LogHelperWrappersWrapper resolving(Runnable resolveAction) {
            return new LogHelperWrappersWrapper(unsafeAction, errorMessage, resolveAction, log);
        }

        public static class LogHelperWrappersWrapper {

            private final Funcionables.Runnable unsafeAction;
            private final String errorMessage;
            private final Runnable resolveAction;
            private final Log log;

            private LogHelperWrappersWrapper(Funcionables.Runnable unsafeAction,
                                            String errorMessage,
                                            Runnable resolveAction,
                                            Log log) {

                this.unsafeAction = unsafeAction;
                this.errorMessage = errorMessage;
                this.resolveAction = resolveAction;
                this.log = log;
            }

            /**
             * Perform specified action whether {@code unsafeAction} throws exception or not.
             * Action will be performed in finally block
             * @param finalizeAction the action to perform in finally block
             * @return {@code false} if action was successfully performed, {@code true} if exception
             *         was cached from it
             */
            public boolean finalize(Runnable finalizeAction) {
                boolean ret;
                try {
                    unsafeAction.run();
                    ret = false;
                } catch (Exception e) {
                    log.exception(errorMessage, e);
                    resolveAction.run();
                    ret = true;
                } finally {
                    finalizeAction.run();
                }
                return ret;
            }
        }
    }

    public static @NotNull String date() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),
                                                  TimeZone.getDefault().toZoneId()));
    }
}
