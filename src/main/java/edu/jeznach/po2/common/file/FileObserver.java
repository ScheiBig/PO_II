package edu.jeznach.po2.common.file;

import com.diogonunes.jcdp.color.api.Ansi;
import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.ExtendedLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static edu.jeznach.po2.common.file.FileObserver.FileEvent.Type.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
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

    private final @NotNull Path rootPath;
    private final @NotNull WatchService watchService;
    private final @NotNull Map<@NotNull WatchKey, @NotNull Path> registeredKeys;
    private boolean trace = false;
    private final @NotNull ExtendedLinkedList<FileEvent> queuedEvents;
    private final @NotNull Log log;
    private boolean watching = false;

    /**
     * Creates new FileObserver for {@code rootDirectory} and all of its current and future
     * subdirectories.
     * <p>It is not specified how two instances watching same directory will behave, so its
     * advised to create new instance for every directory that should be observed, in a way that
     * they won't contain any duplicated subdirectories.
     * @param rootDirectory the root of directory tree to observe
     * @param log the {@link Log} object used for logging important events
     * @throws IOException if an I/O error occurs
     */
    public FileObserver(@NotNull Path rootDirectory,
                        @NotNull Log log) throws IOException {
        System.out.println(rootDirectory.toString());
        this.rootPath = rootDirectory;
        this.log = log;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.registeredKeys = new HashMap<>();
        registerAll(rootDirectory);
        this.trace = true;
        this.queuedEvents = new ExtendedLinkedList<>();
        this.watching = true;
    }
    /**
     * Creates new FileObserver without watched directory.
     * <p>No-directory FileObserver still can be used to manage queue of file events.
     * @param log the {@link Log} object used for logging important events
     */
    @SuppressWarnings("ConstantConditions")
    public FileObserver(@NotNull Log log) {
        this.rootPath = null;
        this.watchService = null;
        this.registeredKeys = null;
        this.log = log;
        this.queuedEvents = new ExtendedLinkedList<>();
        this.watching = false;
    }

    /**
     * Pops element from {@link FileEvent} queue. This method may block if no event
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
    public @NotNull FileEvent popEvent() throws InterruptedException {
        synchronized (queuedEvents) {
            if (queuedEvents.isEmpty())
                queuedEvents.wait();
            return queuedEvents.pop();
        }
    }

    /**
     * Adds new {@link FileEvent} to end of queue, with regard to its uniqueness.
     * @param filePath the relative (to storage root directory) path to file related to event
     * @param type the type of this event
     * @return created {@link FileEvent}, or the identical one that already exists
     */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull FileEvent addEvent(@NotNull Path filePath, @NotNull FileEvent.Type type) {
        FileEvent fileEvent = new FileEvent(filePath, type);
        synchronized (queuedEvents) {
            if (queuedEvents.contains(fileEvent)) return fileEvent;
            queuedEvents.add(fileEvent);
            queuedEvents.notify();
        }
        return fileEvent;
    }


    /**
     * Adds new {@link FileEvent} to end of queue, with regard to its uniqueness.
     * @param filePath the relative (to storage root directory) path to file related to event
     * @param type the type of this event
     * @param meta the additional metadata related to this event
     * @return created {@link FileEvent}, or the identical one that already exists
     */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull FileEvent addEvent(@NotNull Path filePath, @NotNull FileEvent.Type type, @NotNull String meta) {
        FileEvent fileEvent = new FileEvent(filePath, type, meta);
        synchronized (queuedEvents) {
            if (queuedEvents.contains(fileEvent)) return fileEvent;
            queuedEvents.add(fileEvent);
            queuedEvents.notify();
        }
        return fileEvent;
    }


    /**
     * Adds new {@link FileEvent} to front of queue.
     * @param filePath the relative (to storage root directory) path to file related to event
     * @param type the type of this event
     * @return created {@link FileEvent}, or the identical one that already exists
     */
    public @NotNull FileEvent pushEvent(@NotNull Path filePath, @NotNull FileEvent.Type type) {
        FileEvent fileEvent = new FileEvent(filePath, type);
        synchronized (queuedEvents) {
            queuedEvents.push(fileEvent);
            queuedEvents.notify();
        }
        return fileEvent;
    }

    /**
     * Main producer loop. In each iteration pulls all file system events and appends them
     * to {@link #queuedEvents}, removing/replacing existing ones, if handling those should
     * no longer be pursued. This method should never be called directly, rather {@link Thread#start()}
     * be called instead.
     */
    @Override
    public void run() {
        if (watching) {
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    return;
                }

                Path eventDir = registeredKeys.get(key);
                if (eventDir == null) {
                    log.debug(new Log.Message(
                            System.currentTimeMillis(),
                            "🔕",
                            "Directory unregistered!",
                            "Could not find directory associated with retrieved WatchKey"
                    ), Ansi.Attribute.NONE, Ansi.FColor.RED);
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        log.debug(new Log.Message(
                                System.currentTimeMillis(),
                                "📚",
                                "Event overflow!",
                                "FileEvents may have been lost or discarded due to slow processing"
                        ), Ansi.Attribute.NONE, Ansi.FColor.RED);
                        continue;
                    }

                    WatchEvent<Path> entryEvent = cast(event);
                    Path name = entryEvent.context();
                    if (name.toString().endsWith(Configuration.TEMP_EXTENSION)) continue;
                    Path child = eventDir.resolve(name);
                    Path file = rootPath.relativize(child);
                    check: {
                        if (ENTRY_CREATE.equals(kind)) {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                try {
                                    registerAll(child);
                                } catch (IOException e) {
                                    log.exception(e);
                                }
                            } else {
                                FileEvent fileEvent = new FileEvent(file, Create_Node);
                                synchronized (queuedEvents) {
                                    queuedEvents.add(fileEvent);
                                    queuedEvents.notify();
                                }
                            }
                        } else if (ENTRY_MODIFY.equals(kind)) {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) break check;
                            FileEvent fileEvent = new FileEvent(file, Update_Node);
                            synchronized (queuedEvents) {
                                boolean shouldAdd = true;
                                for (FileEvent queuedEvent : queuedEvents) {
                                    if (queuedEvent.filePath.equals(file) &&
                                        (queuedEvent.eventType.equals(Create_Node) ||
                                         queuedEvent.eventType.equals(Update_Node))) {
                                        shouldAdd = false;
                                        break;
                                    }
                                }
                                if (shouldAdd) {
                                    queuedEvents.add(fileEvent);
                                    queuedEvents.notify();
                                }
                            }
                        } else if (ENTRY_DELETE.equals(kind)) {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) break check;
                            FileEvent fileEvent = new FileEvent(file, Delete_Node);
                            synchronized (queuedEvents) {
                                queuedEvents.removeIf(
                                        queuedEvent -> queuedEvent.filePath.equals(file) &&
                                                       (queuedEvent.eventType.equals(Create_Node) ||
                                                        queuedEvent.eventType.equals(Update_Node))
                                );
                                queuedEvents.add(fileEvent);
                                queuedEvents.notify();
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) registeredKeys.remove(key);
                    System.err.println(queuedEvents.toString());
                }
            }
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

    /**
     * Represents event related to file modification (CUD)
     */
    public static final class FileEvent {

        /**
         * Relative (to watched root directory) path to file related to event.
         */
        public final @NotNull Path filePath;
        /**
         * The type of this event.
         * @see Type
         */
        public final @NotNull Type eventType;

        private @Nullable String meta;

        /**
         * Additional metadata related to this event
         * @return additional metadata related to this event
         */
        public final @Nullable String meta() { return meta; }

        /**
         * Creates new {@code FileEvent} identifier
         * @param filePath the relative (to watched root directory) path to file related to event
         * @param eventType the type of this event
         */
        public FileEvent(@NotNull Path filePath,
                         @NotNull Type eventType) {
            this.filePath = filePath;
            this.eventType = eventType;
        }

        /**
         * Creates new {@code FileEvent} identifier with additional metadata
         * @param filePath the relative (to watched root directory) path to file related to event
         * @param eventType the type of this event
         * @param meta the additional metadata related to this event
         */
        public FileEvent(@NotNull Path filePath,
                         @NotNull Type eventType,
                         @NotNull String meta) {
            this.filePath = filePath;
            this.eventType = eventType;
            this.meta = meta;
        }

        /**
         * Represents type of event. Event type may represent single event (file deleted,
         * multiple merged updates), or double-merged one (file created and updated as
         *
         * file created with content)
         */
        public enum Type {
            /** New File was created (and possibly already updated) */
            Create_Node,
            /** Existing File was updated (on client, and possibly multiple times) */
            Update_Node,
            /** Existing File was deleted (and possibly created/updated before) */
            Delete_Node,
            /** Share file to receiver */
            Assign_Node,
            /** Reject sharing file to receiver */
            Refuse_Node,
            /** New file contents must be downloaded */
            Devour_Node,
            /** Existing file must be updated */
            Revise_Node,
            /** Existing file must be deleted */
            Remove_Node,
            /** Existing file was shared to user */
            Couple_Node,
            /** Existing file is no longer shared */
            Unlink_Node
        }

        @Override
        public String toString() {
            return eventType + ": " + filePath.toString() +
                   (meta != null ?
                           (" (" + meta + ")") :
                           "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileEvent event = (FileEvent) o;
            return filePath.equals(event.filePath) &&
                   eventType == event.eventType &&
                   Objects.equals(meta, event.meta);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filePath, eventType, meta);
        }
    }
}
