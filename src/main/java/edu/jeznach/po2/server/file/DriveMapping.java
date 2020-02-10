package edu.jeznach.po2.server.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DriveMapping {

    private @NotNull String name = "";
    public @NotNull String getName() { return this.name; }
    protected void setName(@NotNull String name) { this.name = name; }

    private @NotNull String log_name = "";
    public @NotNull String getLog_name() { return this.log_name; }
    protected void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    private @NotNull String drive_location = "";
    public @NotNull String getDrive_location() { return this.drive_location; }
    protected void setDrive_location(@NotNull String drive_location) { this.drive_location = drive_location; }

    private @NotNull List<User> users = new ArrayList<>();
    public @NotNull List<User> getUsers() { return this.users; }
    protected void setUsers(@NotNull List<User> users) { this.users = users; }

    DriveMapping() {  }

    protected DriveMapping(InitParams params) {
        this.setName(params.driveLocation.getName());
        this.setLog_name(params.logName);
        this.setDrive_location(params.driveLocation.getAbsolutePath());
    }

    public static class User {

        private @NotNull String username = "";
        public @NotNull String getUsername() { return this.username; }
        protected void setUsername(@NotNull String username) { this.username = username; }

        private @Nullable List<FileMapping> files = new ArrayList<>();
        public @Nullable List<FileMapping> getFiles() { return this.files; }
        protected void setFiles(@Nullable List<FileMapping> files) { this.files = files; }

        private @NotNull Long used_space_bytes = 0L;
        public @NotNull Long getUsed_space_bytes() { return this.used_space_bytes; }
        protected void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

        private @Nullable List<SharedFileMapping> shared_files;
        public @Nullable List<SharedFileMapping> getShared_files() { return this.shared_files; }
        protected void setShared_files(@Nullable List<SharedFileMapping> shared_files) { this.shared_files = shared_files; }

        User() {  }

        protected User(@NotNull String username) {
            this.username = username;
        }
    }

    /**
     * Represents parameters injected to {@link DriveMapping} constructor
     */
    protected static class InitParams {

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
         * @param driveLocation Localisation of mapped drive. {@link File#getName() File#name} will
         *                      represent {@link DriveMapping#name DriveMapping#name},
         *                      {@link File#getAbsolutePath() File#absolutePath} will represent
         *                      {@link DriveMapping#drive_location DriveMapping#drive_location}
         * @param logName Name that will be used for log file
         */
        protected InitParams(@NotNull File driveLocation, @NotNull String logName) {
            this.driveLocation = driveLocation;
            this.logName = logName;
        }
    }
}
