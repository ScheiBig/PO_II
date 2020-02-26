package edu.jeznach.po2.server.work;

import edu.jeznach.po2.common.communication.Messages;
import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.file.FileManager;
import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.SharedFileMapping;
import edu.jeznach.po2.common.gui.FileTree;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.BreakableInputStream;
import edu.jeznach.po2.common.util.LogHelper;
import edu.jeznach.po2.common.util.Nothing;
import edu.jeznach.po2.common.util.Pair;
import edu.jeznach.po2.server.App;
import edu.jeznach.po2.server.file.DriveFileMapper;
import edu.jeznach.po2.server.file.DriveMapping;
import edu.jeznach.po2.server.file.ServFileMapping;
import edu.jeznach.po2.server.file.UserMappingCollector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileEventWorker extends Thread {

    private Log log;
    private LogHelper helper;
    private Socket socket;
    private PrintStream out;
    private OutputStream byteOut;
    private BreakableInputStream in;
    private InputStream byteIn;
    private App app;

    public FileEventWorker(@NotNull Log log,
                           @NotNull Socket socket,
                           @NotNull App app) throws IOException {
        this.helper = new LogHelper(this.log = log);
        this.socket = socket;
        this.byteOut = socket.getOutputStream();
        this.out = new PrintStream(byteOut);
        this.byteIn = socket.getInputStream();
        this.in = new BreakableInputStream(byteIn);
        this.app = app;
    }

    @Override
    @SuppressWarnings({"UnnecessaryContinue", "RedundantCast", "ResultOfMethodCallIgnored"})// <1,2- clarifying <3- unneeded
    public void run() {
        while (true) {
            String message;
            try {
                message = new String(in.readLine());
            } catch (IOException e) {
                log.exception("Cannot read message", e);
                break;
            }
//            if (message == null) continue;
            Messages.Msg command = Messages.parseMessage(message);
            if (command instanceof Messages.CreateFile) {

                String filePath = ((Messages.CreateFile) command).file();
                String checksum = ((Messages.CreateFile) command).checksum();
                int size = ((Messages.CreateFile) command).size()
                                                          .intValue();
                String username = ((Messages.CreateFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                DriveFileMapper mapper = app.acquireMapper();
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                try {
                    //noinspection ConstantConditions <- caught
                    if (user.getFiles()
                            .stream()
                            .anyMatch(f -> f.getPathname()
                                            .equals(filePath))) {
                        /// File exists - decline transfer
                        out.println(Messages.NO);
                        app.releaseMapper(mapper);
                        continue;
                    }
                } catch (NullPointerException ignore) {
                    /// User doesn't exist - proceed
                    /// User doesn't have any files - proceed
                }
                out.println(Messages.OK);
                /// Create and attach result file
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath);
                if (helper.expecting(() -> {
                    file.getParentFile()
                        .mkdirs();
                    file.createNewFile();
                }, "Cannot create result file")
                          .resolve(() -> app.releaseMapper(mapper))) break;
                mapper.attachFile(file, checksum, username);
                /// Create temporary file
                File tempFile = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath +
                        Configuration.TEMP_EXTENSION
                );
                if (helper.expecting(() -> {
                    tempFile.getParentFile()
                            .mkdirs();
                    tempFile.createNewFile();
                }, "Cannot create temporary file")
                          .resolve(() -> helper.expect(() -> {
                              Files.delete(file.toPath());
                              mapper.detachFile(file, username);
                              app.releaseMapper(mapper);
                          }, "Cannot dispose files"))) break;
                /// Receive file and reload fileTree
                if (helper.expecting(() -> {
                    FileManager.receiveFile(null, null, tempFile, byteIn, size, false);
                    SwingUtilities.invokeLater(() -> reload(app.getFileTrees()
                                                               .get(App.getDriveIndex(mapper.getMapping()
                                                                                            .getName())),
                                                            mapper));
                }, "Cannot receive file")
                          .resolving(() -> {
                              mapper.detachFile(file, username);
                              helper.expect(() -> {
                                  Files.delete(file.toPath());
                                  Files.delete(tempFile.toPath());
                              }, "Cannot dispose files");
                          })
                          .finalize(() -> app.releaseMapper(mapper))) break;
                /// Replace result file with temp one
                if (helper.expecting(() -> Files.move(tempFile.toPath(), file.toPath(),
                                                      StandardCopyOption.REPLACE_EXISTING),
                                     "Cannot replace file")
                          .resolve(() -> helper.expect(() -> {
                              Files.delete(file.toPath());
                              Files.delete(tempFile.toPath());
                          }, "Cannot dispose files"))) {
                    break;
                } else continue;

            } else if (command instanceof Messages.UpdateFile) {

                String filePath = ((Messages.UpdateFile) command).file();
                String checksum = ((Messages.UpdateFile) command).checksum();
                int size = ((Messages.UpdateFile) command).size()
                                                          .intValue();
                String username = ((Messages.UpdateFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                ServFileMapping fileMap;
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    fileMap = user.getFiles()
                                  .stream()
                                  .filter(f -> f.getPathname()
                                                .equals(filePath))
                                  .findFirst()
                                  .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline transfer
                    /// User doesn't have any files - decline transfer
                    /// File doesn't exist - decline transfer
                    out.println(Messages.NO);
                    continue;
                }
                out.println(Messages.OK);
                DriveFileMapper mapper = app.forceMapper(fileMap.getDrive());
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath
                );
                /// Create temporary file
                File tempFile = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath +
                        Configuration.TEMP_EXTENSION
                );
                if (helper.expecting(() -> {
                    tempFile.getParentFile()
                            .mkdirs();
                    tempFile.createNewFile();
                }, "Cannot create temporary file")
                          .resolve(() -> app.releaseMapper(mapper))) break;
                /// Download file - if fails release mapper and dispose created file
                if (helper.expecting(() -> FileManager.receiveFile(null, null, tempFile,
                                                                   byteIn, size, false),
                                     "Cannot receive file")
                          .resolve(() -> {
                              app.releaseMapper(mapper);
                              helper.expect(() -> Files.delete(tempFile.toPath()),
                                            "Cannot dispose files");
                          })) break;
                /// Replace result file with temp one and update mapping - if fails
                /// release mapper and dispose created file, in either case - release mapper
                if (helper.expecting(() -> {
                    Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    mapper.updateFile(file, checksum, username);
                }, "Cannot replace file")
                          .resolving(() -> helper.expect(() -> {
                              Files.delete(file.toPath());
                              Files.delete(tempFile.toPath());
                          }, "Cannot dispose files"))
                          .finalize(() -> app.releaseMapper(mapper))) {
                    break;
                } else continue;

            } else if (command instanceof Messages.DeleteFile) {

                String filePath = ((Messages.DeleteFile) command).file();
                String username = ((Messages.DeleteFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                ServFileMapping fileMap;
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    fileMap = user.getFiles()
                                  .stream()
                                  .filter(f -> f.getPathname()
                                                .equals(filePath))
                                  .findFirst()
                                  .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline query
                    /// User doesn't have any files - decline query
                    /// File doesn't exist - decline query
                    out.println(Messages.NO);
                    continue;
                }
                out.println(Messages.OK);
                DriveFileMapper mapper = app.forceMapper(fileMap.getDrive());
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath
                );
                /// Delete file and detach from mapping
                if (helper.expecting(() -> {
                    mapper.detachFile(file, username);
                    Files.delete(file.toPath());
                }, "Cannot delete file")
                          .resolving(Nothing::DO)
                          .finalize(() -> app.releaseMapper(mapper))) {
                    break;
                } else continue;

            } else if (command instanceof Messages.ShareFile) {

                String filePath = ((Messages.ShareFile) command).file();
                String receiver = ((Messages.ShareFile) command).receiver();
                String username = ((Messages.ShareFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                DriveMapping.User rec = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(receiver);
                try {
                    //noinspection ConstantConditions <- caught
                    if (rec.getShared_files()
                           .stream()
                           .anyMatch(f -> f.getPathname()
                                           .equals(filePath))) {
                        /// File is already shared - decline query
                        out.println(Messages.NO);
                        continue;
                    }
                } catch (NullPointerException ignore) {
                    /// Receiver doesn't exist - proceed
                    /// Receiver doesn't have any files - proceed
                }
                ServFileMapping fileMap;
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    fileMap = user.getFiles()
                                  .stream()
                                  .filter(f -> f.getPathname()
                                                .equals(filePath))
                                  .findFirst()
                                  .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline query
                    /// User doesn't have any files - decline query
                    /// File doesn't exist - decline query
                    out.println(Messages.NO);
                    continue;
                }
                out.println(Messages.OK);
                DriveFileMapper mapper = app.forceMapper(fileMap.getDrive());
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath
                );
                mapper.shareFile(file, username, receiver);
                Optional.ofNullable(app.getMessengers().get(receiver))
                        .ifPresent(m-> m.notifyShared(username, filePath));
                app.releaseMapper(mapper);
                continue;

            } else if (command instanceof Messages.UnshareFile) {

                String filePath = ((Messages.UnshareFile) command).file();
                String receiver = ((Messages.UnshareFile) command).receiver();
                String username = ((Messages.UnshareFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                DriveMapping.User rec = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(receiver);
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    rec.getShared_files()
                       .stream()
                       .filter(f -> f.getPathname()
                                     .equals(filePath))
                       .findFirst()
                       .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline query
                    /// User doesn't have any files - decline query
                    /// File doesn't exist - decline query
                    out.println(Messages.NO);
                    continue;
                }
                ServFileMapping fileMap;
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    fileMap = user.getFiles()
                                  .stream()
                                  .filter(f -> f.getPathname()
                                                .equals(filePath))
                                  .findFirst()
                                  .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline query
                    /// User doesn't have any files - decline query
                    /// File doesn't exist - decline query
                    out.println(Messages.NO);
                    /// Try removing sharing of file from all possible mappings
                    app.getMappersCopy()
                       .forEach(m -> m.unshareFile(new File(
                               m.getMapping()
                                .getDrive_location() +
                               File.separator +
                               username +
                               File.separator +
                               filePath), username, receiver));
                    continue;
                }
                out.println(Messages.OK);
//                TODO check origin of this call
//                Optional<ServFileMapping> fileMapping = user.getFiles()
//                                                            .stream()
//                                                            .filter(f -> f.getPathname()
//                                                                          .equals(filePath))
//                                                            .findFirst();
                DriveFileMapper mapper = app.forceMapper(fileMap.getDrive());
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath
                );
                mapper.unshareFile(file, username, receiver);
                Optional.ofNullable(app.getMessengers().get(receiver))
                        .ifPresent(m-> m.notifyUnshared(username, filePath));
                app.releaseMapper(mapper);
                continue;

            } else if (command instanceof Messages.RequestFile) {

                String filePath = ((Messages.RequestFile) command).file();
                String username = ((Messages.RequestFile) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                ServFileMapping fileMap;
                try {
                    //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- caught
                    fileMap = user.getFiles()
                                  .stream()
                                  .filter(f -> f.getPathname()
                                                .equals(filePath))
                                  .findFirst()
                                  .get();
                } catch (NullPointerException | NoSuchElementException ignored) {
                    /// User doesn't exist - decline query
                    /// User doesn't have any files - decline query
                    /// File doesn't exist - decline query
                    out.println(Messages.NO);
                    continue;
                }
                DriveFileMapper mapper = app.forceMapper(fileMap.getDrive());
                app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                  .getName()
                                                                  .split("_")[1]))
                   .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                File file = new File(
                        mapper.getMapping()
                              .getDrive_location() +
                        File.separator +
                        username +
                        File.separator +
                        filePath
                );
                out.println(new Messages.UpdateFile(fileMap.getDrive(),
                                                    filePath,
                                                    fileMap.getSize_bytes(),
                                                    fileMap.getChecksum()
                ));

                /// Send file
                if (helper.expecting(() -> FileManager.sendFile(null, null, file,
                                                                byteOut, (int) file.length(), false),
                                     "Cannot send file")
                          .resolving(Nothing::DO)
                          .finalize(() -> app.releaseMapper(mapper))) {
                    break;
                } else continue;

            } else if (command instanceof Messages.RequestMapping) {

                String username = command.user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                try {
                    @SuppressWarnings("ConstantConditions")// <- caught
                    List<FileMapping> files = Optional.ofNullable(user.getFiles())
                                                      .orElseGet(Collections::emptyList)
                                                      .stream()
                                                      .map(FileMapping::new)
                                                      .collect(Collectors.toList());
                    List<SharedFileMapping> sharedFiles = Optional.ofNullable(user.getShared_files())
                                                                  .orElseGet(Collections::emptyList)
                                                                  .stream()
                                                                  .map(f -> new SharedFileMapping(f, f.getOwner()))
                                                                  .collect(Collectors.toList());
                    out.println(Messages.OK);
                    AtomicReference<String> serializedFiles = new AtomicReference<>();
                    /// Ty to serialize files
                    if (helper.expect(() -> serializedFiles.set(FileManager.toString(
                            Pair.of(files, sharedFiles))
                                      ), "Cannot serialize mapping")) break;
                    out.println(new Messages.UpdateFile("null",
                                                        Messages.RequestMapping.cmd,
                                                        (long) serializedFiles.get()
                                                                              .length(),
                                                        "null"));
                    /// Send mapping
                    if (helper.expect(() -> FileManager.sendString(serializedFiles.get(), byteOut, false),
                                      "Cannot send mapping")) {
                        break;
                    } else continue;
                } catch (NullPointerException ignored) {
                    /// User doesn't exist - decline transfer
                    out.println(Messages.NO_USER);
                    continue;
                }

            } else if (command instanceof Messages.RequestUsers) {

                String username = command.user();
                ArrayList<String> names = new UserMappingCollector(app.getMappersCopy()).collectUserNames();
                names.remove(username);
                out.println(Messages.OK);
                AtomicReference<String> serializedUsers = new AtomicReference<>();
                /// Ty to serialize users
                if (helper.expect(() -> serializedUsers.set(FileManager.toString(names)),
                                  "Cannot serialize user list")) break;
                out.println(new Messages.UpdateFile("null",
                                                    Messages.RequestMapping.cmd,
                                                    (long) serializedUsers.get().length(),
                                                    "null"));
                /// Send users
                if (helper.expect(() -> FileManager.sendString(serializedUsers.get(), byteOut, false),
                                  "Cannot send user list")) {
                    break;
                } else continue;

            } else if (command instanceof Messages.RequestReceivers) {

                String filePath = ((Messages.RequestReceivers) command).file();
                String username = ((Messages.RequestReceivers) command).user();
                DriveMapping.User user = new UserMappingCollector(app.getMappersCopy()).collectUserMapping(username);
                try {
                    @SuppressWarnings("ConstantConditions")// <- caught
                            List<ServFileMapping> files = Optional.ofNullable(user.getFiles())
                                                                  .orElseGet(Collections::emptyList);
                    if (files.isEmpty()) {
                        /// No files found
                        out.println(Messages.NO);
                        continue;
                    }
                    /// Get possible file
                    Optional<ServFileMapping> fileMapping = files.stream()
                                                                 .filter(f -> f.getPathname()
                                                                               .equals(filePath))
                                                                 .findFirst();
                    if (!fileMapping.isPresent()) {
                        /// Related file not found
                        out.println(Messages.NO);
                        continue;
                    }
                    out.println(Messages.OK);
                    DriveFileMapper mapper = app.forceMapper(fileMapping.get().getDrive());
                    app.getCommandFields().get(Integer.parseInt(mapper.getMapping()
                                                                      .getName()
                                                                      .split("_")[1]))
                       .append("< " + LogHelper.date() + " " + command.toProperString() + " >\n");
                    /// Create list of receivers
                    ArrayList<String> receivers = mapper.getMapping()
                                                        .getUsers()
                                                        .stream()
                                                        .filter(u -> Optional.ofNullable(u.getShared_files())
                                                                             .orElseGet(Collections::emptyList)
                                                                             .stream()
                                                                             .filter(f -> f.getOwner()
                                                                                           .equals(username))
                                                                             .anyMatch(f -> f.getPathname()
                                                                                             .equals(filePath)))
                                                        .map(DriveMapping.User::getUsername)
                                                        .collect(Collectors.toCollection(ArrayList::new));
                    AtomicReference<String> serializedReceivers = new AtomicReference<>();
                    /// Try to serialize receivers
                    if (helper.expecting(() -> serializedReceivers.set(FileManager.toString(
                            receivers)), "Cannot serialize receivers list")
                              .resolve(() -> app.releaseMapper(mapper))) break;
                    out.println(new Messages.UpdateFile("null",
                                                        Messages.RequestReceivers.cmd,
                                                        (long) serializedReceivers.get()
                                                                                  .length(),
                                                        "null"));
                    /// Send receivers
                    if (helper.expecting(() -> FileManager.sendString(serializedReceivers.get(), byteOut, false),
                                         "Cannot send receivers list")
                              .resolving(Nothing::DO).finalize(() -> app.releaseMapper(mapper))) {
                        break;
                    } else continue;

                } catch (NullPointerException ignored) {
                    /// User doesn't exist - decline transfer
                    out.println(Messages.NO_USER);
                    continue;
                }

            } else if (command instanceof Messages.FinishConnection) {

                break;

            }
        }
        helper.expect(socket::close, "Cannot close socket");
        System.out.println("closed");
    }

    private void reload(FileTree tree, DriveFileMapper mapper) {
        tree.reload(mapper.getMapping()
                          .getUsers()
                          .stream()
                          .map((Function<DriveMapping.User, List<SharedFileMapping>>) u -> {
                              if (u.getFiles() != null)
                                  return u.getFiles()
                                          .stream()
                                          .map(f -> new SharedFileMapping(f, u.getUsername()))
                                          .collect(Collectors.toList());
                              else return Collections.emptyList();
                          })
                          .flatMap(List::stream)
                          .collect(Collectors.toList()),
                    mapper.getMapping().getName());
    }
}
