package edu.jeznach.po2.server.file;

import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.SharedFileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DriveMapping {

    private @NotNull String name = "";
    public @NotNull String getName() { return this.name; }
    public void setName(@NotNull String name) { this.name = name; }

    private @NotNull String log_name = "";
    public @NotNull String getLog_name() { return this.log_name; }
    public void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    private @NotNull String drive_location = "";
    public @NotNull String getDrive_location() { return this.drive_location; }
    public void setDrive_location(@NotNull String drive_location) { this.drive_location = drive_location; }

    private @NotNull List<User> users = new ArrayList<>();
    public @NotNull List<User> getUsers() { return this.users; }
    public void setUsers(@NotNull List<User> users) { this.users = users; }

    DriveMapping() {  }

    public DriveMapping(InitParams params) {
        this.setDrive_location(new java.io.File(params.driveLocation).getAbsolutePath());
        this.setLog_name(params.logName);
    }

    public static class User {

        private @NotNull String username = "";
        public @NotNull String getUsername() { return this.username; }
        public void setUsername(@NotNull String username) { this.username = username; }

        private @Nullable List<FileMapping> files = new ArrayList<>();
        public @Nullable List<FileMapping> getFiles() { return this.files; }
        public void setFiles(@Nullable List<FileMapping> files) { this.files = files; }

        private @NotNull Long used_space_bytes = 0L;
        public @NotNull Long getUsed_space_bytes() { return this.used_space_bytes; }
        public void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

        private @Nullable List<SharedFileMapping> shared_files = new ArrayList<>();
        public @Nullable List<SharedFileMapping> getShared_files() { return this.shared_files; }
        public void setShared_files(@Nullable List<SharedFileMapping> shared_files) { this.shared_files = shared_files; }

        User() {  }

        public User(@NotNull String username) {
            this.username = username;
        }
    }

    public static class InitParams {

        public @NotNull final String driveLocation;
        public @NotNull final String logName;

        public InitParams(@NotNull String driveLocation, @NotNull String logName) {
            this.driveLocation = driveLocation;
            this.logName = logName;
        }
    }
}
