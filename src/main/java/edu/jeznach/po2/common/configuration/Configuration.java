package edu.jeznach.po2.common.configuration;

import edu.jeznach.po2.common.gui.NotificationSender;
import edu.jeznach.po2.common.util.Throwables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Used to load and provide at runtime properties that were read from configuration file.
 * Properties in this class are stored in java constants (final fields), and should
 * provide effectively unique names (as those may not be 100% same as those in configuration
 * file).
 */
public final class Configuration {

    /** how many threads user application uses to exchange files with server */
    public static final @NotNull Integer THREAD_PER_USER;
    private static final Integer DEFAULT_THREAD_PER_USER = 5;

    /** size limit of storage for each user in megabytes */
    public static final @NotNull Integer SIZE_PER_USER$MB;
    private static final Integer DEFAULT_SIZE_PER_USER = 1024;

    /** should console output use color */
    public static final @NotNull Boolean PRINT_COLOR;
    private static final Boolean DEFAULT_PRINT_COLOR = false;

    /**
     * algorithm used to calculate checksum of files
     * <p>allowed: MD5, SHA-1, SHA-256
     */
    public static final @NotNull String CHECKSUM_ALGORITHM;
    private static final String DEFAULT_CHECKSUM_ALGORITHM = "SHA-1";

    /** which absolute path should be used for server storage, if null will use project directory */
    public static final @Nullable String PATH;
    private static final String DEFAULT_PATH = null;

    /** how many different pseudo-drives should server use */
    public static final @NotNull Integer DRIVE_COUNT;
    private static final Integer DEFAULT_DRIVE_COUNT = 5;

    /** where server icon is located */
    public static final @NotNull String SERVER_ICON_PATH;
    private static final String DEFAULT_SERVER_ICON_PATH = "";

    /** where client icon is located */
    public static final @NotNull String CLIENT_ICON_PATH;
    private static final String DEFAULT_CLIENT_ICON_PATH = "";

    private static final String CONF_YML_PATH = "/edu/jeznach/po2/conf.yml";

    static {
        @NotNull Integer threadPerUser;
        @NotNull Integer sizePerUser$Mb;
        @NotNull Boolean printColor;
        @Nullable String path;
        @NotNull String checksumAlgorithm;
        @NotNull Integer driveCount;
        @NotNull String serverIconPath;
        @NotNull String clientIconPath;
        try {
            Yaml yaml = new Yaml();
            Reader reader = new InputStreamReader(Configuration.class.getResourceAsStream(CONF_YML_PATH));
            Configuration configuration = yaml.load(reader);
            threadPerUser = configuration.application.getThread_per_user();
            sizePerUser$Mb = configuration.application.getSize_per_user();
            printColor = configuration.application.getPrint_color();
            checksumAlgorithm = configuration.application.getChecksum_algorithm();
            path = configuration.server.getPath();
            driveCount = configuration.server.getDrive_count();
            serverIconPath = configuration.server.getIcon_path();
            clientIconPath = configuration.client.getIcon_path();
            reader.close();
        } catch (Throwable e) {
            {
                NotificationSender sender = new NotificationSender(new ImageIcon("").getImage(),
                                                                   "edu.jeznach.po2",
                                                                   null);
                sender.error("Could not load configuration", Throwables.getStackTrace(e));
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(7500);
                            } catch (InterruptedException ignored) {
                            } finally {
                                sender.close();
                            }
                        }
                    }
                }.start();
            }
            threadPerUser = DEFAULT_THREAD_PER_USER;
            sizePerUser$Mb = DEFAULT_SIZE_PER_USER;
            printColor = DEFAULT_PRINT_COLOR;
            path = DEFAULT_PATH;
            checksumAlgorithm = DEFAULT_CHECKSUM_ALGORITHM;
            driveCount = DEFAULT_DRIVE_COUNT;
            serverIconPath = DEFAULT_SERVER_ICON_PATH;
            clientIconPath = DEFAULT_CLIENT_ICON_PATH;
        }
        THREAD_PER_USER = threadPerUser;
        DRIVE_COUNT = driveCount;
        PATH = path;
        switch (checksumAlgorithm) {
            case "MD5":
            case "SHA-1":
            case "SHA-256":
                CHECKSUM_ALGORITHM = checksumAlgorithm;
                break;
            default: {
                NotificationSender sender = new NotificationSender(new ImageIcon("").getImage(),
                                                                   "edu.jeznach.po2",
                                                                   null);
                sender.error("Unknown algorithm: " + checksumAlgorithm,
                             "Using default one: " + DEFAULT_CHECKSUM_ALGORITHM);
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
                CHECKSUM_ALGORITHM = DEFAULT_CHECKSUM_ALGORITHM;
            }
        }
        PRINT_COLOR = printColor;
        SIZE_PER_USER$MB = sizePerUser$Mb;
        SERVER_ICON_PATH = serverIconPath;
        CLIENT_ICON_PATH = clientIconPath;
    }

    private @NotNull Application application = new Application();
    public @NotNull Application getApplication() { return application; }
    public void setApplication(@NotNull Application application) { this.application = application; }

    private @NotNull Server server = new Server();
    public @NotNull Server getServer() { return server; }
    public void setServer(@NotNull Server server) { this.server = server; }

    private @NotNull Client client = new Client();
    public @NotNull Client getClient() { return this.client; }
    public void setClient(@NotNull Client client) { this.client = client; }

    Configuration() { }

    /**
     * Represents application node.
     * <br>Hosts configuration shared between server and client.
     */
    public static final class Application {

        private @NotNull Integer thread_per_user = DEFAULT_THREAD_PER_USER;
        public @NotNull Integer getThread_per_user() { return thread_per_user; }
        public void setThread_per_user(@NotNull Integer thread_per_user) { this.thread_per_user = thread_per_user; }

        private @NotNull Integer size_per_user = DEFAULT_SIZE_PER_USER;
        public @NotNull Integer getSize_per_user() { return size_per_user; }
        public void setSize_per_user(@NotNull Integer size_per_user) { this.size_per_user = size_per_user; }

        private @NotNull Boolean print_color = DEFAULT_PRINT_COLOR;
        public @NotNull Boolean getPrint_color() { return this.print_color; }
        public void setPrint_color(@NotNull Boolean print_color) { this.print_color = print_color; }

        private @NotNull String checksum_algorithm = DEFAULT_CHECKSUM_ALGORITHM;
        public @NotNull String getChecksum_algorithm() { return this.checksum_algorithm; }
        public void setChecksum_algorithm(@NotNull String checksum_algorithm) { this.checksum_algorithm = checksum_algorithm; }

        Application() { }
    }

    /**
     * Represents server node.
     * <br>Hosts server-specific configuration.
     */
    public static final class Server {

        private @Nullable String path;
        public @Nullable String getPath() { return path; }
        public void setPath(@Nullable String path) { this.path = path; }

        private @NotNull Integer drive_count = DEFAULT_DRIVE_COUNT;
        public @NotNull Integer getDrive_count() { return drive_count; }
        public void setDrive_count(@NotNull Integer drive_count) { this.drive_count = drive_count; }

        private @NotNull String icon_path = DEFAULT_SERVER_ICON_PATH;
        public @NotNull String getIcon_path() { return this.icon_path; }
        public void setIcon_path(@NotNull String icon_path) { this.icon_path = icon_path; }

        Server() { }
    }

    /**
     * Represents client node.
     * <br>Hosts client-specific configuration.
     */
    public static final class Client {

        private @NotNull String icon_path = DEFAULT_CLIENT_ICON_PATH;
        public @NotNull String getIcon_path() { return this.icon_path; }
        public void setIcon_path(@NotNull String icon_path) { this.icon_path = icon_path; }

        Client() { }
    }
}
