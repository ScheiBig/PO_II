package edu.jeznach.po2.server.file;

import org.jetbrains.annotations.NotNull;

/**
 * Represents mapping of a file stored in container directory.
 */
public class FileMapping extends edu.jeznach.po2.common.file.FileMapping {
    @Override
    protected void setPathname(@NotNull String pathname) {
        super.setPathname(pathname);
    }

    @Override
    protected void setSize_bytes(@NotNull Long size_bytes) {
        super.setSize_bytes(size_bytes);
    }

    @Override
    protected void setChecksum(@NotNull String checksum) {
        super.setChecksum(checksum);
    }

    @Override
    protected void setModification_timestamp(@NotNull Long modification_timestamp) {
        super.setModification_timestamp(modification_timestamp);
    }

    protected FileMapping() {
        super();
    }

    protected FileMapping(@NotNull String pathname,
                       @NotNull Long size_bytes,
                       @NotNull String checksum,
                       @NotNull Long modification_timestamp) {
        super(pathname, size_bytes, checksum, modification_timestamp);
    }

    protected FileMapping(edu.jeznach.po2.common.file.@NotNull FileMapping fileMapping) {
        super(fileMapping);
    }
}
