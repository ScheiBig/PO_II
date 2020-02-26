package edu.jeznach.po2.server.work;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.server.App;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadWorkerProvider extends Thread {

    private ServerSocket serverSocket;
    private Log log;
    private App app;

    public ThreadWorkerProvider(@NotNull Log log, @NotNull App app) throws IOException {
        this.serverSocket = new ServerSocket(Configuration.SERVER_PORT);
        this.log = log;
        this.app = app;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket newSocket = serverSocket.accept();
                new FileEventWorker(log, newSocket, app).start();
            } catch (IOException e) {
                log.exception(e);
                System.exit(-0x10E);
            }
        }
    }
}
