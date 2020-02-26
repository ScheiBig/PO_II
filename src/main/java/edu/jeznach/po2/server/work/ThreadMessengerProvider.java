package edu.jeznach.po2.server.work;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.BreakableInputStream;
import edu.jeznach.po2.server.App;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadMessengerProvider
        extends Thread {

    private ServerSocket serverSocket;
    private Log log;
    private App app;

    public ThreadMessengerProvider(@NotNull Log log, @NotNull App app) throws IOException {
        this.serverSocket = new ServerSocket(Configuration.COMMUNICATION_PORT);
        this.log = log;
        this.app = app;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket newSocket = serverSocket.accept();
                String username = new String(new BreakableInputStream(newSocket.getInputStream()).readLine());
                UserCommunicationWorker messenger = new UserCommunicationWorker(log, newSocket, app, username);
                app.getMessengers().put(username, messenger);
                System.out.println("Registered messenger: " + username);
            } catch (IOException e) {
                log.exception(e);
                System.exit(-0x10E);
            }
        }
    }
}
