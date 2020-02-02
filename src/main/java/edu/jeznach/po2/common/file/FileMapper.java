package edu.jeznach.po2.common.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.Closeable;
import java.io.File;
import java.io.Reader;
import java.io.Writer;

/**
 * Used to read, save, create and manage indexing and mapping of file structure.
 * <p>Allows to <i>attach</i>, <i>detach</i> and <i>update</i> files.
 * <i>Attach</i> represents that file was created, <i>detach</i> represents
 * that file was deleted and <i>update</i> represents that file contents was
 * changed. Renaming or moving file should be handled as <i>detaching</i> file with old name
 * and <i>attaching</i> once again with new name, as only modification and no
 * creation date is mapped
 * <br><br>
 * <p>Files are mapped as $filesStorageDirectory$(/$owner$)/$file$, where:
 * <p>- $filesStorageDirectory$ is directory used for storage,
 * <p>- $owner$ is owner of file in multi-ser storage (server),
 * <p>- $file$ is full relative path to file.
 * <p>i.e.: /storage/user1/docs/file.txt.
 * @param <M> the type holding indexed mapping, using to manage at runtime and
 *            as structure {@link Yaml#dump(Object, Writer) dumped} to and
 *            {@link Yaml#load(Reader) loaded} from file
 */
public interface FileMapper<M> extends Closeable {

    /**
     * Creates new mapping of file structure and saves its results in file.
     * <br><br>
     * <p>If file permissions, especially writing is permitted, then this method
     * will always {@link Yaml#dump(Object, Writer) dump} its result to specified
     * path, so it is important to provide file that can be overridden and/or losing
     * of its current contents is not important anymore.
     * @param file the file that is to be used to save mapping,
     *                 if {@code null} then mapping will not be saved to file (runtime only)
     * @return object containing created mapping
     */
    @NotNull M createStructure(@Nullable File file);

    /**
     * Loads mapping from file.
     * <br><br>
     * <p>It would be advised to call this method prior to {@link #createStructure(File)},
     * and perform some actions based on difference of those two calls, as this would
     * suggest that modifications to file structure were performed in between execution
     * of application
     * @param file the file that is to be used to load mapping
     * @return object containing loaded mapping
     */
    @Nullable M loadStructure(@NotNull File file);

    /**
     * Attaches file to mapping
     * @param file the {@link File} to be attached to mapping. File should be already existing
     *             prior to this call
     * @return {@code true} if file was attached, {@code false} if this file is already attached
     */
    boolean attachFile(@NotNull File file);

    /**
     * Detaches file from mapping
     * @param file the {@link File} to be attached to mapping. File should be removed after this
     *             call
     * @return {@code true} if file was detached, {@code false} if this file is already detached
     */
    boolean detachFile(@NotNull File file);

    /**
     * Updates file in mapping
     * @param file the {@link File} to be updated in mapping. File should be already updated
     *             prior to this call
     * @return {@code true} if file was updated, {@code false} if no updates is necessary
     */
    boolean updateFile(@NotNull File file);

    /**
     * Shares file to {@code receiver}
     * @param file the {@link File} to be shared. File should be already existing prior to this call
     * @param receiver the user that file should be shared to
     * @return {@code true} if file was shared, {@code false} if file is already shared,
     *         {@code null} if sharing functionality is not supported
     */
    @Nullable Boolean shareFile(@NotNull File file, @NotNull String receiver);
}
