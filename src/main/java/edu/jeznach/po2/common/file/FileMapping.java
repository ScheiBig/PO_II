package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

public class FileMapping {

    private @NotNull String pathname = "";
    public @NotNull String getPathname() { return this.pathname; }
    public void setPathname(@NotNull String pathname) { this.pathname = pathname; }

    private @NotNull Long size_bytes = 0L;
    public @NotNull Long getSize_bytes() { return this.size_bytes; }
    public void setSize_bytes(@NotNull Long size_bytes) { this.size_bytes = size_bytes; }

    private @NotNull String checksum = "";
    public @NotNull String getChecksum() { return this.checksum; }
    public void setChecksum(@NotNull String checksum) { this.checksum = checksum; }

    private @NotNull Long modification_timestamp = 0L;
    public @NotNull Long getModification_timestamp() { return this.modification_timestamp; }
    public void setModification_timestamp(@NotNull Long modification_timestamp) { this.modification_timestamp = modification_timestamp; }

    public FileMapping() { }

    public FileMapping(@NotNull String pathname,
                @NotNull Long size_bytes,
                @NotNull String checksum, @NotNull Long modification_timestamp) {
        this.pathname = pathname;
        this.size_bytes = size_bytes;
        this.checksum = checksum;
        this.modification_timestamp = modification_timestamp;
    }
}
