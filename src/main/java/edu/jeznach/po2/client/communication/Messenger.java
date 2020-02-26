package edu.jeznach.po2.client.communication;

import edu.jeznach.po2.client.gui.NotificationSender;
import edu.jeznach.po2.common.communication.Messages;
import edu.jeznach.po2.common.file.FileManager;
import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.FileObserver;
import edu.jeznach.po2.common.file.SharedFileMapping;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.BreakableInputStream;
import edu.jeznach.po2.common.util.LogHelper;
import edu.jeznach.po2.common.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("FieldCanBeLocal")
public class Messenger extends Thread {
;
    private Log log;
    private LogHelper helper;
    private Socket sendingSocket;
    private final Socket receivingSocket;
    private PrintStream outS;
    private final BreakableInputStream inS;
    private PrintStream outR;
    private final BreakableInputStream inR;
    private String username;
    private FileObserver fileObserver;
    private NotificationSender sender;

    public Messenger(@NotNull Log log,
                     @NotNull Socket sendingSocket,
                     @NotNull Socket receivingSocket,
                     @NotNull String username,
                     @NotNull FileObserver fileObserver,
                     @NotNull NotificationSender sender) throws IOException {
        this.helper = new LogHelper(this.log = log);
        this.sendingSocket = sendingSocket;
        this.receivingSocket = receivingSocket;
        this.sender = sender;
        this.sendingSocket.setSoTimeout(12_000);
        this.receivingSocket.setSoTimeout(12_000);
        this.outS = new PrintStream(sendingSocket.getOutputStream());
        this.inS = new BreakableInputStream(sendingSocket.getInputStream());
        this.outR = new PrintStream(receivingSocket.getOutputStream());
        this.inR = new BreakableInputStream(receivingSocket.getInputStream());
        this.username = username;
        this.fileObserver = fileObserver;
        outR.println(username);
    }

    public @Nullable Pair<List<FileMapping>, List<SharedFileMapping>> requestMapping() {
        synchronized (inS) {
            outS.println(new Messages.RequestMapping(username).toString());
            AtomicReference<String> response = new AtomicReference<>();
            if (helper.expect(() -> response.set(new String(inS.readLine())),
                              "Server not responding")) return null;
            int size;
            if (response.get().equals(Messages.NO_USER)) {
                return Pair.of(Collections.emptyList(), Collections.emptyList());
            } else {
                if (helper.expect(() -> response.set(new String(inS.readLine())),
                                  "Server not responding")) return null;
                Messages.Msg msg = Messages.parseMessage(response.get());
                AtomicReference<Long> s = new AtomicReference<>();
                //noinspection ConstantConditions <- expected
                if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                                  "No size provided in response")) return null;
                size = s.get().intValue();
            }
            AtomicReference<String> receivedMapping = new AtomicReference<>();
            if (helper.expect(() -> receivedMapping.set(FileManager.receiveString(sendingSocket.getInputStream(),
                                                                                  size, true)),
                              "Cannot receive mapping")) return null;
            AtomicReference<Object> mapping = new AtomicReference<>();
            if (helper.expect(() -> mapping.set(FileManager.fromString(receivedMapping.get())),
                              "Cannot deserialize mapping")) return null;
            AtomicReference<Pair<List<FileMapping>, List<SharedFileMapping>>> mappingPair = new AtomicReference<>();
            //noinspection unchecked <- expected
            if (helper.expect(() -> mappingPair.set((Pair<List<FileMapping>, List<SharedFileMapping>>) mapping.get()),
                              "Cannot cast mapping")) return null;

            return mappingPair.get();
        }
    }

    public @Nullable List<String> requestUsers() {
        outS.println(new Messages.RequestUsers(username).toString());
        AtomicReference<String> response = new AtomicReference<>();
        if (helper.expect(() -> response.set(new String(inS.readLine())),
                          "Server not responding")) return null;
        if (response.get().equals(Messages.NO_USER)) {
            return null;
        }
        int size;
        if (helper.expect(() -> response.set(new String(inS.readLine())),
                          "Server not responding")) return null;
        Messages.Msg msg = Messages.parseMessage(response.get());
        AtomicReference<Long> s = new AtomicReference<>();
        //noinspection ConstantConditions <- expected
        if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                          "No size provided in response")) return null;
        size = s.get().intValue();

        AtomicReference<String> receivedListing = new AtomicReference<>();
        if (helper.expect(() -> receivedListing.set(FileManager.receiveString(sendingSocket.getInputStream(),
                                                                              size, true)),
                          "Cannot receive user list")) return null;
        AtomicReference<Object> listing = new AtomicReference<>();
        if (helper.expect(() -> listing.set(FileManager.fromString(receivedListing.get())),
                          "Cannot deserialize user list")) return null;
        AtomicReference<List<String>> usersList = new AtomicReference<>();
        //noinspection unchecked <- expected
        if (helper.expect(() -> usersList.set((List<String>) listing.get()),
                          "Cannot cast user list")) return null;

        return usersList.get();
    }

    public @Nullable List<String> requestReceivers(String filepath) {
        outS.println(new Messages.RequestReceivers(username, filepath));
        AtomicReference<String> response = new AtomicReference<>();
        if (helper.expect(() -> response.set(new String(inS.readLine())),
                          "Server not responding")) return null;
        if (response.get().equals(Messages.NO_USER) || response.get().equals(Messages.NO)) {
            return null;
        }
        int size;
        if (helper.expect(() -> response.set(new String(inS.readLine())),
                          "Server not responding")) return null;
        Messages.Msg msg = Messages.parseMessage(response.get());
        AtomicReference<Long> s = new AtomicReference<>();
        //noinspection ConstantConditions <- expected
        if (helper.expect(() -> s.set(((Messages.ModFileMsg) msg).size()),
                          "No size provided in response")) return null;
        size = s.get().intValue();
        AtomicReference<String> receivedListing = new AtomicReference<>();
        if (helper.expect(() -> receivedListing.set(FileManager.receiveString(sendingSocket.getInputStream(),
                                                                              size, true)),
                          "Cannot receive receivers list")) return null;
        AtomicReference<Object> listing = new AtomicReference<>();
        if (helper.expect(() -> listing.set(FileManager.fromString(receivedListing.get())),
                          "Cannot deserialize receivers list")) return null;
        AtomicReference<List<String>> receiversList = new AtomicReference<>();
        //noinspection unchecked <- expected
        if (helper.expect(() -> receiversList.set((List<String>) listing.get()),
                          "Cannot cast receivers list")) return null;

        return receiversList.get();
    }

    @Override
    public void run() {
        while (true) {
            String msg;
            try {
                msg = new String(inR.readLine());
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                log.exception(e);
                break;
            }
            System.out.println(msg);
            Messages.Msg message = Messages.parseMessage(msg);
            System.out.println("notified: " + message);
            if (message instanceof Messages.ShareFile) {
                this.sender.information(
                        "New file was shared",
                        ((Messages.ShareFile) message).file() + " by " + ((Messages.ShareFile) message).user()
                        );
                fileObserver.addEvent(Paths.get(((Messages.ShareFile) message).file()),
                                      FileObserver.FileEvent.Type.Couple_Node,
                                      ((Messages.ShareFile) message).user());
            }
            if (message instanceof Messages.UnshareFile) {
                this.sender.information(
                        "Sharing file was cancelled",
                        ((Messages.UnshareFile) message).file() + " by " + ((Messages.UnshareFile) message).user()
                );
                fileObserver.addEvent(Paths.get(((Messages.UnshareFile) message).file()),
                                      FileObserver.FileEvent.Type.Unlink_Node,
                                      ((Messages.UnshareFile) message).user());
            }

        }
    }
}
