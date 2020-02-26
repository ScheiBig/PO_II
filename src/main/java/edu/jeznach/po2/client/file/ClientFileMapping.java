package edu.jeznach.po2.client.file;

import edu.jeznach.po2.common.file.FileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents mapping of a shareable file stored in container directory.
 */
@SuppressWarnings("serial")
public class ClientFileMapping extends FileMapping {

    private @Nullable List<String> receivers;
    /**
     * @return the list of users names that this mapped file is shared to
     */
    public @Nullable List<String> getReceivers() { return this.receivers; }
    public void setReceivers(@Nullable List<String> receivers) { this.receivers = receivers; }

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

    protected ClientFileMapping() {  }

    protected ClientFileMapping(@NotNull String pathname,
                                @NotNull Long size_bytes,
                                @NotNull String checksum,
                                @NotNull Long modification_timestamp) {
        super(pathname, size_bytes, checksum, modification_timestamp);
    }

    public ClientFileMapping(@NotNull FileMapping fileMapping) {
        super(fileMapping);
    }
}
