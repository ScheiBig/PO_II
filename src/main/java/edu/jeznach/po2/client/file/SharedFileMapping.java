package edu.jeznach.po2.client.file;

import edu.jeznach.po2.common.file.FileMapping;
import org.jetbrains.annotations.NotNull;

/**
 * Represents mapping of a shared file stored in container directory.
 */
public class SharedFileMapping extends edu.jeznach.po2.common.file.SharedFileMapping {

    @Override
    public void setOwner(@NotNull String owner) {
        super.setOwner(owner);
    }

    protected SharedFileMapping() {
        super();
    }

    protected SharedFileMapping(@NotNull String pathname,
                             @NotNull Long size_bytes,
                             @NotNull String checksum,
                             @NotNull Long modification_timestamp,
                             @NotNull String owner) {
        super(pathname, size_bytes, checksum, modification_timestamp, owner);
    }

    protected SharedFileMapping(@NotNull FileMapping fileMapping, @NotNull String owner) {
        super(fileMapping, owner);
    }

    @Override
    public void setPathname(@NotNull String pathname) {
        super.setPathname(pathname);
    }

    @Override
    public void setSize_bytes(@NotNull Long size_bytes) {
        super.setSize_bytes(size_bytes);
    }

    @Override
    public void setChecksum(@NotNull String checksum) {
        super.setChecksum(checksum);
    }

    @Override
    public void setModification_timestamp(@NotNull Long modification_timestamp) {
        super.setModification_timestamp(modification_timestamp);
    }
}
