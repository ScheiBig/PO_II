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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserCommunicationWorker {

//    private Log log;
//    private LogHelper helper;
//    private Socket socket;
    private PrintStream out;
    private OutputStream byteOut;
    private String username;
//    private BreakableInputStream in;
//    private InputStream byteIn;
//    private App app;

    public UserCommunicationWorker(@NotNull Log log,
                                   @NotNull Socket socket,
                                   @NotNull App app,
                                   @NotNull String username) throws IOException {
//        this.helper = new LogHelper(this.log = log);
//        this.socket = socket;
        this.byteOut = socket.getOutputStream();
        this.username = username;
        this.out = new PrintStream(byteOut);
//        this.byteIn = socket.getInputStream();
//        this.in = new BreakableInputStream(byteIn);
//        this.app = app;
    }

    public void notifyShared(String sharer, String filepath) {
        System.out.println("notify: " + new Messages.ShareFile(sharer, filepath, username));
        out.println(new Messages.ShareFile(sharer, filepath, username));
    }

    public void notifyUnshared(String sharer, String filepath) {
        System.out.println("notify: " + new Messages.UnshareFile(sharer, filepath, username));
        out.println(new Messages.UnshareFile(sharer, filepath, username));
    }

}
