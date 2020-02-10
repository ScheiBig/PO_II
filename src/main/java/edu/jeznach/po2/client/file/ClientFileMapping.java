package edu.jeznach.po2.client.file;

import edu.jeznach.po2.common.file.FileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientFileMapping extends FileMapping {

    private @Nullable List<String> receivers;
    public @Nullable List<String> getReceivers() { return this.receivers; }
    void setReceivers(@Nullable List<String> receivers) { this.receivers = receivers; }

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

    public ClientFileMapping() {
    }

    public ClientFileMapping(@NotNull String pathname,
                             @NotNull Long size_bytes,
                             @NotNull String checksum,
                             @NotNull Long modification_timestamp) {
        super(pathname, size_bytes, checksum, modification_timestamp);
    }

    public ClientFileMapping(@NotNull FileMapping fileMapping) {
        super(fileMapping);
    }
}
