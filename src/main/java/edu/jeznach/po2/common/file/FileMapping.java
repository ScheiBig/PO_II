package edu.jeznach.po2.common.file;

import edu.jeznach.po2.common.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Represents mapping of a file stored in container directory.
 */
public class FileMapping {

    private @NotNull String pathname = "";
    /**
     * @return the full path to this mapped file, relative to directory
     *         used as container for mapped files
     */
    public @NotNull String getPathname() { return this.pathname; }
    protected void setPathname(@NotNull String pathname) { this.pathname = pathname; }

    private @NotNull Long size_bytes = 0L;
    /**
     * @return the size of this mapped file, in bytes
     */
    public @NotNull Long getSize_bytes() { return this.size_bytes; }
    protected void setSize_bytes(@NotNull Long size_bytes) { this.size_bytes = size_bytes; }

    private @NotNull String checksum = "";
    /**
     * @return the checksum calculated for this mapped file contents
     * @see Configuration#CHECKSUM_ALGORITHM
     */
    public @NotNull String getChecksum() { return this.checksum; }
    protected void setChecksum(@NotNull String checksum) { this.checksum = checksum; }

    private @NotNull Long modification_timestamp = 0L;
    /**
     * @return the timestamp of last modification of this mapped file
     * @see File#lastModified()
     */
    public @NotNull Long getModification_timestamp() { return this.modification_timestamp; }
    protected void setModification_timestamp(@NotNull Long modification_timestamp) { this.modification_timestamp = modification_timestamp; }

    protected FileMapping() { }

    protected FileMapping(@NotNull String pathname,
                       @NotNull Long size_bytes,
                       @NotNull String checksum,
                       @NotNull Long modification_timestamp) {
        this.pathname = pathname;
        this.size_bytes = size_bytes;
        this.checksum = checksum;
        this.modification_timestamp = modification_timestamp;
    }

    protected FileMapping(@NotNull FileMapping fileMapping) {
        this(fileMapping.getPathname(),
             fileMapping.getSize_bytes(),
             fileMapping.getChecksum(),
             fileMapping.getModification_timestamp());
    }
}
