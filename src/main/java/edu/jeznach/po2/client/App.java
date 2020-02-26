package edu.jeznach.po2.client;

import edu.jeznach.po2.client.communication.Messenger;
import edu.jeznach.po2.client.file.ClientFileMapper;
import edu.jeznach.po2.client.file.ClientFileMapping;
import edu.jeznach.po2.client.file.ClientMapping;
import edu.jeznach.po2.client.gui.NotificationSender;
import edu.jeznach.po2.client.work.FileEventWorker;
import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.FileObserver;
import edu.jeznach.po2.common.file.SharedFileMapping;
import edu.jeznach.po2.common.gui.FileTree;
import edu.jeznach.po2.common.gui.ObjectMouseAdapter;
import edu.jeznach.po2.common.gui.OperationLabel;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.GuiComponents;
import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static edu.jeznach.po2.common.file.FileObserver.FileEvent.Type.*;

public class App extends JFrame {

    public static final String USER_MAPPING_YAML = "user.yaml";
    private static final String LOG_NAME = "log.txt";
    private @NotNull ClientFileMapper mapper;
    public synchronized @NotNull ClientFileMapper getMapper() { return this.mapper; }
    public synchronized void setMapper(@NotNull ClientFileMapper mapper) { this.mapper = mapper; }
    private @NotNull NotificationSender notificationSender;
    public synchronized @NotNull NotificationSender  getNotificationSender() { return this.notificationSender; }
    private @NotNull Log log;
    public synchronized @NotNull Log getLog() { return this.log; }
    private @NotNull FileObserver observer;
    public synchronized @NotNull FileObserver getObserver() { return this.observer; }
    private @NotNull Messenger messenger;
    public synchronized @NotNull Messenger getMessenger() { return this.messenger; }

    private final App app;

    /**
     * Mouse adapter used to share/unshare files via {@link FileTree}
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ObjectMouseAdapter<JTree> listener = new ObjectMouseAdapter<JTree>() {
        @Override
        public void mouseClicked(MouseEvent e) {
            /// Double click
            if (e.getClickCount() == 2) {
                String filePath =
                        Arrays.stream(
                                getObject().getClosestPathForLocation(e.getX(), e.getY())
                                           .getPath())
                              .map(Object::toString)
                              .filter(s -> s.startsWith("d:") || s.startsWith("f:"))
                              .map(s -> s.substring(2))
                              .collect(Collectors.joining(File.separator));
                /// Left click - share
                if (e.getButton() == MouseEvent.BUTTON1) {
                    List<String> users = messenger.requestUsers();
                    if (users == null) {
                        JOptionPane.showMessageDialog(app,
                                                      "Cannot share file: server not responding!",
                                                      "Share file",
                                                      JOptionPane.WARNING_MESSAGE);
                    } else if (users.size() <= 0) {
                        JOptionPane.showMessageDialog(app,
                                                      "Cannot share file: no other users found!",
                                                      "Share file",
                                                      JOptionPane.WARNING_MESSAGE);
                    } else {
                        String choice = (String) JOptionPane.showInputDialog(
                                app,
                                "Select user to share file to:",
                                "Share file",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                users.toArray(),
                                null
                        );
                        if (choice != null && !choice.isEmpty()) {
                            observer.addEvent(Paths.get(filePath), Assign_Node, choice);
                        }
                    }
                /// Right click - unshare
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    List<String> receivers = messenger.requestReceivers(filePath);
                    if (receivers == null) {
                        JOptionPane.showMessageDialog(app,
                                                      "Cannot unshare file: server not responding!",
                                                      "Unshare file",
                                                      JOptionPane.WARNING_MESSAGE);
                    } else if (receivers.size() <= 0) {
                        JOptionPane.showMessageDialog(app,
                                                      "Cannot unshare file: no receivers attached!",
                                                      "Unshare file",
                                                      JOptionPane.WARNING_MESSAGE);
                    } else {
                        String choice = (String) JOptionPane.showInputDialog(
                                app,
                                "Select user to cancel sharing to:",
                                "Unshare file",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                receivers.toArray(),
                                null
                        );
                        if (choice != null && !choice.isEmpty()) {
                            observer.addEvent(Paths.get(filePath), Refuse_Node, choice);
                        }
                    }
                }
            }
        }
    };

    @SuppressWarnings("DuplicateThrows")
    public App(@NotNull String username, @NotNull String userPath) throws FileNotFoundException, IOException {
        this.app = this;
        /// Pull mapping based on existing files (loading is inefficient due to comparision with server
        Pair<ClientMapping, Boolean> newMappingResult = ClientFileMapper.provider.createStructure(
                new File(USER_MAPPING_YAML),
                new ClientMapping.InitParams(
                        username,
                        LOG_NAME,
                        new File(userPath)
                )
        );

        /// Delete all old temporary files ?

        /// Create GUI and show
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        this.getContentPane().add(panel);

        FileTree fileTree = new FileTree(
                newMappingResult.key()
                                .getFiles() == null ?
                        Collections.emptyList() :
                        newMappingResult.key()
                                        .getFiles(),
                username,
                FileTree.Type.userDirectory,
                listener
        );
        panel.add(fileTree);
        FileTree sharedTree = new FileTree(
                newMappingResult.key().getShared_files() == null ?
                        Collections.emptyList() :
                        newMappingResult.key().getShared_files(),
                "Shared files",
                FileTree.Type.sharedDirectory,
                null
        );
        panel.add(sharedTree);

        /// Initialize all possible fields
        this.log = new Log(new File(userPath + File.separator + LOG_NAME));
        this.notificationSender = new NotificationSender(log);
        this.observer = new FileObserver(Paths.get(userPath).resolve(ClientMapping.Directories.files.toString()), log);
        this.observer.start();
        Socket messengerSendingSocket = new Socket(Configuration.SERVER_ADDRESS, Configuration.SERVER_PORT);
        Socket messengerReceivingSocket = new Socket(Configuration.SERVER_ADDRESS, Configuration.COMMUNICATION_PORT);
        this.messenger = new Messenger(this.log, messengerSendingSocket, messengerReceivingSocket, username, this.observer,
                                       this.notificationSender);
        if (newMappingResult.value())
            notificationSender.information("Created new mapping", "User not registered in this directory yet");
        this.mapper = new ClientFileMapper(newMappingResult.key(), new File(userPath +
                                                                            File.separator +
                                                                            USER_MAPPING_YAML));

        /// Initialize GUI with some empty values
        JPanel jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(340, 580));
        jPanel.setMinimumSize(new Dimension(340, 580));
        JScrollPane scrollPane = new JScrollPane();
        JPanel bigPanel = new JPanel();
        bigPanel.setLayout(new BoxLayout(bigPanel, BoxLayout.Y_AXIS));
        this.pack();
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(GuiComponents.getImageFromClasspath(Configuration.CLIENT_ICON_PATH));
        this.setTitle("PO II Client");

        /// Populate FileObserver eventQueue based on comparision of client and server mapping
        ClientMapping newMapping = newMappingResult.key();
        @Nullable Pair<List<FileMapping>, List<SharedFileMapping>> serMapping;
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-0x1E);
        }
        serMapping = messenger.requestMapping();
        /// Mapping from server contains user / shared files - compare and populate queue
        if (serMapping != null && (!serMapping.key().isEmpty() || !serMapping.value().isEmpty())) {
            List<FileMapping> updateOnClient = new ArrayList<>();
            List<FileMapping> updateOnServer = new ArrayList<>();
            List<FileMapping> updateOnEither = new ArrayList<>();
            /// Current mapping contains user files
            if (newMapping.getFiles() != null) {
                /// Populate list of files to update
                for (ClientFileMapping clientFile : newMapping.getFiles())
                    for (FileMapping serverFile : serMapping.key()) {
                        if (clientFile.getPathname()
                                      .equals(serverFile.getPathname())) {
                            updateOnEither.add(clientFile);
                            updateOnEither.add(serverFile);
                            if (clientFile.getModification_timestamp() > serverFile.getModification_timestamp()) {
                                updateOnServer.add(clientFile);
                            } else if (clientFile.getModification_timestamp() <
                                       serverFile.getModification_timestamp()) {
                                updateOnClient.add(serverFile);
                            }
                        }
                    }
                /// Populate list of files to resolve
                List<FileMapping> resolveOnClient = new ArrayList<>(newMapping.getFiles());
                List<FileMapping> resolveOnServer = new ArrayList<>(serMapping.key());
                resolveOnClient.removeAll(updateOnEither);
                resolveOnServer.removeAll(updateOnEither);
                this.notificationSender.success(
                        "Mapping sync finished",
                        "To update: " + updateOnClient.size() + "/" + updateOnServer.size() +
                        "\nTo resolve: " + resolveOnClient.size() + "/" + resolveOnServer.size() +
                        "\n(local/remote)"
                );
                for (FileMapping fileMapping : updateOnClient) {
                    this.observer.addEvent(Paths.get(fileMapping.getPathname()), Revise_Node);
                }
                for (FileMapping fileMapping : updateOnServer) {
                    this.observer.addEvent(Paths.get(fileMapping.getPathname()), Update_Node);
                }
                for (FileMapping fileMapping : resolveOnClient) {
                    int option = JOptionPane.showOptionDialog(this,
                                                              fileMapping.getPathname() +
                                                              " is not paired with remote file, what to do with (closing will preserve)",
                                                              "Resolve file",
                                                              JOptionPane.YES_NO_OPTION,
                                                              JOptionPane.QUESTION_MESSAGE,
                                                              null,
                                                              new String[]{"Preserve", "Delete"},
                                                              null);
                    if (option == JOptionPane.YES_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        this.observer.addEvent(Paths.get(fileMapping.getPathname()), Create_Node);
                    } else {
                        this.observer.addEvent(Paths.get(fileMapping.getPathname()), Remove_Node);
                    }
                }
                for (FileMapping fileMapping : resolveOnServer) {
                    int option = JOptionPane.showOptionDialog(this,
                                                              fileMapping.getPathname() +
                                                              " is not paired with local file, what to do with (closing will preserve)",
                                                              "Resolve file",
                                                              JOptionPane.YES_NO_OPTION,
                                                              JOptionPane.QUESTION_MESSAGE,
                                                              null,
                                                              new String[]{"Preserve", "Delete"},
                                                              null);
                    if (option == JOptionPane.YES_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        this.observer.addEvent(Paths.get(fileMapping.getPathname()), Devour_Node);
                    } else {
                        this.observer.addEvent(Paths.get(fileMapping.getPathname()), Delete_Node);
                    }
                }
            /// Current mapping contains no user files - add all local files to queue
            } else {
                this.notificationSender.success(
                        "Mapping sync finished",
                        "No local files detected: downloading all " + serMapping.key().size()
                );
                for (FileMapping fileMapping : serMapping.key()) {
                    this.observer.addEvent(Paths.get(fileMapping.getPathname()), Devour_Node);
                }
            }

            /// Current mapping contain shared files - remove all no longer shared
            if (newMapping.getShared_files() != null) {
                @NotNull Pair<List<FileMapping>, List<SharedFileMapping>> finalSerMapping = serMapping;
                List<SharedFileMapping> filesToUnshare = new ArrayList<>();
                newMapping.getShared_files()
                          .stream()
                          /// Remove all no longer shared
                          .filter(f -> finalSerMapping.value()
                                                      .stream()
                                                      .noneMatch(sf -> (sf.getPathname().equals(f.getPathname()) &&
                                                                       sf.getOwner().equals(f.getOwner()))))
                          .forEach(filesToUnshare::add);
                for (SharedFileMapping sharedFileMapping : filesToUnshare) {
                    this.observer.addEvent(Paths.get(sharedFileMapping.getPathname()),
                                           Unlink_Node, sharedFileMapping.getOwner());
                }
//                newMapping.getShared_files().removeIf(
//                        f -> finalSerMapping.value()
//                                .stream()
//                                .noneMatch(sf -> (sf.getPathname().equals(f.getPathname()) &&
//                                                  sf.getOwner().equals(f.getOwner()))));
            }

            for (SharedFileMapping sharedFileMapping : serMapping.value()) {
                this.observer.addEvent(Paths.get(sharedFileMapping.getPathname()),
                                       Couple_Node, sharedFileMapping.getOwner());
            }
        /// Server returned empty mapping - add all local files to queue
        } else if (serMapping != null) {
            if (newMapping.getFiles() != null) {
                this.notificationSender.success(
                        "Mapping sync finished",
                        "No remote files detected: uploading all " + newMapping.getFiles().size()
                );
                for (ClientFileMapping fileMapping : newMapping.getFiles()) {
                    this.observer.addEvent(Paths.get(fileMapping.getPathname()), Create_Node);
                }
            }
        /// Server did not respond
        } else {
            JOptionPane.showMessageDialog(this,
                                          "Cannot connect to Server!",
                                          "Connection failed",
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(-0x10E);
        }

        /// Run all worker threads (just now, otherwise if client takes to long to specify fate
        /// of resolved files, then workers will exceed timeout on socket)
        for (int i = 0; i < Configuration.THREAD_PER_USER; i++) {
            OperationLabel label = new OperationLabel();
            label.idle("Waiting", "...");
            bigPanel.add(label);
            new FileEventWorker(log,
                                new Socket(Configuration.SERVER_ADDRESS,
                                           Configuration.SERVER_PORT),
                                username,
                                observer,
                                mapper,
                                fileTree,
                                sharedTree,
                                userPath,
                                this.notificationSender,
                                label).start();
        }
        messenger.start();
        scrollPane.getViewport().add(bigPanel);
        jPanel.add(BorderLayout.CENTER, scrollPane);
        panel.add(jPanel);
        this.pack();
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            App app = new App(args[0], args[1]);
        } else {
            System.err.println("Need two parameters - username, path to user storage");
        }
    }
}
