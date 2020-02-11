package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A simple observer that watches directory for changes of its files. All current and
 * future directories are recursively added to observed set. Events of file being created,
 * updated or deleted are queued in a way, that only last update event (or delete which
 * cancels all updates) is stashed in queue of available {@link FileEvent FileEvents}.
 * <br><br>
 * <p><i>Observation</i> of specified directory is done in infinite loop, so it is advised
 * to provide dedicated thread. Queued events are shifted in thread-safe fashion, but code
 * block that acquires event should immediately take action based on its type.
 */
public class FileObserver extends Thread {

    private final @NotNull WatchService watchService;
    private final @NotNull Map<@NotNull WatchKey, @NotNull Path> registeredKeys;
    @SuppressWarnings("UnusedAssignment") private boolean trace = false;
    private final @NotNull LinkedList<FileEvent> queuedEvents;

    /**
     * Creates new FileObserver for {@code rootDirectory} and all of its current and future
     * subdirectories.
     * <p>It is not specified how two instances watching same directory will behave, so its
     * advised to create new instance for every directory that should be observed, in a way that
     * they won't contain any duplicated subdirectories.
     * @param rootDirectory the root of directory tree to observe
     * @throws IOException if an I/O error occurs <i>(very thoughtful of JDK developers
     *                     to not specify what error)</i>
     */
    public FileObserver(Path rootDirectory) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.registeredKeys = new HashMap<>();
        registerAll(rootDirectory);
        this.trace = true;
        this.queuedEvents = new LinkedList<>();
    }

    /**
     * Shifts element from {@link FileEvent} queue. This method may block if no event
     * is currently available.
     * <br><br>
     * <p>Thread that makes this call is <b>required</b> to take action that handles returned
     * event, as event cannot be returned to queue.
     * @return first event from queue
     * @throws InterruptedException if any thread interrupted the current thread before
     *                              or while the current thread was waiting for a notification.
     *                              The <i>interrupted status</i> of the current thread is
     *                              cleared when this exception is thrown
     */
    public @NotNull FileEvent shiftEvent() throws InterruptedException {
        synchronized (queuedEvents) {
            if (queuedEvents.isEmpty()) wait();
            else {
                return queuedEvents.poll();
            }
            throw new InterruptedException("Moved out of queue scope");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * @return {@code true} - new directory, {@code false} update directory to new,
     *         {@code null} - update directory to same
     */
    @SuppressWarnings("UnusedReturnValue")
    private @Nullable Boolean registerDirectory(final @NotNull Path directory) throws IOException {
        Boolean ret;
        WatchKey key = directory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        Path existing = registeredKeys.get(key);
        if (trace) {
            if (existing == null) {
                ret = true;
            } else {
                if (!directory.equals(existing)) ret = false;
                else ret = null;
            }
        } else ret = true;
        registeredKeys.put(key, directory);
        return ret;
    }

    private void registerAll(final @NotNull Path rootPath) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void run() {

    }

    public static final class FileEvent {

    }
}
