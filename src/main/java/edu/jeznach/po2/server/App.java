package edu.jeznach.po2.server;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.file.SharedFileMapping;
import edu.jeznach.po2.common.gui.FileTree;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.GuiComponents;
import edu.jeznach.po2.common.util.Pair;
import edu.jeznach.po2.server.file.DriveFileMapper;
import edu.jeznach.po2.server.file.DriveMapping;
import edu.jeznach.po2.server.work.ThreadMessengerProvider;
import edu.jeznach.po2.server.work.ThreadWorkerProvider;
import edu.jeznach.po2.server.work.UserCommunicationWorker;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App extends JFrame {

    public static final String DRIVE_MAPPING_YAML = "drive.yaml";
    private static final String LOG_NAME = "log.txt";
    public final String serverPath;
    private @NotNull List<DriveFileMapper> mappers;
    public @NotNull List<DriveFileMapper> getMappersCopy() { return new ArrayList<>(this.mappers); }
    private final @NotNull Map<DriveFileMapper, Integer> mappersWithUsage;
    public @NotNull DriveFileMapper acquireMapper() {
        synchronized (mappersWithUsage) {
            List<Map.Entry<DriveFileMapper, Integer>> list = new LinkedList<>(mappersWithUsage.entrySet());
            list.sort(Map.Entry.comparingByValue());
            Map.Entry<DriveFileMapper, Integer> leastOccupied = list.get(0);
            Integer value = leastOccupied.setValue(leastOccupied.getValue() + 1);
            return leastOccupied.getKey();
        }
    }
    public @NotNull DriveFileMapper forceMapper(@NotNull String drive) {
        synchronized (mappersWithUsage) {
            List<Map.Entry<DriveFileMapper, Integer>> list = new LinkedList<>(mappersWithUsage.entrySet());
            Optional<Map.Entry<DriveFileMapper, Integer>> mapper = list.stream()
                                                                       .filter(m -> m.getKey()
                                                                                     .getMapping()
                                                                                     .getName()
                                                                                     .equals(drive))
                                                                       .findFirst();
            if (mapper.isPresent()) {
                int value = mapper.get().getValue() + 1;
                mapper.get().setValue(value);
                return mapper.get().getKey();
            } else throw new IllegalArgumentException();
        }
    }
    public void releaseMapper(DriveFileMapper mapper) {
        synchronized (mappersWithUsage) {
            int usage = mappersWithUsage.get(mapper);
            mappersWithUsage.put(mapper, usage - 1);
        }
    }

    private @NotNull Map<String, UserCommunicationWorker> messengers = new HashMap<>();
    @NotNull public Map<String, UserCommunicationWorker> getMessengers() { return messengers; }

    public static int getDriveIndex(String drive) {
        return Integer.parseInt(drive.substring("drive_".length()));
    }
    private @NotNull List<FileTree> fileTrees;
    public @NotNull List<FileTree> getFileTrees() { return this.fileTrees; }
    private @NotNull List<JTextArea> commandFields;
    @NotNull public List<JTextArea> getCommandFields() { return commandFields; }
    private @NotNull Map<String, Log> logs;
    public @NotNull Map<String, Log> getLogs() { return this.logs; }
    private @NotNull Log log;
    public @NotNull Log getLog() { return this.log; }

    @SuppressWarnings("DuplicateThrows")
    public App(@NotNull String serverPath) throws FileNotFoundException, IOException {
        /// Load all drives and create mappers
        this.serverPath = serverPath;
        File log = new File(serverPath + File.separator + LOG_NAME);
        this.log = new Log(log);
        this.mappers = new ArrayList<>();
        this.mappersWithUsage = new HashMap<>();
        for (int i = 0; i < Configuration.DRIVE_COUNT; i++) {
            File drive = new File(serverPath + File.separator + "drive_" + i);
            //noinspection ResultOfMethodCallIgnored
            drive.mkdirs();
            DriveMapping currentMapping;
            try {
                currentMapping = DriveFileMapper.provider.loadStructure(
                        new File(drive.getAbsolutePath() + File.separator + DRIVE_MAPPING_YAML)
                );
            } catch (FileNotFoundException e) {
                this.log.exception("No current mapping available", e);
                currentMapping = null;
            }

            Pair<DriveMapping, Boolean> createdMapping = DriveFileMapper.provider.createStructure(
                    new File(DRIVE_MAPPING_YAML),
                    new DriveMapping.InitParams(drive, LOG_NAME)
            );
            if (currentMapping != null) {
                currentMapping.getUsers()
                              .stream()
                              .filter(u -> u.getShared_files() != null)
                              .peek(u -> System.out.println(u + "does files"))
                              .filter(u -> u.getShared_files().size() > 0)
                              .peek(u -> System.out.println(u + "has files"))
                              .forEach(u -> {
                                  Optional<DriveMapping.User> userOptional =
                                          createdMapping.key()
                                                  .getUsers()
                                                  .stream()
                                                  .filter(cu -> cu.getUsername()
                                                                  .equals(u.getUsername()))
                                                  .findFirst();
                                  DriveMapping.User user;
                                  if (!userOptional.isPresent()) {
                                      user = new DriveMapping.User(u.getUsername());
                                      createdMapping.key().getUsers().add(user);
                                  } else {
                                      user = userOptional.get();
                                  }
                                  if (user.getShared_files() == null) {
                                      user.setShared_files(new ArrayList<>());
                                  }
                                  user.getShared_files().addAll(u.getShared_files());
                              });
            }
            DriveFileMapper mapper = new DriveFileMapper(createdMapping.key(),
                                                    new File(drive.getAbsolutePath() +
                                                             File.separator + DRIVE_MAPPING_YAML));
            mappers.add(mapper);
            mappersWithUsage.put(mapper, 0);
        }

        /// Create GUI and show
        this.fileTrees = new ArrayList<>();
        this.commandFields = new ArrayList<>();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        this.getContentPane().add(panel);
        mappers.forEach(dfm -> {
            FileTree fileTree = new FileTree(dfm.getMapping()
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
                                         dfm.getMapping()
                                            .getName(),
                                         FileTree.Type.driveDirectory,
                                         null
            );
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JTextArea l = new JTextArea();
            l.setEditable(false);
            JScrollPane s = new JScrollPane(l);
            s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            GuiComponents.setExactSize(s, new Dimension(300, 150));
            GuiComponents.setExactSize(s, new Dimension(300, 150));
            GuiComponents.setExactSize(s, new Dimension(300, 150));
            p.add(fileTree);
            p.add(s);
            panel.add(p);
            fileTrees.add(fileTree);
            commandFields.add(l);
        });

        this.pack();
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(GuiComponents.getImageFromClasspath(Configuration.SERVER_ICON_PATH));
        this.setTitle("PO II Server");

        new ThreadWorkerProvider(this.log, this).start();
        new ThreadMessengerProvider(this.log, this).start();
    }

    public static void main(String[] args) throws IOException {
        App app = new App("C:\\PO_II\\serversB");
    }
}
