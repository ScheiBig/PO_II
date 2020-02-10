package edu.jeznach.po2.server.file;

import edu.jeznach.po2.common.file.FileMapper;
import edu.jeznach.po2.server.file.DriveMapping.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Used to collect {@link User} information from several {@link DriveFileMapper} objects.
 */
public class UserMappingCollector {

    private @NotNull List<DriveFileMapper> mappers;

    /**
     * Creates new {@code UserMappingCollector} from provided mappers.
     * @param mappers the list of {@link DriveFileMapper} objects to pull data from
     */
    public UserMappingCollector(@NotNull List<DriveFileMapper> mappers) {
        this.mappers = mappers;
    }

    /**
     * Merges all data about {@link User} in managed mappers.
     * @param username the username of user to pull data for
     * @return {@link User} object that contains all data mapped for user, {@code null} if user
     *         is not yet <i>"registered"</i> on server
     */
    public @Nullable User collectUserMapping(@NotNull String username) {
        Stream<User> userStream = mappers.stream()
                                         .map(FileMapper::getMapping)
                                         .map(DriveMapping::getUsers)
                                         .map(l -> l.stream()
                                                                 .filter(u -> u.getUsername().equals(username))
                                                                 .findFirst())
                                         .filter(Optional::isPresent)
                                         .map(Optional::get);
        if (userStream.count() <= 0) return null;
        return userStream.map(u -> new User[] { u })
                         .collect(UserMappingCollector.merging());
    }

    /**
     * Collects list of names of all <i>"registered"</i> users in managed mappers.
     * @return list of username strings
     */
    public @NotNull List<String> collectUserNames() {
        return mappers.stream()
                      .map(FileMapper::getMapping)
                      .map(DriveMapping::getUsers)
                      .flatMap(List::stream)
                      .map(User::getUsername)
                      .distinct()
                      .collect(Collectors.toList());
    }

    static Collector<User[], User[], User> merging() {
        return Collector.of(
                () -> new User[1],
                UserMappingCollector::mergeUsers,
                UserMappingCollector::mergeUsers,
                u -> u[0]
        );
    }

    static User[] mergeUsers(User[] to, User[] from) {
        if (to[0].getFiles() != null) {
            if (from[0].getFiles() != null) to[0].getFiles().addAll(from[0].getFiles());
        } else {
            if (from[0].getFiles() != null) to[0].setFiles(from[0].getFiles());
        }
        if (to[0].getShared_files() != null) {
            if (from[0].getShared_files() != null) to[0].getShared_files().addAll(from[0].getShared_files());
        } else {
            if (from[0].getShared_files() != null) to[0].setShared_files(from[0].getShared_files());
        }
        to[0].setUsed_space_bytes(to[0].getUsed_space_bytes() + from[0].getUsed_space_bytes());
        return to;
    }
}
