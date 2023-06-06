package edu.jeznach.po2.common.file;

import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used to manage indexing and mapping of file structure.
 * <p>Allows to <i>attach</i>, <i>detach</i> and <i>update</i> files.
 * <i>Attach</i> represents that file was created, <i>detach</i> represents
 * that file was deleted and <i>update</i> represents that file contents was
 * changed. Renaming or moving file should be handled as <i>detaching</i> file with old name
 * and <i>attaching</i> once again with new name, as only modification and no
 * creation date is mapped
 * <br><br>
 * <p>Files are mapped as $filesStorageDirectory$(/$owner$)/$file$, where:
 * <p>- $filesStorageDirectory$ is directory used for storage,
 * <p>- $owner$ is owner node (user or type directory) of file,
 * <p>- $file$ is full relative path to file.
 * <p>i.e.: /storage/user1/docs/file.txt.
 * @param <M> the type holding indexed mapping, using to manage at runtime and
 *            as structure {@link Yaml#dump(Object, Writer) dumped} to and
 *            {@link Yaml#load(Reader) loaded} from file
 */
public abstract class FileMapper<M> {

    /**
     * Contains factory methods for creating/loading file mapping.
     * <br><br>
     * <p>All {@link FileMapper} implementations should hide this field with
     * instance of its own implementations
     */
    @SuppressWarnings({"rawtypes", "NotNullFieldNotInitialized"})
    protected static @NotNull FileMappingProvider provider;

    @NotNull private M mapping;
    /** @return the mapping that is maintained by this object */
    @NotNull public M getMapping() { return this.mapping; }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull protected Optional<File> mappingFile;

    /**
     * Creates new file mapper.
     * @param mapping the mapping object
     * @param file the file that is used to store mapping
     */
    public FileMapper(@NotNull M mapping, @Nullable File file) {
        this.mapping = mapping;
        this.mappingFile = Optional.ofNullable(file);
    }

    /**
     * Attaches file to mapping
     * @param file the {@link File} to be attached to mapping. File should be already existing
     *             in mapped directory prior to this call
     * @param checksum the checksum calculated for {@code file}
     * @param node the name of node that file belongs to
     * @return {@code true} if file was attached, {@code false} if this file is already attached
     */
    public abstract boolean attachFile(@NotNull File file, @NotNull String checksum, @NotNull String node);

    /**
     * Detaches file from mapping
     * @param file the {@link File} to be attached to mapping. File still be existing in mapped directory
     *             prior to this call
     * @param node the name of node that file belongs to
     * @return {@code true} if file was detached, {@code false} if this file is already detached
     */
    public abstract boolean detachFile(@NotNull File file, @NotNull String node);

    /**
     * Updates file in mapping
     * @param file the {@link File} to be updated in mapping. File should be already updated
     *             in directory prior to this call
     * @param checksum the checksum calculated for {@code file}
     * @param node the name of node that file belongs to
     * @return {@code true} if file was updated, {@code false} if no updates is necessary/possible
     */
    public abstract boolean updateFile(@NotNull File file, @NotNull String checksum, @NotNull String node);

    /**
     * Shares file to {@code receiver}
     * @param file the {@link File} to be shared. File should be already existing in directory
     *             prior to this call
     * @param node the name of node that file belongs to
     * @param receiver the node that file should be shared to
     * @return {@code true} if file was shared, {@code false} if file is already shared,
     *         {@code null} if sharing functionality is not supported
     */
    @Nullable public abstract Boolean shareFile(@NotNull File file, @NotNull String node, @NotNull String receiver);

    /**
     * Cancels sharing of file to {@code receiver}
     * @param file the {@link File} that was shared. File should be still existing in directory
     *             prior to this call
     * @param node the name of node that file belongs to
     * @param receiver the node that file was shared to
     * @return {@code true} if file was shared, {@code false} if file is already unshared,
     *         {@code null} if sharing functionality is not supported
     */
    @Nullable public abstract Boolean unshareFile(@NotNull File file, @NotNull String node, @NotNull String receiver);

    /**
     * Represents companion object of {@link FileMapper} stored in {@link FileMapper#provider},
     * used for creating/loading file mapping via factory methods.
     * <p>Implementation of this class should be used to pass file mapping to {@link FileMapper}
     * constructor. It should also be impossible to initialize this class outside of this parent,
     * as it should only be instantiated as singleton in {@link FileMapper}.
     * @param <M> the type holding indexed mapping. This should be same as type used by
     *            {@link FileMapper}, as those implementations should be coupled
     * @param <P> the type wrapping initial parameters of structure
     */
    public abstract static class FileMappingProvider<M, P> {

        protected FileMappingProvider() {  }

        /**
         * Creates new mapping of file structure and saves its results in file.
         * <br><br>
         * <p>If file permissions, especially writing is permitted, then this method
         * will always {@link Yaml#dump(Object, Writer) dump} its result to specified
         * path, so it is important to provide file that can be overridden and/or losing
         * of its current contents is not important anymore.
         * @param file the file that is to be used to save mapping,
         *                 if {@code null} then mapping will not be saved to file (runtime only)
         * @param parameters the parameters that are being passed to file mapping object constructor
         * @return {@link Pair} of object containing created mapping and boolean value ({@code true}
         *         if {@code file} didn't exist prior to call, {@code false} if it did, {@code null}
         *         if {@code file} was not provided
         * @throws RuntimeException if exception was thrown while initialising file mapping (optional)
         * @throws FileNotFoundException if provided {@code file} was a directory rather than file
         * @throws NotDirectoryException if node that should represent file placeholder is not
         *                               a directory (optional)
         * @throws IOException if an I/O exception occurs while creating mapping file
         */
        @NotNull public abstract Pair<M, Boolean> createStructure(@Nullable File file, @NotNull P parameters)
                throws RuntimeException, FileNotFoundException, NotDirectoryException, IOException;

        /**
         * Loads mapping from file.
         * <br><br>
         * <p>It would be advised to call this method prior to {@link #createStructure(File, Object) createStructure(File, P)},
         * and perform some actions based on difference of those two calls, as this would
         * suggest that modifications to file structure were performed in between execution
         * of application
         * @param file the file that is to be used to load mapping
         * @return object containing loaded mapping
         * @throws FileNotFoundException if provided {@code file} was not found,
         *                               or was a directory rather than file
         * @throws YAMLException if parsing {@code file} throws exception
         * @throws IOException if an I/O exception occurs while closing reading stream
         */
        @Nullable public abstract M loadStructure(@NotNull File file)
                throws FileNotFoundException, YAMLException, IOException;
    }

    /**
     * Recursively creates list of files.
     * <p>Mapping of filesystem is resolved i.e.:
     * <blockquote><pre><code>
     * 📁 rootDirectory
     * ├ 📄 file.txt
     * └ 📁 childDirectory
     *   └ 📄 anotherFile.txt
     * </code></pre></blockquote>
     * into:
     * <blockquote><pre><code>
     * files: [
     *   - pathname: file.txt
     *   - pathname: childDirectory/anotherFile.txt
     * ]
     * </code></pre></blockquote>
     * @param directory the directory to create list of files of, usually same as {@code rootDirectory}
     * @param rootDirectory the root directory, it is used cut off this part of path from list of files
     * @return list of all files contained in {@code directory}, and its subdirectories
     */
    protected static List<FileMapping> listFiles(File directory, File rootDirectory) {
        List<FileMapping> ret;
        File[] nodes = directory.listFiles();
        if (nodes != null && nodes.length > 0) {
            ret = Arrays.stream(nodes)
                        .filter(f -> !f.isDirectory())
                        .map(f -> {
                            try {
                                return new FileMapping(
                                        getRelativePath(f, rootDirectory),
                                    f.length(),
                                    FileManager.getChecksum(f),
                                    f.lastModified()
                                );
                            } catch (Exception e) {
                                return new FileMapping(e.getMessage(), -1L, "", -1L);
                            }
                        })
                        .collect(Collectors.toList());
            List<File> directoryNodes = Arrays.stream(nodes)
                                              .filter(File::isDirectory)
                                              .collect(Collectors.toList());
            for (File directoryNode : directoryNodes) {
                ret.addAll(listFiles(directoryNode, rootDirectory));
            }
            return ret;
        } else return new ArrayList<>();
    }

    /**
     * Resolves path of {@code file} relative to {@code rootDirectory}
     * @param file the file to get path of
     * @param rootDirectory the directory to create path relative to
     * @return {@link String} with relative path
     */
    @NotNull public static String getRelativePath(@NotNull File file, @NotNull File rootDirectory) {
        return file.getAbsolutePath().substring(rootDirectory.getAbsolutePath().length() + 1);
    }
}
