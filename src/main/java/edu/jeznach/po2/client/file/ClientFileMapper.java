package edu.jeznach.po2.client.file;

import edu.jeznach.po2.client.file.ClientMapping.Directories;
import edu.jeznach.po2.client.gui.NotificationSender;
import edu.jeznach.po2.common.file.FileMapper;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Represents mapping of client directories structure.
 * <p>This class only accepts nodes represented by {@link Directories}
 * @see FileMapper
 */
public class ClientFileMapper extends FileMapper<ClientMapping> {



    /**
     * Creates new file mapper.
     *
     * @param mapping the mapping object
     * @param file    the file that is used to store mapping
     */
    public ClientFileMapper(@NotNull ClientMapping mapping, @Nullable File file) {
        super(mapping, file);
    }

    @Override
    public boolean attachFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        checkNode(node);
        return false;
    }

    @Override
    public boolean detachFile(@NotNull File file, @NotNull String node) {
        checkNode(node);
        return false;
    }

    @Override
    public boolean updateFile(@NotNull File file, @NotNull String checksum, @NotNull String node) {
        checkNode(node);
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

    public static class ClientMappingProvider
            extends FileMappingProvider<ClientFileMapper, ClientMapping.InitParams> {

        private ClientMappingProvider() {  }

        @Override
        public @NotNull Pair<ClientFileMapper, Boolean> createStructure(@Nullable File file,
                                                                        ClientMapping.@NotNull InitParams parameters)
                throws RuntimeException, IOException {
            return null;
        }

        @Nullable
        @Override
        public ClientFileMapper loadStructure(@NotNull File file)
                throws FileNotFoundException, YAMLException, IOException {
            return null;
        }
    }



}
