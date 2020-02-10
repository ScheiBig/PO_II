package edu.jeznach.po2.client.file;

import edu.jeznach.po2.common.file.FileMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientFileMapping extends FileMapping {

    private @Nullable List<String> receivers;
    public @Nullable List<String> getReceivers() { return this.receivers; }
    public void setReceivers(@Nullable List<String> receivers) { this.receivers = receivers; }
    
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
