package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

public class SharedFileMapping extends FileMapping {

    private @NotNull String owner = "";
    public @NotNull String getOwner() { return this.owner; }
    public void setOwner(@NotNull String owner) { this.owner = owner; }

    public SharedFileMapping() { super(); }

    public SharedFileMapping(@NotNull String pathname,
                             @NotNull Long size_bytes,
                             @NotNull String checksum,
                             @NotNull Long modification_timestamp,
                             @NotNull String owner) {
        super(pathname, size_bytes, checksum, modification_timestamp);
        this.owner = owner;
    }

    public SharedFileMapping(@NotNull FileMapping fileMapping,
                             @NotNull String owner) {
        this(fileMapping.getPathname(),
             fileMapping.getSize_bytes(),
             fileMapping.getChecksum(),
             fileMapping.getModification_timestamp(),
             owner);
    }
}
