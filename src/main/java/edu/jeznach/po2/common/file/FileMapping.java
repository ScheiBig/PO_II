package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

public class FileMapping {

    @NotNull private String pathname = "";
    @NotNull public String getPathname() { return this.pathname; }
    public void setPathname(@NotNull String pathname) { this.pathname = pathname; }

    @NotNull private Long size_bytes = 0L;
    @NotNull public Long getSize_bytes() { return this.size_bytes; }
    public void setSize_bytes(@NotNull Long size_bytes) { this.size_bytes = size_bytes; }

    @NotNull private String checksum = "";
    @NotNull public String getChecksum() { return this.checksum; }
    public void setChecksum(@NotNull String checksum) { this.checksum = checksum; }

    @NotNull private Long modification_timestamp = 0L;
    @NotNull public Long getModification_timestamp() { return this.modification_timestamp; }
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
