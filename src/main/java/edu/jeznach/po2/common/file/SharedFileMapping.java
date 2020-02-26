package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents mapping of a shared file stored in container directory.
 */
public class SharedFileMapping extends FileMapping implements Serializable {

    private static final long serialVersionUID = -7343413443959064322L;

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

    public SharedFileMapping(@NotNull FileMapping fileMapping,
                             @NotNull String owner) {
        super(fileMapping);
        this.owner = owner;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    @Override
    public String toString() {
        return "SharedFileMapping{" +
               "owner='" + owner + '\'' +
               super.toString() + " }";
    }
}
