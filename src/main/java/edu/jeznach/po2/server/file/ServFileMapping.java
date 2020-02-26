package edu.jeznach.po2.server.file;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"serial", "SpellCheckingInspection"})
public class ServFileMapping
        extends edu.jeznach.po2.common.file.FileMapping {
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

    private @NotNull String drive;
    public @NotNull String getDrive() { return this.drive; }
    public void setDrive(@NotNull String drive) { this.drive = drive; }

    protected ServFileMapping() {
        super();
    }

    protected ServFileMapping(@NotNull String pathname,
                              @NotNull Long size_bytes,
                              @NotNull String checksum,
                              @NotNull Long modification_timestamp,
                              @NotNull String drive) {
        super(pathname, size_bytes, checksum, modification_timestamp);
        this.drive = drive;
    }

    protected ServFileMapping(edu.jeznach.po2.common.file.@NotNull FileMapping fileMapping,
                              @NotNull String drive) {
        super(fileMapping);
        this.drive = drive;
    }
}
