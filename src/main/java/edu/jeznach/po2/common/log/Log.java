package edu.jeznach.po2.common.log;

import edu.jeznach.po2.configuration.Configuration;
import edu.jeznach.po2.util.CollectionAssembler;
import edu.jeznach.po2.util.Optionals;
import edu.jeznach.po2.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Used for logging messages to console and file, if provided.
 * <br><br>
 * <p>All public method of this class are thread-safe, as underlying {@link PrintStream PrintStreams} are.
 * <p>This implementation uses {@link PrintStream},
 * so it is important to call {@link #close()} to release resources when no longer needed.
 */
public class Log
        implements Closeable {

    private Optional<PrintStream> logStream;

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
    public Log(@Nullable String logFile) {
        this.logStream = Optionals.ofThrowable(() -> new PrintStream(logFile));
    }

    /**
     * Logs event of file being created.
     * @param user the user that created the file
     * @param file the file that was created. File should be already existing
     *             prior to this call
     * @param usage the value of how much size limit is used by user, in bytes
     */
    public void fileCreated(@NotNull String user, @NotNull File file, @NotNull Long usage) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.A, "❓") + " ";
        message += "User " + user + " created file: " + file.getAbsolutePath() + ";\n";
        message += String.format("File size: %.2fKB, user size usage: %.2f%%",
                                 file.length() / 1024.0,
                                 usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0) + ";\n}";
        System.out.println(message);
        archive(message);
    }

    /**
     * Logs event of file being deleted.
     * @param user the user that deleted the file
     * @param file the file that was deleted. File should be removed after this
     *             call
     * @param usage the value of how much size limit is used by user, in bytes
     */
    public void fileDeleted(@NotNull String user, @NotNull File file, @NotNull Long usage) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.D, "❓") + " ";
        message += "User " + user + " deleted file: " + file.getAbsolutePath() + ";\n";
        message += String.format("File size: %.2fKB, user size usage: %.2f%%",
                                 file.length() / 1024.0,
                                 usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0) + ";\n}";
        System.out.println(message);
        archive(message);
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
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.U, "❓") + " ";
        message += "User " + user + " updated file: " + file.getAbsolutePath() + ";\n";
        message += String.format("File size: %.2fKB => %.2fKB, user size usage: %.2f%%",
                                 file.length() / 1024.0,
                                 oldSize / 1024.0,
                                 usage / Configuration.SIZE_PER_USER$MB / 1024 / 1024.0) + ";\n}";
        System.out.println(message);
        archive(message);
    }

    /**
     * Logs event of file being renamed.
     * @param user the user that renamed the file
     * @param file the file that was renamed. File should be already renamed
     *             prior to this call
     * @param oldPath the old path of renamed file, absolute
     */
    public void fileRenamed(@NotNull String user, @NotNull File file, @NotNull String oldPath) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.R, "❓") + " ";
        message += "User " + user + " renamed file: " + oldPath + " => " + file.getAbsolutePath() + ";\n}";
        System.out.println(message);
        archive(message);
    }

    /**
     * Logs event of file being shared.
     * @param user the user that shared the file
     * @param file the file that was shared
     * @param receiver the user that file is shared to
     */
    public void fileShared(@NotNull String user, @NotNull File file, @NotNull String receiver) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.S, "❓") + " ";
        message += "User " + user + " shared to user: " + receiver + " file: " + file.getAbsolutePath() + ";\n}";
        System.out.println(message);
        archive(message);
    }

    /**
     * Logs event of file being unshared.
     * @param user the user that unshared the file
     * @param file the file that was unshared
     * @param receiver the user that file is being unshared from
     */
    public void fileUnshared(@NotNull String user, @NotNull File file, @NotNull String receiver) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.T, "❓") + " ";
        message += "User " + user + " unshared to user: " + receiver + " file: " + file.getAbsolutePath() + ";\n}";
        System.out.println(message);
        archive(message);}

    /**
     * Logs event of file being moved.
     * @param user the user that moved the file
     * @param file the file that was moved
     * @param oldPath the old path of moved file, absolute
     */
    public void fileMoved(@NotNull String user, @NotNull File file, @NotNull String oldPath) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.M, "❓") + " ";
        message += "User " + user + " moved file: " + oldPath + " => " + file.getAbsolutePath() + ";\n}";
        System.out.println(message);
        archive(message);}

    /**
     * Logs event of file being rejected (cannot upload).
     * @param user the user that file was being reject from
     * @param file the file that is being rejected (can be empty, but path should contain abstract directions)
     */
    public void fileRejected(@NotNull String user, @NotNull File file) {
        String message = getCurrentTime() + " -> {\n";
        message += icons.getOrDefault(IconName.C, "❓") + " ";
        message += "File: " + file.getAbsolutePath() + " from user: " + user + " was rejected;\n}";
        System.out.println(message);
        archive(message);}

    /**
     * Saves {@code message} to log file.
     * @param message the message to save
     */
    public void archive(@Nullable String message) {
        logStream.ifPresent(printStream -> printStream.println(message));
    }

    /**
     * Closes {@link PrintStream} of log file.
     * <br><br>
     * <p> After this call all log messages will be only printed to console, and {@link #archive(String)}
     * will do nothing.
     */
    @Override
    public void close() {
        logStream.ifPresent(PrintStream::close);
    }

    private static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
