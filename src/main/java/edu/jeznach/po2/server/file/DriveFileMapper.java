package edu.jeznach.po2.server.file;

import edu.jeznach.po2.common.file.FileMapper;
import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DriveFileMapper extends FileMapper<DriveMapping> {

    public static final DriveFileMappingProvider provider = new DriveFileMappingProvider();

    public DriveFileMapper(@NotNull DriveMapping mapping, @Nullable File file) {
        super(mapping, file);
    }


    @Override
    public boolean attachFile(@NotNull File file) {
        return false;
    }

    @Override
    public boolean detachFile(@NotNull File file) {
        return false;
    }

    @Override
    public boolean updateFile(@NotNull File file) {
        return false;
    }

    @Override
    public @Nullable Boolean shareFile(@NotNull File file, @NotNull String receiver) {
        return null;
    }

    public static class DriveFileMappingProvider
            extends FileMappingProvider<DriveMapping, DriveMapping.InitParams> {

        private DriveFileMappingProvider() { }

        @NotNull
        @Override
        public Pair<DriveMapping, Boolean> createStructure(@Nullable File file,
                                                           @NotNull DriveMapping.InitParams parameters)
                throws IOException {
                Yaml yaml = new Yaml();
                DriveMapping mapping = new DriveMapping(parameters);
                Boolean fileCreated;
                if (file != null) {
                    File drive = new File(parameters.driveLocation);
                    //noinspection ResultOfMethodCallIgnored
                    drive.mkdirs();
                    mapping.setName(drive.getName());

                    File[] driveDirectories = drive.listFiles();
                    mapping.setUsers(listUsers(driveDirectories));

                    File fileToCreate = new File(drive.getAbsolutePath() + "/" + file.getPath());
                    fileCreated = fileToCreate.createNewFile();
                    Writer writer = new OutputStreamWriter(new FileOutputStream(fileToCreate));
                    yaml.dump(mapping, writer);
                    writer.close();
                } else fileCreated = null;
                return Pair.of(mapping, fileCreated);
        }

        @SuppressWarnings("DuplicateThrows")
        @Nullable
        @Override
        public DriveMapping loadStructure(@NotNull File file) throws FileNotFoundException, YAMLException, IOException {
            Yaml yaml = new Yaml();
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                return yaml.load(reader);
            } catch (YAMLException e) {
                throw new YAMLException("Exception thrown while parsing file mapping," +
                                        " try to manually repair file, or delete it to " +
                                        "create it from files (will remove all sharing links)", e);
            }
        }

        private static List<DriveMapping.User> listUsers(File[] directories) {
            if (directories != null) {
                return Arrays.stream(directories)
                             .filter(File::isDirectory)
                             .map(f -> Pair.of(new DriveMapping.User(f.getName()), f))
                             .peek(p -> p.key.setFiles(listFiles(p.value, p.value)))
                             .map(p -> p.key)
                             .peek(user -> {
                                 if (user.getFiles() != null) {
                                     user.setUsed_space_bytes(
                                             user.getFiles()
                                                 .stream()
                                                 .map(FileMapping::getSize_bytes)
                                                 .reduce(0L, Long::sum)
                                     );
                                 }
                             })
                             .collect(Collectors.toList());
            } else return new ArrayList<>();
        }
    }
}
