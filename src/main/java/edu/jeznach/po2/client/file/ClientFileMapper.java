package edu.jeznach.po2.client.file;

import edu.jeznach.po2.client.file.ClientMapping.Directories;
import edu.jeznach.po2.client.gui.NotificationSender;
import edu.jeznach.po2.common.file.FileMapper;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.Pair;
import edu.jeznach.po2.server.file.DriveFileMapper;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages mapping of client structure.
 * <p>This class only accepts nodes represented by {@link Directories}
 * @see FileMapper
 */
public class ClientFileMapper extends FileMapper<ClientMapping> {

    /**
     * Contains factory methods for creating/loading file mapping.
     * <br><br>
     * <p>All {@link FileMapper} implementations should hide this field with
     * instance of its own implementations
     */
    public static @NotNull ClientMappingProvider provider;
    static { provider = new ClientMappingProvider(); }

    /**
     * Creates new file mapper.
     *
     * @param mapping the mapping object
     * @param file    the file that is used to store mapping
     */
    public ClientFileMapper(@NotNull ClientMapping mapping, @Nullable File file) { super(mapping, file); }

    @Override
    public boolean attachFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        checkNode(node);
        String relativeFilePath = getRelativePath(file,
                                                  new File(getMapping().getClient_location()
                                                           + File.separator + node));
        if (Directories.files.toString().equals(node)) {
            if (getMapping().getFiles() == null) getMapping().setFiles(new ArrayList<>());
            Optional<ClientFileMapping> fileOptional = getMapping().getFiles()
                                                                   .stream()
                                                                   .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                   .findFirst();
            if (!fileOptional.isPresent()) {
                getMapping().getFiles()
                            .add(new ClientFileMapping(
                                    relativeFilePath, file.length(), checksum, file.lastModified()
                            ));
                mappingFile.ifPresent(this::dumpToFile);
                return true;
            }
        } else if (Directories.cancel.toString().equals(node)) {
            if (getMapping().getCancelled_files() == null) getMapping().setCancelled_files(new ArrayList<>());
            Optional<FileMapping> fileOptional = getMapping().getCancelled_files()
                                                             .stream()
                                                             .filter(f -> f.getPathname().equals(relativeFilePath))
                                                             .findFirst();
            if (!fileOptional.isPresent()) {
                getMapping().getCancelled_files()
                            .add(new FileMapping(
                                    relativeFilePath, file.length(), checksum, file.lastModified()
                            ));
                mappingFile.ifPresent(this::dumpToFile);
                return true;
            }
        } else if (Directories.shared.toString().equals(node)) {
            String[] path = relativeFilePath.split(Pattern.quote(File.separator), 2);
            if (path.length != 2) throw new IllegalArgumentException("Path " + relativeFilePath + " is not a shared file path.");
            if (getMapping().getShared_files() == null) getMapping().setShared_files(new ArrayList<>());
            Optional<SharedFileMapping> fileOptional = getMapping().getShared_files()
                                                                   .stream()
                                                                   .filter(f -> f.getPathname().equals(path[1]))
                                                                   .filter(f -> f.getOwner().equals(path[0]))
                                                                   .findFirst();
            if (!fileOptional.isPresent()) {
                getMapping().getShared_files()
                            .add(new SharedFileMapping(
                                    path[1], file.length(), checksum, file.lastModified(), path[0]
                            ));
                mappingFile.ifPresent(this::dumpToFile);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean detachFile(@NotNull File file, @NotNull String node) {
        checkNode(node);
        String relativeFilePath = getRelativePath(file,
                                                  new File(getMapping().getClient_location()
                                                           + File.separator + node));
        if (Directories.files.toString().equals(node)) {
            if (getMapping().getFiles() != null) {
                Optional<ClientFileMapping> fileOptional = getMapping().getFiles()
                                                                       .stream()
                                                                       .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                       .findFirst();
                if (fileOptional.isPresent()) {
                    getMapping().getFiles().remove(fileOptional.get());
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        } else if (Directories.cancel.toString().equals(node)) {
            if (getMapping().getCancelled_files() != null) {
                Optional<FileMapping> fileOptional = getMapping().getCancelled_files()
                                                                 .stream()
                                                                 .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                 .findFirst();
                if (fileOptional.isPresent()) {
                    getMapping().getCancelled_files().remove(fileOptional.get());
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        } else if (Directories.shared.toString().equals(node)) {
            if (getMapping().getShared_files() != null) {
                String[] path = relativeFilePath.split(Pattern.quote(File.separator), 2);
                if (path.length != 2)
                    throw new IllegalArgumentException("Path " + relativeFilePath + " is not a shared file path.");
                Optional<SharedFileMapping> fileOptional = getMapping().getShared_files()
                                                                       .stream()
                                                                       .filter(f -> f.getPathname().equals(path[1]))
                                                                       .filter(f -> f.getOwner().equals(path[0]))
                                                                       .findFirst();
                if (fileOptional.isPresent()) {
                    getMapping().getShared_files().remove(fileOptional.get());
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        checkNode(node);
        String relativeFilePath = getRelativePath(file,
                                                  new File(getMapping().getClient_location()
                                                           + File.separator + node));
        if (Directories.files.toString().equals(node)) {
            if (getMapping().getFiles() != null) {
                Optional<ClientFileMapping> fileOptional = getMapping().getFiles()
                                                                       .stream()
                                                                       .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                       .findFirst();
                if (fileOptional.isPresent()) {
                    ClientFileMapping fileMapping = fileOptional.get();
                    long oldSize = fileMapping.getSize_bytes();
                    fileMapping.setChecksum(checksum);
                    fileMapping.setModification_timestamp(file.lastModified());
                    fileMapping.setSize_bytes(file.length());
                    getMapping().setUsed_space_bytes(
                            getMapping().getUsed_space_bytes() - oldSize + file.length()
                    );
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        } else if (Directories.cancel.toString().equals(node)) {
            if (getMapping().getCancelled_files() != null) {
                Optional<FileMapping> fileOptional = getMapping().getCancelled_files()
                                                                 .stream()
                                                                 .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                 .findFirst();
                if (fileOptional.isPresent()) {
                    FileMapping fileMapping = fileOptional.get();
                    long oldSize = fileMapping.getSize_bytes();
                    fileMapping.setChecksum(checksum);
                    fileMapping.setModification_timestamp(file.lastModified());
                    fileMapping.setSize_bytes(file.length());
                    getMapping().setUsed_space_bytes(
                            getMapping().getUsed_space_bytes() - oldSize + file.length()
                    );
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        } else if (Directories.shared.toString().equals(node)) {
            if (getMapping().getShared_files() != null) {
                String[] path = relativeFilePath.split(Pattern.quote(File.separator), 2);
                if (path.length != 2)
                    throw new IllegalArgumentException("Path " + relativeFilePath + " is not a shared file path.");
                Optional<SharedFileMapping> fileOptional = getMapping().getShared_files()
                                                                       .stream()
                                                                       .filter(f -> f.getPathname().equals(path[1]))
                                                                       .filter(f -> f.getOwner().equals(path[0]))
                                                                       .findFirst();
                if (fileOptional.isPresent()) {
                    SharedFileMapping fileMapping = fileOptional.get();
                    long oldSize = fileMapping.getSize_bytes();
                    fileMapping.setChecksum(checksum);
                    fileMapping.setModification_timestamp(file.lastModified());
                    fileMapping.setSize_bytes(file.length());
                    getMapping().setUsed_space_bytes(
                            getMapping().getUsed_space_bytes() - oldSize + file.length()
                    );
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable Boolean shareFile(@NotNull File file, @NotNull String node, @NotNull String receiver) {
        checkNode(node);
        if (!node.equals(Directories.files.toString())) return null;
        if (getMapping().getFiles() != null) {
            String relativeFilePath = getRelativePath(file,
                                                      new File(getMapping().getClient_location()
                                                      + File.separator + node));
            Optional<ClientFileMapping> fileOptional = getMapping().getFiles()
                                                                   .stream()
                                                                   .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                   .findFirst();
            if (fileOptional.isPresent()) {
                ClientFileMapping mappedFile = fileOptional.get();
                if (mappedFile.getReceivers() == null) {
                    mappedFile.setReceivers(new ArrayList<>());
                } else {
                    Optional<String> receiverOptional = mappedFile.getReceivers()
                                                                  .stream()
                                                                  .filter(r -> r.equals(receiver))
                                                                  .findFirst();
                    if (receiverOptional.isPresent()) return false;
                }
                mappedFile.getReceivers().add(receiver);
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable Boolean unshareFile(@NotNull File file, @NotNull String node, @NotNull String receiver) {
        checkNode(node);
        if (!node.equals(Directories.files.toString())) return null;
        if (getMapping().getFiles() != null) {
            String relativeFilePath = getRelativePath(file,
                                                      new File(getMapping().getClient_location()
                                                               + File.separator + node));
            Optional<ClientFileMapping> fileOptional = getMapping().getFiles()
                                                                   .stream()
                                                                   .filter(f -> f.getPathname().equals(relativeFilePath))
                                                                   .findFirst();
            if (fileOptional.isPresent()) {
                ClientFileMapping mappedFile = fileOptional.get();
                if (mappedFile.getReceivers() == null) return false;
                Optional<String> receiverOptional = mappedFile.getReceivers()
                                                              .stream()
                                                              .filter(r -> r.equals(receiver))
                                                              .findFirst();
                if (receiverOptional.isPresent()) {
                    mappedFile.getReceivers().remove(receiverOptional.get());
                    return true;
                } else return false;
            }
        }
        return false;
    }

    private void error(Exception e) {
        NotificationSender sender =
                new NotificationSender(getMapping().getName(),
                                       new Log(new File(getMapping().getClient_location() +
                                                        File.separator +
                                                        getMapping().getLog_name())));
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        sender.error(e.getMessage(), w.toString());
        sender.close();
    }

    private void dumpToFile(File file) {
        try {
            Writer writer = new FileWriter(file);
            writer.write(new Yaml().dump(getMapping()));
            writer.close();
        } catch (IOException e) {
            error(e);
        }
    }

    private void checkNode(@NotNull String node) {
        try {
            ClientMapping.Directories.valueOf(node);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Node " + node + " is not proper client node.", e);
        }
    }

    /**
     * Represents companion object of {@link DriveFileMapper} stored in {@link DriveFileMapper#provider},
     * used for creating/loading file mapping via factory methods.
     */
    public static class ClientMappingProvider
            extends FileMappingProvider<ClientMapping, ClientMapping.InitParams> {

        private ClientMappingProvider() {  }

        @SuppressWarnings("DuplicateThrows")
        @Override
        public @NotNull Pair<ClientMapping, Boolean> createStructure(@Nullable File file,
                                                                     ClientMapping.@NotNull InitParams parameters)
                throws FileNotFoundException, NotDirectoryException, IOException {
            Yaml yaml = new Yaml();
            ClientMapping mapping = new ClientMapping(parameters);
            Boolean fileCreated;
            File client = parameters.clientLocation;
            //noinspection ResultOfMethodCallIgnored
            client.mkdirs();
            File[] listFiles = client.listFiles();
            if (listFiles != null) {
                for (File listFile : listFiles) {
                    String name = listFile.getName();
                    if (Directories.files.toString().equals(name)) {
                        if (!listFile.isDirectory())
                            throw new NotDirectoryException("Node: " + name + " is not a directory." +
                                                            " Cannot create mapping");
                        mapping.setFiles(listFiles(listFile, listFile).stream()
                                                                      .map(ClientFileMapping::new)
                                                                      .collect(Collectors.toList()));
                    } else if (Directories.cancel.toString().equals(name)) {
                        if (!listFile.isDirectory())
                            throw new NotDirectoryException("Node: " + name + " is not a directory." +
                                                            " Cannot create mapping");
                        mapping.setCancelled_files(listFiles(listFile, listFile).stream()
                                                                                .map(FileMapping::new)
                                                                                .collect(Collectors.toList()));
                    } else if (Directories.shared.toString().equals(name)) {
                        if (!listFile.isDirectory())
                            throw new NotDirectoryException("Node: " + name + " is not a directory." +
                                                            " Cannot create mapping");
                        mapping.setShared_files(listUsers(listFile.listFiles()));
                    }
                }
            }
            if (file != null) {
                File fileToCreate = new File(client.getAbsolutePath() + "/" + file.getPath());
                fileCreated = fileToCreate.createNewFile();
                Writer writer = new OutputStreamWriter(new FileOutputStream(fileToCreate));
                yaml.dump(mapping, writer);
                writer.close();
            } else fileCreated = null;
            return Pair.of(mapping, fileCreated);
        }

        @SuppressWarnings("DuplicateThrows")
        @Override
        public @Nullable ClientMapping loadStructure(@NotNull File file)
                throws FileNotFoundException, YAMLException, IOException {
            Yaml yaml = new Yaml();
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                return yaml.load(reader);
            } catch (YAMLException e) {
                throw new YAMLException("Exception thrown while parsing file mapping," +
                                        " try to manually repair file, or delete it to " +
                                        "create it from files (will remove all sharing links)", e);
            }
        }

        private static List<SharedFileMapping> listUsers(File[] directories) {
            if (directories != null) {
                return Arrays.stream(directories)
                             .filter(File::isDirectory)
                             .map(f -> Pair.of(listFiles(f, f), f.getName()))
                             .map(fl -> fl.key.stream()
                                              .map(f -> new SharedFileMapping(f, fl.value))
                                              .collect(Collectors.toList()))
                             .flatMap(List::stream)
                             .collect(Collectors.toList());
            } else return new ArrayList<>();
        }
    }
}
