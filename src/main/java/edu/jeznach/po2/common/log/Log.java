package edu.jeznach.po2.common.log;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.util.CollectionAssembler;
import edu.jeznach.po2.common.util.Optionals;
import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Used for logging messages to console and file, if provided.
 * <br><br>
 * <p>All public method of this class are thread-safe, as underlying {@link PrintStream PrintStreams} are.
 * <p>This implementation uses {@link PrintStream},
 * so it is important to call {@link #close()} to release resources when no longer needed.
 */
public class Log
        implements Closeable {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<PrintStream> logStream;

    private ColoredPrinter printer = new ColoredPrinter.Builder(0, false).build();
    private synchronized void print(String s, Ansi.Attribute attribute, Ansi.FColor color) {
        printer.print(s, attribute, color, Ansi.BColor.NONE);
    }
    private synchronized void println(String s, Ansi.Attribute attribute, Ansi.FColor color) {
        printer.println(s, attribute, color, Ansi.BColor.NONE);
    }

    private enum IconName {
        /** Create new file */ A,
        /** Delete existing file */ D,
        /** Update existing file */ U,
        /** Rename existing file */ R,
        /** Share file */ S,
        /** Disable sharing of file */ T,
        /** Move file */ M,
        /** Reject file */ C
    }

    private static Map<Log.IconName, String> icons = CollectionAssembler.map(IconName.class, String.class,
                                                                             Pair.of(IconName.A, "✨"),
                                                                             Pair.of(IconName.D, "🗑"),
                                                                             Pair.of(IconName.U, "📝"),
                                                                             Pair.of(IconName.R, "🏷"),
                                                                             Pair.of(IconName.S, "🔗"),
                                                                             Pair.of(IconName.T, "🔒"),
                                                                             Pair.of(IconName.M, "🚀"),
                                                                             Pair.of(IconName.C, "🌡"));

    /**
     * Creates nes instance of {@code Log} and sets its output file to {@code logFile}, if one is provided.
     * @param logFile the file to use as output of logger
     */
    @SuppressWarnings("ConstantConditions")
    public Log(@Nullable File logFile) {
        this.logStream = Optionals.ofThrowable(() -> new PrintStream(new FileOutputStream(logFile, true)));
    }

    /**
     * Logs event of file being created.
     * @param user the user that created the file
     * @param file the file that was created. File should be already existing
     *             prior to this call
     * @param usage the value of how much size limit is used by user, in bytes
     */
    public void fileCreated(@NotNull String user, @NotNull File file, @NotNull Long usage) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.A, "❓"),
                "User " + user + " created file: " + file.getAbsolutePath(),
                String.format("File size: %.2fKB, user size usage: %.2f%%",
                              file.length() / 1024.0,
                              usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0)
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.GREEN);
    }

    /**
     * Logs event of file being deleted.
     * @param user the user that deleted the file
     * @param file the file that was deleted. File should be removed after this
     *             call
     * @param usage the value of how much size limit is used by user, in bytes
     */
    public void fileDeleted(@NotNull String user, @NotNull File file, @NotNull Long usage) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.D, "❓"),
                "User " + user + " deleted file: " + file.getAbsolutePath(),
                String.format("File size: %.2fKB, user size usage: %.2f%%",
                              file.length() / 1024.0,
                              usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0)
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.MAGENTA);
    }

    /**
     * Logs event of file content being updated.
     * @param user the user that updated the file
     * @param file the file that was updated. File should be already updated
     *             prior to this call
     * @param oldSize the old size of updated file, in bytes
     * @param usage the value of how much size limit is used by user, in bytes
     */
    public void fileUpdated(@NotNull String user, @NotNull File file, @NotNull Long oldSize, @NotNull Long usage) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.U, "❓"),
                "User " + user + " updated file: " + file.getAbsolutePath(),
                String.format("File size: %.2fKB => %.2fKB, user size usage: %.2f%%",
                              file.length() / 1024.0,
                              oldSize / 1024.0,
                              usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0)
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.CYAN);
    }

    /**
     * Logs event of file being renamed.
     * @param user the user that renamed the file
     * @param file the file that was renamed. File should be already renamed
     *             prior to this call
     * @param oldPath the old path of renamed file, absolute
     */
    public void fileRenamed(@NotNull String user, @NotNull File file, @NotNull String oldPath) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.R, "❓"),
                "User " + user + " renamed file: " + oldPath + " => " + file.getAbsolutePath(),
                ""
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.BLUE);
    }

    /**
     * Logs event of file being shared.
     * @param user the user that shared the file
     * @param file the file that was shared
     * @param receiver the user that file is shared to
     */
    public void fileShared(@NotNull String user, @NotNull File file, @NotNull String receiver) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.S, "❓"),
                "User " + user + " shared to user: " + receiver + " file: " + file.getAbsolutePath(),
                ""
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.GREEN);
    }

    /**
     * Logs event of file being unshared.
     * @param user the user that unshared the file
     * @param file the file that was unshared
     * @param receiver the user that file is being unshared from
     */
    public void fileUnshared(@NotNull String user, @NotNull File file, @NotNull String receiver) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.T, "❓"),
                "User " + user + " unshared to user: " + receiver + " file: " + file.getAbsolutePath(),
                ""
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.YELLOW);
    }

    /**
     * Logs event of file being moved.
     * @param user the user that moved the file
     * @param file the file that was moved
     * @param oldPath the old path of moved file, absolute
     */
    public void fileMoved(@NotNull String user, @NotNull File file, @NotNull String oldPath) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.M, "❓"),
                "User " + user + " moved file: " + oldPath + " => " + file.getAbsolutePath(),
                ""
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.BLUE);
    }

    /**
     * Logs event of file being rejected (cannot upload).
     * @param user the user that file was being reject from
     * @param file the file that is being rejected (can be empty, but path should contain abstract directions)
     */
    public void fileRejected(@NotNull String user, @NotNull File file) {
        Message message = new Message(
                getCurrentTime(),
                icons.getOrDefault(IconName.C, "❓"),
                "File: " + file.getAbsolutePath() + " from user: " + user + " was rejected",
                ""
        );
        debug(message, Ansi.Attribute.NONE, Ansi.FColor.RED);
    }

    /**
     * Saves {@code message} to log file.
     * @param message the message to save
     */
    public void archive(@Nullable Message message) {
        logStream.ifPresent(printStream -> printStream.println(message));
    }

    /**
     * Saves {@code message} to log file and displays it.
     * @param message the message to save and print
     */
    public void debug(@NotNull Message message) {
        logStream.ifPresent(printStream -> printStream.println(message));
        System.out.println(message);
    }

    /**
     * Saves {@code message} to log file and displays it in color.
     * @param message message the message to save and print
     * @param attribute {@link Ansi.Attribute}
     * @param color {@link Ansi.FColor}
     */
    public void debug(@NotNull Message message, Ansi.Attribute attribute, Ansi.FColor color) {
        logStream.ifPresent(printStream -> printStream.println(message));
        if (Configuration.PRINT_COLOR) {
            String[] splitMessage = message.toStringTuple();
            print(splitMessage[0], attribute, color);
            println(splitMessage[1], Ansi.Attribute.NONE, color);
            print(splitMessage[2], Ansi.Attribute.NONE, Ansi.FColor.NONE);
            print(splitMessage[3], attribute, Ansi.FColor.NONE);
            println(splitMessage[4], Ansi.Attribute.NONE, Ansi.FColor.NONE);
            println(splitMessage[5], Ansi.Attribute.NONE, Ansi.FColor.NONE);
            println(splitMessage[6], Ansi.Attribute.NONE, color);
        } else System.out.println(message);
    }

    /**
     * Closes {@link PrintStream} of log file.
     * <br><br>
     * <p> After this call all log messages will be only printed to console, and {@link #archive(Message)}
     * will do nothing.
     */
    @Override
    public void close() {
        logStream.ifPresent(PrintStream::close);
    }

    private static Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Represents loggable message.
     */
    public static final class Message {

        /**
         * Time of message creation.
         */
        @NotNull public final Long timestamp;
        @NotNull public final String date() {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            return dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                                                      TimeZone.getDefault().toZoneId()));
        }

        /**
         * Emoji icon to represent message.
         */
        @NotNull public final String icon;

        /**
         * Title of message.
         */
        @NotNull public final String title;

        /**
         * Detailed description of message.
         */
        @NotNull public final String description;

        /**
         * Creates message.
         * @param timestamp the time of message creation
         * @param icon the icon representing message
         * @param title the title of message
         * @param description the description of message
         */
        public Message(@NotNull Long timestamp,
                       @NotNull String icon,
                       @NotNull String title,
                       @NotNull String description) {
            this.timestamp = timestamp;
            this.icon = icon;
            this.title = title;
            this.description = description;
        }

        @Override
        @NotNull public String toString() {
            return date() + " -> {\n" +
                   icon + " " + title + ":\n" +
                   description + "\n}";
        }

        @NotNull public String[] toStringTuple() {
            return new String[] {
                    date(),
                    " -> {",
                    icon + " ",
                    title,
                    ":",
                    description,
                    "}"
            };
        }
    }
}
