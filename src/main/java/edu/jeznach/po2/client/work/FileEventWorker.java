package edu.jeznach.po2.client.work;

import edu.jeznach.po2.client.file.ClientFileMapper;
import edu.jeznach.po2.client.file.ClientFileMapping;
import edu.jeznach.po2.client.file.ClientMapping;
import edu.jeznach.po2.common.communication.Messages;
import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.file.FileManager;
import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.FileObserver;
import edu.jeznach.po2.common.gui.FileTree;
import edu.jeznach.po2.common.gui.NotificationSender;
import edu.jeznach.po2.common.gui.OperationLabel;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.BreakableInputStream;
import edu.jeznach.po2.common.util.LogHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static edu.jeznach.po2.client.file.ClientMapping.Directories.*;

public class FileEventWorker extends Thread {

    private Log log;
    private LogHelper helper;
    private Socket socket;
    private PrintStream out;
    private BreakableInputStream in;
    private String username;
    private FileObserver fileObserver;
    private ClientFileMapper mapper;
    private FileTree fileTree;
    private FileTree sharedTree;
    private String clientPath;
    private NotificationSender sender;
    private OperationLabel label;

    public FileEventWorker(@NotNull Log log,
                           @NotNull Socket socket,
                           @NotNull String username,
                           @NotNull FileObserver fileObserver,
                           @NotNull ClientFileMapper mapper,
                           @NotNull FileTree fileTree,
                           @NotNull FileTree sharedTree,
                           @NotNull String clientPath,
                           NotificationSender sender,
                           OperationLabel label) throws IOException {
        this.helper = new LogHelper(this.log = log);
        this.socket = socket;
        this.label = label;
        this.socket.setSoTimeout(24_000);
        this.out = new PrintStream(socket.getOutputStream());
        this.in = new BreakableInputStream(socket.getInputStream());
        this.username = username;
        this.fileObserver = fileObserver;
        this.mapper = mapper;
        this.fileTree = fileTree;
        this.sharedTree = sharedTree;
        this.clientPath = clientPath;
        this.sender = sender;
    }

//    @Override
//    public void run() {
//        while (true) {
//            FileObserver.FileEvent event;
//            try {
//                System.out.println("e");
//                event = fileObserver.popEvent();
//                System.out.println("E");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                break;
//            }
//            File absoluteFile;
//            try {
//                System.out.println(">" + event.toString());
//                switch (event.eventType) {
//                    case Create_Node: {
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        String checksum = FileManager.getChecksum(absoluteFile);
//                        boolean attached = mapper.attachFile(absoluteFile,
//                                                             checksum,
//                                                             files.toString());
//                        if (!attached)
//                            if (mapper.getMapping()
//                                      .getUsed_space_bytes() >
//                                Configuration.SIZE_PER_USER$MB * 1_000_000) {
//                                mapper.detachFile(absoluteFile, files.toString());
//                                Files.move(absoluteFile.toPath(),
//                                           getAbsoluteFile(event.filePath.toString(), cancel).toPath());
//                                this.log.fileRejected(username, absoluteFile);
//                                label.idle("Waiting", "...");
//                                break;
//                            }
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        out.println(new Messages.CreateFile(username, event.filePath.toString(), absoluteFile.length(),
//                                                            checksum));
//                        String response;
//                        try { response = in.readLine(); } catch (SocketTimeoutException e) { response = ""; }
//                        if (response.equals(Messages.NO)) {
//                            log.exception(new IOException("File found"));
//                            label.idle("Waiting", "...");
//                            break;
//                        }
//                        FileManager.sendFile(label, event.eventType.toString(), absoluteFile, socket.getOutputStream(),
//                                             (int) absoluteFile.length(), true);
//                        label.idle("Waiting", "...");
//                        break;
//                    }
//                    case Update_Node: {
//                        label.up(event.eventType.toString() + " 0%", event.filePath.toString());
//                        System.out.println("up " + event.filePath);
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        String checksum = FileManager.getChecksum(absoluteFile);
//                        System.out.println("1 " + event.filePath);
//                        Optional<ClientFileMapping> existing =
//                                Optional.ofNullable(mapper.getMapping()
//                                                          .getFiles())
//                                        .orElse(Collections.emptyList())
//                                        .stream()
//                                        .filter(f -> f.getPathname()
//                                                      .equals(event.filePath.toString()))
//                                        .findFirst();
//                        System.out.println("2 " + event.filePath);
//                        if (!existing.isPresent()) {
//                            log.exception(new IOException("File not found"));
//                            break;
//                        } else {
//                            System.out.println("3 " + event.filePath);
//                            if (mapper.getMapping()
//                                      .getUsed_space_bytes() -
//                                existing.get()
//                                        .getSize_bytes() +
//                                absoluteFile.length() > Configuration.SIZE_PER_USER$MB * 1_000_000) {
//                                mapper.detachFile(absoluteFile, files.toString());
//                                Files.move(absoluteFile.toPath(),
//                                           getAbsoluteFile(event.filePath.toString(), cancel).toPath());
//                                this.log.fileRejected(username, absoluteFile);
//                                break;
//                            }
//                        }
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        out.println(new Messages.UpdateFile(username, event.filePath.toString(), absoluteFile.length(),
//                                                            checksum));
//                        String response;
//                        try { response = in.readLine(); } catch (SocketTimeoutException e) { response = ""; }
//                        if (response.equals(Messages.NO)) {
//                            log.exception(new IOException("File not found"));
//                            break;
//                        }
//                        FileManager.sendFile(label, event.eventType.toString(), absoluteFile, socket.getOutputStream(),
//                                             (int) absoluteFile.length(), true);
//                        break;
//                    }
//                    case Delete_Node: {
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        out.println(new Messages.DeleteFile(username, event.filePath.toString()));
//                        mapper.detachFile(absoluteFile,
//                                          files.toString());
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        break;
//                    }
//                    case Devour_Node: {
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        absoluteFile.getParentFile().mkdirs();
//                        absoluteFile.createNewFile();
//                        out.println(new Messages.RequestFile(username, event.filePath.toString()));
//                        String response;
//                        try { response = in.readLine(); } catch (SocketTimeoutException e) { response = ""; }
//                        if (response.equals(Messages.NO)) {
//                            log.exception(new IOException("File not found"));
//                            break;
//                        }
//                        Messages.UpdateFile fileSpec = (Messages.UpdateFile) Messages.parseMessage(response);
//                        if (fileSpec == null) {
//                            log.exception(new IOException("Command unrecognised"));
//                            break;
//                        }
//                        long size = fileSpec.size();
//                        FileManager.receiveFile(label, event.eventType.toString(), absoluteFile,
//                                                socket.getInputStream(), (int) size, mapper,
//                                                files.toString(), true, true);
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        break;
//                    }
//                    case Revise_Node: {
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        out.println(new Messages.RequestFile(username, event.filePath.toString()));
//                        String response;
//                        try { response = in.readLine(); } catch (SocketTimeoutException e) { response = ""; }
//                        if (response.equals(Messages.NO)) {
//                            log.exception(new IOException("File not found"));
//                            break;
//                        }
//                        Messages.UpdateFile fileSpec = (Messages.UpdateFile) Messages.parseMessage(response);
//                        if (fileSpec == null) {
//                            log.exception(new IOException("Command unrecognised"));
//                            break;
//                        }
//                        long size = fileSpec.size();
//                        FileManager.receiveFile(label, event.eventType.toString(), absoluteFile,
//                                                socket.getInputStream(), (int) size, mapper,
//                                                files.toString(), false, true);
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        break;
//                    }
//                    case Remove_Node: {
//
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        mapper.detachFile(absoluteFile,
//                                          files.toString());
//                        SwingUtilities.invokeLater(() -> reload(fileTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.driveDirectory));
//                        Files.delete(absoluteFile.toPath());
//                        break;
//                    }
//                    case Couple_Node: {
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), shared);
//                        String receiver = event.filePath.subpath(0, 1)
//                                                        .toString();
//                        absoluteFile.getParentFile().mkdirs();
//                        absoluteFile.createNewFile();
//                        out.println(new Messages.RequestFile(receiver, event.filePath.toString()));
//                        String response;
//                        try { response = in.readLine(); } catch (SocketTimeoutException e) { response = ""; }
//                        if (response.equals(Messages.NO)) {
//                            log.exception(new IOException("File not found"));
//                            break;
//                        }
//                        Messages.UpdateFile fileSpec = (Messages.UpdateFile) Messages.parseMessage(response);
//                        if (fileSpec == null) {
//                            log.exception(new IOException("Command unrecognised"));
//                            break;
//                        }
//                        long size = fileSpec.size();
//                        FileManager.receiveFile(label, event.eventType.toString(), absoluteFile,
//                                                socket.getInputStream(), (int) size, mapper,
//                                                shared.toString(), true, true);
//                        SwingUtilities.invokeLater(() -> reload(sharedTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.sharedDirectory));
//                        break;
//                    }
//                    case Unlink_Node: {
//
//                        absoluteFile = getAbsoluteFile(event.filePath.toString(), files);
//                        String receiver = event.filePath.subpath(0, 1)
//                                                        .toString();
//                        mapper.detachFile(absoluteFile,
//                                          shared.toString());
//                        SwingUtilities.invokeLater(() -> reload(sharedTree,
//                                                                mapper.getMapping(),
//                                                                FileTree.Type.sharedDirectory));
//                        Files.delete(absoluteFile.toPath());
//                        break;
//                    }
//                }
//            } catch (FileNotFoundException e) {
//                log.exception(e);
//            } catch (IOException e) {
//                log.exception(e);
//                System.exit(-0x10E);
//            } catch (Exception e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//    }

    @Override
    @SuppressWarnings({"UnnecessaryContinue", "ResultOfMethodCallIgnored"})// <1- clarifying
    public void run() {
        while (true) {
            AtomicReference<FileObserver.FileEvent> ev = new AtomicReference<>();
            if (helper.expect(() -> ev.set(fileObserver.popEvent()),
                              "Cannot pop FileEvent")) break;
            FileObserver.FileEvent event = ev.get();
            File absoluteFile;
            String filePath = event.filePath.toString();
            if (event.eventType == FileObserver.FileEvent.Type.Create_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                AtomicReference<String> checksum = new AtomicReference<>();
                if (helper.expect(() -> checksum.set(FileManager.getChecksum(absoluteFile)),
                                  "Cannot calculate Checksum")) break;
                try {
                    long size;
                    /// Check if file is already attached - if not add size to calculate overflow
                    //noinspection ConstantConditions <- caught
                    if (mapper.getMapping().getFiles().stream().anyMatch(f -> f.getPathname().equals(filePath))) {
                        size = mapper.getMapping().getUsed_space_bytes();
                    } else {
                        size = mapper.getMapping().getUsed_space_bytes() + absoluteFile.length();
                    }
                    /// Check if size overflows storage limit - if so reject file
                    if (size > Configuration.SIZE_PER_USER$MB * 1_000_000) {
                        /// Log and notify event
                        log.fileRejected(username, absoluteFile);
                        Log.Message msg = Log.fileRejectedMsg(username, absoluteFile);
                        sender.error(msg.title, msg.description);
                        if (helper.expect(() -> {
                            Files.move(absoluteFile.toPath(),
                                       getAbsoluteFile(filePath, cancel).toPath());
                            mapper.attachFile(absoluteFile, checksum.get(), cancel.toString());
                        }, "Cannot move file to rejected")) break;
                        continue;
                    } else if (size > Configuration.SIZE_PER_USER$MB * 1_000_000 * 0.9) {
                        double usage = size / (Configuration.SIZE_PER_USER$MB * 1_000_000 * 0.9);
                        double mb = size / 1_000_000.0;
                        sender.warning(
                                "Approaching storage limit",
                                "Usage: " + String.format("%.2f", usage) +
                                " (" + String.format("%.3f", mb) + "MB of " +
                                Configuration.SIZE_PER_USER$MB + "MB)"
                        );
                    }
                } catch (NullPointerException ignored) {
                    /// No files mapped - proceed as used size is 0
                }
                /// Attach file and reload FileTree
                mapper.attachFile(absoluteFile, checksum.get(), files.toString());
                SwingUtilities.invokeLater(() -> reload(fileTree,
                                                        mapper.getMapping(),
                                                        FileTree.Type.driveDirectory));
                /// Send command and wait for response
                out.println(new Messages.CreateFile(username, event.filePath.toString(), absoluteFile.length(),
                                                    checksum.get()));
                AtomicReference<String> response = new AtomicReference<>();

                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                /// Check if response is NO, if so - break
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot perform sending file", new IOException("File already exists"));
                    break;
                }
                /// Send file
                if (helper.expect(() -> {
                    FileManager.sendFile(label, event.eventType.toString(),
                                         absoluteFile, socket.getOutputStream(),
                                         (int) absoluteFile.length(), true);
                    label.idle();
                }, "Cannot send file")) {
                    break;
                } else continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Update_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                AtomicReference<String> checksum = new AtomicReference<>();
                if (helper.expect(() -> checksum.set(FileManager.getChecksum(absoluteFile)),
                                  "Cannot calculate Checksum")) break;
                /// Try to retrieve existing mapping
                AtomicReference<FileMapping> fileMapping = new AtomicReference<>();
                //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- expected
                if (helper.expect(() -> fileMapping.set(mapper.getMapping()
                                                              .getFiles()
                                                              .stream()
                                                              .filter(f -> f.getPathname()
                                                                            .equals(filePath))
                                                              .findFirst()
                                                              .get()),
                                  "Cannot find file mapping to update")) break;
                /// Check if size overflows storage limit - if so reject file
                long size = mapper.getMapping().getUsed_space_bytes();
                if (size - fileMapping.get().getSize_bytes() + absoluteFile.length() >
                    Configuration.SIZE_PER_USER$MB * 1_000_000) {
                    /// Log and notify event
                    log.fileRejected(username, absoluteFile);
                    Log.Message msg = Log.fileRejectedMsg(username, absoluteFile);
                    sender.error(msg.title, msg.description);
                    if (helper.expect(() -> {
                        Files.move(absoluteFile.toPath(),
                                   getAbsoluteFile(filePath, cancel).toPath());
                        mapper.attachFile(absoluteFile, checksum.get(), cancel.toString());
                    }, "Cannot move file to rejected")) break;
                    continue;
                } else if (size > Configuration.SIZE_PER_USER$MB * 1_000_000 * 0.9) {
                    double usage = size / (Configuration.SIZE_PER_USER$MB * 1_000_000 * 0.9);
                    double mb = size / 1_000_000.0;
                    sender.warning(
                            "Approaching storage limit",
                            "Usage: " + String.format("%.2f", usage) +
                            " (" + String.format("%.3f", mb) + "MB of " +
                            Configuration.SIZE_PER_USER$MB + "MB)"
                    );
                }
                /// Send command and wait for response
                out.println(new Messages.UpdateFile(username, event.filePath.toString(), absoluteFile.length(),
                                                    checksum.get()));
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                /// Check if response is NO, if so - break
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot perform sending file", new IOException("File doesn't exists"));
                    break;
                }
                /// Send file
                if (helper.expect(() -> {
                    FileManager.sendFile(label, event.eventType.toString(),
                                         absoluteFile, socket.getOutputStream(),
                                         (int) absoluteFile.length(), true);
                    label.idle();
                }, "Cannot send file")) {
                    break;
                } else continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Delete_Node) {

                /// Send command and wait for response
                out.println(new Messages.DeleteFile(username, event.filePath.toString()));
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                /// Check if response is NO, if so - break
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot perform deleting file", new IOException("File doesn't exists"));
                    break;
                } else {
                    label.idle();
                    continue;
                }

            } else if (event.eventType == FileObserver.FileEvent.Type.Assign_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                AtomicReference<ClientFileMapping> fileMapping = new AtomicReference<>();
                //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- expected
                if (helper.expect(() -> fileMapping.set(mapper.getMapping()
                                                              .getFiles()
                                                              .stream()
                                                              .filter(f -> f.getPathname()
                                                                            .equals(filePath))
                                                              .findFirst()
                                                              .get()),
                                  "Cannot find file mapping to share")) break;
                /// Try to send command
                //noinspection ConstantConditions <- expected
                if (helper.expect(() -> out.println(new Messages.ShareFile(username,
                                                                           filePath,
                                                                           event.meta())),
                                                    "No receiver was provided")) break;
                /// Wait for response
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                /// Check if response is NO, if so - break
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot share file", new IOException("File doesn't exist or is already shared"));
                    break;
                } else {
                    ClientFileMapping clientFileMapping = fileMapping.get();
                    List<String> receivers = clientFileMapping.getReceivers();
                    if (receivers == null) {
                        clientFileMapping.setReceivers(new ArrayList<>());
                        receivers = clientFileMapping.getReceivers();
                    }
                    receivers.add(event.meta());
                    label.idle();
                    continue;
                }

            } else if (event.eventType == FileObserver.FileEvent.Type.Refuse_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                AtomicReference<ClientFileMapping> fileMapping = new AtomicReference<>();
                //noinspection ConstantConditions,OptionalGetWithoutIsPresent <- expected
                if (helper.expect(() -> fileMapping.set(mapper.getMapping()
                                                              .getFiles()
                                                              .stream()
                                                              .filter(f -> f.getPathname()
                                                                            .equals(filePath))
                                                              .findFirst()
                                                              .get()),
                                  "Cannot find file mapping to unshare")) break;
                /// Try to send command
                //noinspection ConstantConditions <- expected
                if (helper.expect(() -> out.println(new Messages.UnshareFile(username,
                                                                             filePath,
                                                                             event.meta())),
                                  "No receiver was provided")) break;
                /// Wait for response
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                /// Check if response is NO, if so - break
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot unshare file", new IOException("File doesn't exist or is already unshared"));
                    break;
                } else {
//                    ClientFileMapping clientFileMapping = fileMapping.get();
//                    List<String> receivers = clientFileMapping.getReceivers();
//                    if (receivers == null) {
//                        log.exception("Cannot unshare file", new IOException("File doesn't exist or is already unshared"));
//                        break;
//                    }
//                    receivers.remove(event.meta());
                    label.idle();
                    continue;
                }

            } else if (event.eventType == FileObserver.FileEvent.Type.Devour_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                /// Send command and wait for response
                 out.println(new Messages.RequestFile(username, filePath));
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                int size;
                /// Check if response is NO, if so - break, if isn't parse
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot download file", new IOException("Remote file doesn't exist"));
                    break;
                } else {
                    Messages.Msg msg = Messages.parseMessage(response.get());
                    AtomicReference<Long> s = new AtomicReference<>();
                    //noinspection ConstantConditions <- expected
                    if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                                      "No size provided in response")) break;
                    size = s.get().intValue();
                }
                /// Create result file
                if (helper.expect(absoluteFile::createNewFile,
                                  "Cannot create result file")) break;
                /// Create temporary file
                File tempFile = new File(
                        absoluteFile.getAbsolutePath() + Configuration.TEMP_EXTENSION
                );
                if (helper.expecting(tempFile::createNewFile,
                                     "Cannot create temporary file")
                          .resolve(() -> helper.expect(() -> {
                              Files.delete(absoluteFile.toPath());
                              mapper.detachFile(absoluteFile, files.toString());
                              }, "Cannot dispose files"))) break;
                /// Receive, replace and attach file and reload fileTree
                if (helper.expecting(() -> {
                    String checksum = FileManager.receiveFile(label, event.eventType.toString(), tempFile,
                                                              socket.getInputStream(), size, true);
                    mapper.attachFile(absoluteFile, checksum, files.toString());
                    Files.move(tempFile.toPath(), absoluteFile.toPath(),
                               StandardCopyOption.REPLACE_EXISTING);
                    SwingUtilities.invokeLater(() -> reload(fileTree,
                                                            mapper.getMapping(),
                                                            FileTree.Type.driveDirectory));
                    label.idle();
                }, "Cannot receive file").resolve(() -> helper.expect(() -> {
                    Files.delete(absoluteFile.toPath());
                    Files.delete(tempFile.toPath());
                    mapper.detachFile(absoluteFile, files.toString());
                }, "Cannot dispose files"))) {
                    break;
                } else continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Revise_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                out.println(new Messages.RequestFile(username, filePath));
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                int size;
                /// Check if response is NO, if so - break, if isn't parse
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot download file", new IOException("Remote file doesn't exist"));
                    break;
                } else {
                    Messages.Msg msg = Messages.parseMessage(response.get());
                    AtomicReference<Long> s = new AtomicReference<>();
                    //noinspection ConstantConditions <- expected
                    if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                                      "No size provided in response")) break;
                    size = s.get().intValue();
                }
                /// Create temporary file
                File tempFile = new File(
                        absoluteFile.getAbsolutePath() + Configuration.TEMP_EXTENSION
                );
                if (helper.expecting(tempFile::createNewFile,
                                     "Cannot create temporary file")
                          .resolve(() -> helper.expect(() -> {
                              Files.delete(absoluteFile.toPath());
                              mapper.detachFile(absoluteFile, files.toString());
                          }, "Cannot dispose files"))) break;
                /// Receive, replace and update file
                if (helper.expecting(() -> {
                    String checksum = FileManager.receiveFile(label, event.eventType.toString(), tempFile,
                                                              socket.getInputStream(), size, true);
                    Files.move(tempFile.toPath(), absoluteFile.toPath(),
                               StandardCopyOption.REPLACE_EXISTING);
                    mapper.updateFile(absoluteFile, checksum, files.toString());
                    label.idle();
                }, "Cannot receive file").resolve(
                        () -> helper.expect(() -> Files.delete(tempFile.toPath()),
                                            "Cannot dispose files"))) {
                    break;
                } else continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Remove_Node) {

                absoluteFile = getAbsoluteFile(filePath, files);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                if (helper.expect(() -> Files.delete(absoluteFile.toPath()),
                                  "Cannot delete file")) break;
                mapper.detachFile(absoluteFile, files.toString());
                SwingUtilities.invokeLater(() -> reload(fileTree,
                                                        mapper.getMapping(),
                                                        FileTree.Type.driveDirectory));
                label.idle();
                continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Couple_Node) {

                absoluteFile = getAbsoluteFile(event.meta() + File.separator + filePath,
                                               shared);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                /// Send command and wait for response
                //noinspection ConstantConditions <- expected
                if (helper.expect(() -> out.println(new Messages.RequestFile(event.meta(), filePath)),
                                  "No receiver was provided")) break;
                AtomicReference<String> response = new AtomicReference<>();
                if (helper.expect(() -> response.set(new String(in.readLine())),
                                  "Server not responding")) break;
                int size;
                /// Check if response is NO, if so - break, if isn't parse
                if (response.get().equals(Messages.NO)) {
                    log.exception("Cannot download file", new IOException("Remote file doesn't exist or is already unshared"));
                    break;
                } else {
                    Messages.Msg msg = Messages.parseMessage(response.get());
                    AtomicReference<Long> s = new AtomicReference<>();
                    //noinspection ConstantConditions <- expected
                    if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                                      "No size provided in response")) break;
                    size = s.get().intValue();
                }
                /// Create result file
                if (helper.expect(() -> {
                    absoluteFile.getParentFile().mkdirs();
                    absoluteFile.createNewFile();
                }, "Cannot create result file")) break;
                /// Create temporary file
                File tempFile = new File(
                        absoluteFile.getAbsolutePath() + Configuration.TEMP_EXTENSION
                );
                if (helper.expecting(tempFile::createNewFile,
                                     "Cannot create temporary file")
                          .resolve(() -> helper.expect(() -> {
                              Files.delete(absoluteFile.toPath());
                              mapper.detachFile(absoluteFile, files.toString());
                          }, "Cannot dispose files"))) break;
                /// Receive, replace and attach file and reload fileTree
                if (helper.expecting(() -> {
                    String checksum = FileManager.receiveFile(label, event.eventType.toString(), tempFile,
                                                              socket.getInputStream(), size, true);
                    mapper.attachFile(absoluteFile, checksum, shared.toString());
                    Files.move(tempFile.toPath(), absoluteFile.toPath(),
                               StandardCopyOption.REPLACE_EXISTING);
                    SwingUtilities.invokeLater(() -> reload(sharedTree,
                                                            mapper.getMapping(),
                                                            FileTree.Type.sharedDirectory));
                    label.idle();
                }, "Cannot receive file").resolve(() -> helper.expect(() -> {
                    Files.delete(absoluteFile.toPath());
                    Files.delete(tempFile.toPath());
                    mapper.detachFile(absoluteFile, shared.toString());
                }, "Cannot dispose files"))) {
                    break;
                } else continue;

            } else if (event.eventType == FileObserver.FileEvent.Type.Unlink_Node) {

                absoluteFile = getAbsoluteFile(event.meta() + File.separator + filePath,
                                               shared);
                label.up(event.eventType.toString() + " initializing...", absoluteFile.getAbsolutePath());
                if (helper.expect(() -> Files.delete(absoluteFile.toPath()),
                                  "Cannot delete file")) break;
                if (helper.expect(() -> mapper.detachFile(
                        absoluteFile,
                        shared.toString()
                ), "Cannot unshare file")) break;
                SwingUtilities.invokeLater(() -> reload(sharedTree,
                                                        mapper.getMapping(),
                                                        FileTree.Type.sharedDirectory));
                label.idle();
                continue;

            }
        }
        helper.expect(() -> socket.close(), "Cannot close socket");
        label.crash();
    }

    private void reload(FileTree tree, ClientMapping mapper, FileTree.Type type) {
        if (type.equals(FileTree.Type.userDirectory)) {
            tree.reload(mapper.getFiles(),
                        mapper.getName());
        } else if (type.equals(FileTree.Type.sharedDirectory)) {
            tree.reload(mapper.getShared_files(),
                        "Shared files");
        }
    }

    private File getAbsoluteFile(String file, ClientMapping.Directories dir) {
        return new File(clientPath +
                 File.separator +
                 dir.toString() +
                 File.separator + file);
    }
}
