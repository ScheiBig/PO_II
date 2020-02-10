package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

public class SharedFileMapping extends FileMapping {

    private @NotNull String owner = "";
    public @NotNull String getOwner() { return this.owner; }
    protected void setOwner(@NotNull String owner) { this.owner = owner; }

    protected SharedFileMapping() { super(); }

    protected SharedFileMapping(@NotNull String pathname,
                             @NotNull Long size_bytes,
                             @NotNull String checksum,
                             @NotNull Long modification_timestamp,
                             @NotNull String owner) {
        super(pathname, size_bytes, checksum, modification_timestamp);
        this.owner = owner;
    }

    protected SharedFileMapping(@NotNull FileMapping fileMapping,
                             @NotNull String owner) {
        super(fileMapping);
        this.owner = owner;
    }
}
