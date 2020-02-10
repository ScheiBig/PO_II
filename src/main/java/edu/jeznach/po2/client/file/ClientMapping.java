package edu.jeznach.po2.client.file;

import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.SharedFileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class ClientMapping {

    private @NotNull String name = "";
    public @NotNull String getName() { return this.name; }
    public void setName(@NotNull String name) { this.name = name; }

    private @NotNull String log_name = "";
    public @NotNull String getLog_name() { return this.log_name; }
    public void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    private @NotNull String client_location = "";
    public @NotNull String getClient_location() { return this.client_location; }
    public void setClient_location(@NotNull String client_location) { this.client_location = client_location; }

    private @NotNull Long used_space_bytes = 0L;
    public @NotNull Long getUsed_space_bytes() { return this.used_space_bytes; }
    public void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

    private @Nullable List<ClientFileMapping> files;
    public @Nullable List<ClientFileMapping> getFiles() { return this.files; }
    public void setFiles(@Nullable List<ClientFileMapping> files) { this.files = files; }

    private @Nullable List<FileMapping> cancelled_files;
    public @Nullable List<FileMapping> getCancelled_files() { return this.cancelled_files; }
    public void setCancelled_files(@Nullable List<FileMapping> cancelled_files) { this.cancelled_files = cancelled_files; }

    private @Nullable List<SharedFileMapping> shared_files;
    public @Nullable List<SharedFileMapping> getShared_files() { return this.shared_files; }
    public void setShared_files(@Nullable List<SharedFileMapping> shared_files) { this.shared_files = shared_files; }

    ClientMapping() {  }

    public ClientMapping(@NotNull InitParams params) {
        this.setName(params.clientName);
        this.setLog_name(params.logName);
        this.setClient_location(params.clientLocation.getAbsolutePath());
    }

    public static class InitParams {

        public @NotNull final String clientName;

        public @NotNull final String logName;

        public @NotNull final File clientLocation;

        public InitParams(@NotNull String clientName,
                          @NotNull File clientLocation,
                          @NotNull String logName) {
            this.clientName = clientName;
            this.clientLocation = clientLocation;
            this.logName = logName;
        }
    }

    public enum Directories {
        /** Name of directory to hold user owned files */ files,
        /** Name of directory to hold user files that got cancelled from upload */ cancel,
        /** Name of directory to hold files shared to user */ shared
    }
}
