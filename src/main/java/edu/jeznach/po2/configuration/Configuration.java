package edu.jeznach.po2.configuration;

import edu.jeznach.po2.gui.NotificationSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Configuration {

    @NotNull public static final Integer THREAD_PER_USER;
    private static final Integer DEFAULT_THREAD_PER_USER = 5;

    @NotNull public static final Integer SIZE_PER_USER$MB;
    private static final Integer DEFAULT_SIZE_PER_USER = 1024;

    @Nullable public static final String PATH;
    private static final String DEFAULT_PATH = null;

    @NotNull public static final Integer DRIVE_COUNT;
    private static final Integer DEFAULT_DRIVE_COUNT = 5;

    @NotNull public static final String SERVER_ICON_PATH;
    private static final String DEFAULT_SERVER_ICON_PATH = "";

    @NotNull public static final String CLIENT_ICON_PATH;
    private static final String DEFAULT_CLIENT_ICON_PATH = "";

    private static final String CONF_YML_PATH = "src/main/resources/edu/jeznach/po2/confyml";

    static {
        @NotNull Integer threadPerUser;
        @NotNull Integer sizePerUser$Mb;
        @Nullable String path;
        @NotNull Integer driveCount;
        @NotNull String serverIconPath;
        @NotNull String clientIconPath;
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(CONF_YML_PATH);
            Configuration configuration = yaml.load(inputStream);
            threadPerUser = configuration.application.getThread_per_user();
            sizePerUser$Mb = configuration.application.getSize_per_user();
            path = configuration.server.getPath();
            driveCount = configuration.server.getDrive_count();
            serverIconPath = configuration.server.getIcon_path();
            clientIconPath = configuration.client.getIcon_path();
        } catch (IOException e) {
            {
                NotificationSender sender = new NotificationSender(new ImageIcon("").getImage(), "edu.jeznach.po2");
                sender.error("Could not load configuration", e.getMessage());
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(7500);
                            } catch (InterruptedException ignored) {
                            } finally {
                                sender.disposeTrayIcon();
                            }
                        }
                    }
                }.start();
            }
            threadPerUser = DEFAULT_THREAD_PER_USER;
            sizePerUser$Mb = DEFAULT_SIZE_PER_USER;
            path = DEFAULT_PATH;
            driveCount = DEFAULT_DRIVE_COUNT;
            serverIconPath = DEFAULT_SERVER_ICON_PATH;
            clientIconPath = DEFAULT_CLIENT_ICON_PATH;
        }
        DRIVE_COUNT = driveCount;
        PATH = path;
        SIZE_PER_USER$MB = sizePerUser$Mb;
        THREAD_PER_USER = threadPerUser;
        SERVER_ICON_PATH = serverIconPath;
        CLIENT_ICON_PATH = clientIconPath;
    }

    @NotNull private Application application;
    @NotNull public Application getApplication() { return application; }
    public void setApplication(@NotNull Application application) { this.application = application; }

    @NotNull private Server server;
    @NotNull public Server getServer() { return server; }
    public void setServer(@NotNull Server server) { this.server = server; }

    @NotNull private Client client;
    @NotNull public Client getClient() { return this.client; }
    public void setClient(@NotNull Client client) { this.client = client; }

    public Configuration() { }

    public Configuration(@NotNull Application application,
                         @NotNull Server server) {
        this.application = application;
        this.server = server;
    }

    public static class Application {

        @NotNull private Integer thread_per_user;
        @NotNull public Integer getThread_per_user() { return thread_per_user; }
        public void setThread_per_user(@NotNull Integer thread_per_user) { this.thread_per_user = thread_per_user; }

        @NotNull private Integer size_per_user;
        @NotNull public Integer getSize_per_user() { return size_per_user; }
        public void setSize_per_user(@NotNull Integer size_per_user) { this.size_per_user = size_per_user; }

        public Application() { }

        public Application(@NotNull Integer thread_per_user, @NotNull Integer size_per_user) {
            this.thread_per_user = thread_per_user;
            this.size_per_user = size_per_user;
        }
    }

    public static class Server {

        @Nullable private String path;
        @Nullable public String getPath() { return path; }
        public void setPath(@Nullable String path) { this.path = path; }

        @NotNull private Integer drive_count;
        @NotNull public Integer getDrive_count() { return drive_count; }
        public void setDrive_count(@NotNull Integer drive_count) { this.drive_count = drive_count; }

        @NotNull private String icon_path;
        @NotNull public String getIcon_path() { return this.icon_path; }
        public void setIcon_path(@NotNull String icon_path) { this.icon_path = icon_path; }

        public Server() { }

        public Server(@Nullable String path,
                      @NotNull Integer drive_count,
                      @NotNull String icon_path) {
            this.path = path;
            this.drive_count = drive_count;
            this.icon_path = icon_path;
        }
    }

    public static class Client {

        @NotNull private String icon_path;
        @NotNull public String getIcon_path() { return this.icon_path; }
        public void setIcon_path(@NotNull String icon_path) { this.icon_path = icon_path; }

        public Client() { }

        public Client(@NotNull String icon_path) {
            this.icon_path = icon_path;
        }
    }
}
