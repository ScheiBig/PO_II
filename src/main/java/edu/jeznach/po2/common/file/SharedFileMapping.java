package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

/**
 * Represents mapping of a shared file stored in container directory.
 */
public class SharedFileMapping extends FileMapping {

    private @NotNull String owner = "";
    /**
     * @return the username of owner of this shared file mapping
     */
    public @NotNull String getOwner() { return this.owner; }
    public void setOwner(@NotNull String owner) { this.owner = owner; }

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
