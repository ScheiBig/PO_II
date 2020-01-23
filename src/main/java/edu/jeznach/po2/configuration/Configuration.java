package edu.jeznach.po2.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class Configuration {

    @NotNull public static final Integer THREAD_PER_USER;
    private static final Integer DEFAULT_THREAD_PER_USER = 5;

    @NotNull public static final Integer SIZE_PER_USER$MB;
    private static final Integer DEFAULT_SIZE_PER_USER = 1024;

    @Nullable public static final String PATH;
    private static final String DEFAULT_PATH = null;

    @NotNull public static final Integer DRIVE_COUNT;
    private static final Integer DEFAULT_DRIVE_COUNT = 5;

    static {
        @NotNull Integer threadPerUser;
        @NotNull Integer sizePerUser$Mb;
        @Nullable String path;
        @NotNull Integer driveCount;
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream("src/main/resources/edu/jeznach/po2/conf.yml");
            Configuration configuration = yaml.load(inputStream);
            threadPerUser = configuration.application.getThread_per_user();
            sizePerUser$Mb = configuration.application.getSize_per_user();
            path = configuration.server.getPath();
            driveCount = configuration.server.getDrive_count();
        } catch (IOException e) {
            {
                //TODO log that configuration was not provided
                e.printStackTrace();
                System.err.println("TODO log that configuration was not provided");
            }
            threadPerUser = DEFAULT_THREAD_PER_USER;
            sizePerUser$Mb = DEFAULT_SIZE_PER_USER;
            path = DEFAULT_PATH;
            driveCount = DEFAULT_DRIVE_COUNT;
        }
        DRIVE_COUNT = driveCount;
        PATH = path;
        SIZE_PER_USER$MB = sizePerUser$Mb;
        THREAD_PER_USER = threadPerUser;
    }

    @NotNull private Application application;
    @NotNull public Application getApplication() { return application; }
    public void setApplication(@NotNull Application application) { this.application = application; }

    @NotNull private Server server;
    @NotNull public Server getServer() { return server; }
    public void setServer(@NotNull Server server) { this.server = server; }

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

        public Server() { }

        public Server(@Nullable String path, @NotNull Integer drive_count) {
            this.path = path;
            this.drive_count = drive_count;
        }
    }
}
