package edu.jeznach.po2.server.file;

import edu.jeznach.po2.common.file.FileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DriveMapping {

    @NotNull private String name = "";
    @NotNull public String getName() { return this.name; }
    public void setName(@NotNull String name) { this.name = name; }

    @NotNull private String log_name = "";
    @NotNull public String getLog_name() { return this.log_name; }
    public void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    @NotNull private String drive_location = "";
    @NotNull public String getDrive_location() { return this.drive_location; }
    public void setDrive_location(@NotNull String drive_location) { this.drive_location = drive_location; }

    @NotNull private List<User> users = new ArrayList<>();
    @NotNull public List<User> getUsers() { return this.users; }
    public void setUsers(@NotNull List<User> users) { this.users = users; }

    DriveMapping() {  }

    public DriveMapping(InitParams params) {
        this.setDrive_location(new java.io.File(params.driveLocation).getAbsolutePath());
        this.setLog_name(params.logName);
    }

    public static class User {

        @NotNull private String username = "";
        @NotNull public String getUsername() { return this.username; }
        public void setUsername(@NotNull String username) { this.username = username; }

        @Nullable private List<FileMapping> files = new ArrayList<>();
        @Nullable public List<FileMapping> getFiles() { return this.files; }
        public void setFiles(@Nullable List<FileMapping> files) { this.files = files; }

        @NotNull private Long used_space_bytes = 0L;
        @NotNull public Long getUsed_space_bytes() { return this.used_space_bytes; }
        public void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

        @Nullable private List<FileMapping> shared_files = new ArrayList<>();
        @Nullable public List<FileMapping> getShared_files() { return this.shared_files; }
        public void setShared_files(@Nullable List<FileMapping> shared_files) { this.shared_files = shared_files; }

        User() {  }

        public User(@NotNull String username) {
            this.username = username;
        }
    }

    public static class InitParams {

        @NotNull public final String driveLocation;
        @NotNull public final String logName;

        public InitParams(@NotNull String driveLocation, @NotNull String logName) {
            this.driveLocation = driveLocation;
            this.logName = logName;
        }
    }
}
