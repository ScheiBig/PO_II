package edu.jeznach.po2.server.file;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.file.FileManager;
import edu.jeznach.po2.common.file.FileMapper;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.Pair;
import edu.jeznach.po2.common.util.Throwables;
import edu.jeznach.po2.server.gui.NotificationSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages mapping of server drive structure.
 * @see FileMapper
 */
public class DriveFileMapper extends FileMapper<DriveMapping> {

    /**
     * Contains factory methods for creating/loading file mapping.
     * <br><br>
     * <p>All {@link FileMapper} implementations should hide this field with
     * instance of its own implementations
     */
    public static final DriveFileMappingProvider provider;
    static { provider = new DriveFileMappingProvider(); }

    /**
     * Creates new file mapper and attempts to store given mapping.
     * @param mapping the mapping object
     * @param file the file that is used to store mapping
     */
    public DriveFileMapper(@NotNull DriveMapping mapping, @Nullable File file) {
        super(mapping, file);
        mappingFile.ifPresent(this::dumpToFile);
    }


    @Override
    public synchronized boolean attachFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        String attach = "ATTACH " + getMapping().getName() + ": " + file.getName();
        List<DriveMapping.User> users = getMapping().getUsers();
        Optional<DriveMapping.User> optionalUser = users.stream()
                                                        .filter(u -> u.getUsername().equals(node))
                                                        .findFirst();
        DriveMapping.User user;
        if (!optionalUser.isPresent()) {
            DriveMapping.User newUser = new DriveMapping.User(node);
            users.add(newUser);
            user = newUser;
            attach += " create user";
        } else user = optionalUser.get();
        List<ServFileMapping> files = user.getFiles();
        String relativeFilePath = FileManager.getRelativePath(getMapping().getDrive_location()
                                                              + File.separator + node, file.getPath());
        if (files != null) {
            Optional<ServFileMapping> fileOptional = files.stream()
                                                          .filter(f -> f.getPathname().equals(relativeFilePath))
                                                          .findFirst();
            if (fileOptional.isPresent()) {
                System.out.println(attach);
                return false;
            } else {
                attach += " add file";
                files.add(new ServFileMapping(relativeFilePath,
                                              file.length(),
                                              checksum,
                                              file.lastModified(),
                                              getMapping().getName()));
                user.setUsed_space_bytes(user.getUsed_space_bytes() + file.length());
                mappingFile.ifPresent(this::dumpToFile);
                System.out.println(attach);
                return true;
            }
        } else {
            attach += " create files";
            user.setFiles(new ArrayList<>());
            //noinspection ConstantConditions
            user.getFiles().add(new ServFileMapping(relativeFilePath,
                                                    file.length(),
                                                    checksum,
                                                    file.lastModified(),
                                                    getMapping().getName()));
            user.setUsed_space_bytes(user.getUsed_space_bytes() + file.length());
            mappingFile.ifPresent(this::dumpToFile);
            System.out.println(attach);
            return true;
        }
    }

    @Override
    public synchronized boolean detachFile(@NotNull File file, @NotNull String node) {
        List<DriveMapping.User> users = getMapping().getUsers();
        Optional<DriveMapping.User> optionalUser = users.stream()
                                                        .filter(u -> u.getUsername().equals(node))
                                                        .findFirst();
        if (optionalUser.isPresent()) {
            List<ServFileMapping> files = optionalUser.get()
                                                      .getFiles();
            String relativeFilePath = FileManager.getRelativePath(getMapping().getDrive_location()
                                                                  + File.separator + node, file.getPath());
            if (files != null) {
                Optional<ServFileMapping> fileOptional = files.stream()
                                                              .filter(f -> f.getPathname()
                                                                        .equals(relativeFilePath))
                                                              .findFirst();
                if (fileOptional.isPresent()) {
                    files.remove(fileOptional.get());
                    optionalUser.get()
                                .setUsed_space_bytes(
                                        optionalUser.get()
                                                    .getUsed_space_bytes() - file.length());
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized boolean updateFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        List<DriveMapping.User> users = getMapping().getUsers();
        Optional<DriveMapping.User> optionalUser = users.stream()
                                                        .filter(u -> u.getUsername().equals(node))
                                                        .findFirst();
        if (optionalUser.isPresent()) {
            List<ServFileMapping> files = optionalUser.get().getFiles();
            String relativeFilePath = FileManager.getRelativePath(getMapping().getDrive_location()
                                                                  + File.separator + node, file.getPath());
            if (files != null) {
                Optional<ServFileMapping> fileOptional = files.stream()
                                                              .filter(f -> f.getPathname().equals(relativeFilePath))
                                                              .findFirst();
                if (fileOptional.isPresent()) {
                    ServFileMapping fileMapping = fileOptional.get();
                    long oldSize = fileMapping.getSize_bytes();
                    fileMapping.setChecksum(checksum);
                    fileMapping.setModification_timestamp(file.lastModified());
                    fileMapping.setSize_bytes(file.length());
                    optionalUser.get().setUsed_space_bytes(
                            optionalUser.get().getUsed_space_bytes() - oldSize + file.length()
                    );
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized @Nullable Boolean shareFile(@NotNull File file, @NotNull String node, @NotNull String receiver) {
        System.out.println("share");
        List<DriveMapping.User> users = getMapping().getUsers();
        Optional<DriveMapping.User> optionalUser = users.stream()
                                                        .filter(u -> u.getUsername().equals(node))
                                                        .findFirst();
        Optional<DriveMapping.User> optionalReceiver = users.stream()
                                                        .filter(u -> u.getUsername().equals(receiver))
                                                        .findFirst();
        if (optionalUser.isPresent() && optionalReceiver.isPresent()) {
            List<ServFileMapping> files = optionalUser.get()
                                                      .getFiles();
            String relativeFilePath = FileManager.getRelativePath(getMapping().getDrive_location()
                                                                  + File.separator + node, file.getPath());
            if (files != null) {
                Optional<ServFileMapping> fileOptional = files.stream()
                                                              .filter(f -> f.getPathname().equals(relativeFilePath))
                                                              .findFirst();
                List<ServSharedFileMapping> shared_files = optionalReceiver.get().getShared_files();
                if (fileOptional.isPresent()) {
                    if (shared_files == null) {
                        optionalReceiver.get().setShared_files(new ArrayList<>());
                        shared_files = optionalReceiver.get().getShared_files();
                    }
                    ServSharedFileMapping sharedFileMapping = new ServSharedFileMapping(fileOptional.get(), node);
                    shared_files.add(sharedFileMapping);
                    mappingFile.ifPresent(this::dumpToFile);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized @Nullable Boolean unshareFile(@NotNull File file, @NotNull String node, @NotNull String receiver) {
            List<DriveMapping.User> users = getMapping().getUsers();
            Optional<DriveMapping.User> optionalUser = users.stream()
                                                            .filter(u -> u.getUsername().equals(node))
                                                            .findFirst();
        Optional<DriveMapping.User> optionalReceiver = users.stream()
                                                            .filter(u -> u.getUsername().equals(receiver))
                                                            .findFirst();
        if (optionalUser.isPresent() && optionalReceiver.isPresent()) {
            List<ServFileMapping> files = optionalUser.get()
                                                      .getFiles();
            String relativeFilePath = FileManager.getRelativePath(getMapping().getDrive_location()
                                                                  + File.separator + node, file.getPath());
            if (files != null) {
                Optional<ServFileMapping> fileOptional = files.stream()
                                                              .filter(f -> f.getPathname().equals(relativeFilePath))
                                                              .findFirst();
                List<ServSharedFileMapping> shared_files = optionalReceiver.get().getShared_files();
                if (fileOptional.isPresent() && shared_files != null) {
                    Optional<ServSharedFileMapping> sharedFileMapping =
                            shared_files.stream()
                                        .filter(f -> f.getOwner().equals(node))
                                        .filter(f -> f.getPathname().equals(relativeFilePath))
                                        .findFirst();
                    if (sharedFileMapping.isPresent()) {
                        shared_files.remove(sharedFileMapping.get());
                        mappingFile.ifPresent(this::dumpToFile);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void error(Exception e) {
        NotificationSender sender =
                new NotificationSender(getMapping().getName(),
                                       new Log(new File(getMapping().getDrive_location() +
                                                        File.separator +
                                                        getMapping().getLog_name())));
        sender.error(e.getMessage(), Throwables.getStackTrace(e));
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

    /**
     * Represents companion object of {@link DriveFileMapper} stored in {@link DriveFileMapper#provider},
     * used for creating/loading file mapping via factory methods.
     */
    public static class DriveFileMappingProvider
            extends FileMappingProvider<DriveMapping, DriveMapping.InitParams> {

        private DriveFileMappingProvider() { }

        @SuppressWarnings("DuplicateThrows")
        @Override
        public @NotNull Pair<DriveMapping, Boolean> createStructure(@Nullable File file,
                                                                    @NotNull DriveMapping.InitParams parameters)
                throws FileNotFoundException, IOException {
                Yaml yaml = new Yaml();
                DriveMapping mapping = new DriveMapping(parameters);
                Boolean fileCreated;
                File drive = parameters.driveLocation;
                //noinspection ResultOfMethodCallIgnored
                drive.mkdirs();
                File[] driveDirectories = drive.listFiles();
                mapping.setUsers(listUsers(driveDirectories, mapping.getName()));
                if (file != null) {
                    File fileToCreate = new File(drive.getAbsolutePath() + File.separator + file.getPath());
                    fileCreated = fileToCreate.createNewFile();
                    Writer writer = new OutputStreamWriter(new FileOutputStream(fileToCreate));
                    yaml.dump(mapping, writer);
                    writer.close();
                } else fileCreated = null;
                return Pair.of(mapping, fileCreated);
        }

        @SuppressWarnings("DuplicateThrows")
        @Override
        public @Nullable DriveMapping loadStructure(@NotNull File file)
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

        private static List<DriveMapping.User> listUsers(File[] directories, String drive) {
            if (directories != null) {
                return Arrays.stream(directories)
                             .filter(File::isDirectory)
                             .filter(f -> !f.getName().endsWith(Configuration.TEMP_EXTENSION))
                             .map(f -> Pair.of(new DriveMapping.User(f.getName()), f))
                             .peek(p -> p.key().setFiles(listFiles(p.value(), p.value())
                                                                 .stream()
                                                                 .map(f -> new ServFileMapping(f, drive))
                                                                 .collect(Collectors.toList())))
                             .map(Pair::key)
                             .peek(user -> {
                                 if (user.getFiles() != null) {
                                     user.setUsed_space_bytes(
                                             user.getFiles()
                                                 .stream()
                                                 .map(ServFileMapping::getSize_bytes)
                                                 .reduce(0L, Long::sum)
                                     );
                                 }
                             })
                             .collect(Collectors.toList());
            } else return new ArrayList<>();
        }
    }
}
