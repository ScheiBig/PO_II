package edu.jeznach.po2.server.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents mapping of a server drive file structure.
 * <p>Proper structure is:
 * <blockquote><pre><code>
 * 🖥 server - location of server structure
 * ├ 📁 <b>drive</b>> - location of mapped drive structure
 * │ ├ 📁 user - directory containing /user/ files
 * │ │ └ ⸬
 * │ ├ ⸬
 * │ ├ 📄 mapping - file that contains mapping of this structure
 * │ └ 📄 log - file that contains messages logged by this drive
 * └ ⸬
 * </code></pre></blockquote>
 */
public class DriveMapping {

    private @NotNull String name = "";
    /**
     * @return the name of this drive
     */
    public @NotNull String getName() { return this.name; }
    protected void setName(@NotNull String name) { this.name = name; }

    private @NotNull String log_name = "";
    /**
     * @return the name of file that is used for logging
     */
    public @NotNull String getLog_name() { return this.log_name; }
    protected void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    private @NotNull String drive_location = "";
    /**
     * @return the absolute path to drive structure
     */
    public @NotNull String getDrive_location() { return this.drive_location; }
    protected void setDrive_location(@NotNull String drive_location) { this.drive_location = drive_location; }

    private @NotNull List<User> users = new ArrayList<>();
    /**
     * @return the list of mappings of users that store files on this drive
     */
    public @NotNull List<User> getUsers() { return this.users; }
    protected void setUsers(@NotNull List<User> users) { this.users = users; }

    protected DriveMapping() {  }

    protected DriveMapping(InitParams params) {
        this.setName(params.driveLocation.getName());
        this.setLog_name(params.logName);
        this.setDrive_location(params.driveLocation.getAbsolutePath());
    }

    /**
     * Represents user that stores files on server drive.
     */
    public static class User {

        private @NotNull String username = "";
        /**
         * @return the username of this user
         */
        public @NotNull String getUsername() { return this.username; }
        protected void setUsername(@NotNull String username) { this.username = username; }

        private @Nullable List<FileMapping> files = new ArrayList<>();
        /**
         * @return the list of mappings of files, that client stores on this server drive
         */
        public @Nullable List<FileMapping> getFiles() { return this.files; }
        protected void setFiles(@Nullable List<FileMapping> files) { this.files = files; }

        private @NotNull Long used_space_bytes = 0L;
        /**
         * @return the current storage space occupied by client files on this drive, in bytes
         */
        public @NotNull Long getUsed_space_bytes() { return this.used_space_bytes; }
        protected void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

        private @Nullable List<SharedFileMapping> shared_files;
        /**
         * @return the list of mappings of files, that were shared to client
         */
        public @Nullable List<SharedFileMapping> getShared_files() { return this.shared_files; }
        protected void setShared_files(@Nullable List<SharedFileMapping> shared_files) { this.shared_files = shared_files; }

        protected User() {  }

        protected User(@NotNull String username) {
            this.username = username;
        }
    }

    /**
     * Represents parameters injected to {@link DriveMapping} constructor
     */
    public static class InitParams {

        /**
         *  Localisation of mapped drive. {@link File#getName() File#name} will represent
         *  {@link DriveMapping#name DriveMapping#name}, {@link File#getAbsolutePath() File#absolutePath}
         *  will represent {@link DriveMapping#drive_location DriveMapping#drive_location}.
         */
        protected @NotNull final File driveLocation;
        /**
         * Name that will be used for log file.
         */
        protected @NotNull final String logName;

        /**
         * @param driveLocation the localisation of mapped drive. {@link File#getName() File#name} will
         *                      represent {@link DriveMapping#name DriveMapping#name},
         *                      {@link File#getAbsolutePath() File#absolutePath} will represent
         *                      {@link DriveMapping#drive_location DriveMapping#drive_location}
         * @param logName the name that will be used for log file
         */
        public InitParams(@NotNull File driveLocation, @NotNull String logName) {
            this.driveLocation = driveLocation;
            this.logName = logName;
        }
    }
}
